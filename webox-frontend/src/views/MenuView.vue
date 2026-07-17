<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { showToast } from 'vant'
import 'vant/es/toast/style'
import { ApiError } from '@/api/http'
import { addCartItem } from '@/api/cart'
import { getMenuList } from '@/api/menu'
import { useAuthStore } from '@/stores/auth'
import { usePreferencesStore } from '@/stores/preferences'
import {
  CATEGORY_OPTIONS,
  formatPrice,
  getAllergenLabel,
  getCategoryLabel,
  getDishId,
} from '@/utils/menu'
import {
  applyFlavorSort,
  applyRecommendSort,
  getMatchedUserAllergens,
  hasAllergenConflict,
  isDishAllergenMatched,
} from '@/utils/preferences'

const router = useRouter()
const authStore = useAuthStore()
const preferencesStore = usePreferencesStore()
const { email, isLoggedIn } = storeToRefs(authStore)
const { preferences } = storeToRefs(preferencesStore)

const loading = ref(false)
const list = ref([])
const activeCategory = ref('all')
const addingId = ref(null)
/** all | recommend */
const viewMode = ref('all')

const displayList = computed(() => {
  if (viewMode.value === 'recommend') {
    return applyRecommendSort(list.value, preferences.value)
  }
  return applyFlavorSort(list.value, preferences.value)
})

async function fetchList() {
  loading.value = true
  try {
    const params = {}
    if (activeCategory.value && activeCategory.value !== 'all') {
      params.category = activeCategory.value
    }
    const data = await getMenuList(params)
    list.value = Array.isArray(data) ? data : data?.list || []
  } catch (error) {
    list.value = []
    const message =
      error instanceof ApiError ? error.message : '加载菜单失败'
    showToast(message)
  } finally {
    loading.value = false
  }
}

async function ensurePreferences() {
  if (!isLoggedIn.value) return
  try {
    await preferencesStore.fetchPreferences()
  } catch {
    // 偏好接口未就绪时不影响看菜单
  }
}

function isAllergenRisk(item) {
  return hasAllergenConflict(item.allergens, preferences.value.allergens)
}

/** 命中用户偏好的过敏原文案，如「花生、乳制品」 */
function matchedAllergenText(item) {
  return getMatchedUserAllergens(item.allergens, preferences.value.allergens)
    .map(getAllergenLabel)
    .join('、')
}

function goDetail(item) {
  const id = getDishId(item)
  if (id == null) return
  router.push(`/menu/${id}`)
}

function goCart() {
  router.push('/cart')
}

function goOrders() {
  router.push('/orders')
}

function goPreferences() {
  router.push('/preferences')
}

async function handleAddToCart(item) {
  if (!isLoggedIn.value) {
    showToast('请先登录')
    router.push('/login')
    return
  }

  const menuItemId = getDishId(item)
  if (menuItemId == null) {
    showToast('菜品无效')
    return
  }

  addingId.value = menuItemId
  try {
    await addCartItem({ menuItemId, quantity: 1 })
    showToast('已加入购物车')
  } catch (error) {
    const message =
      error instanceof ApiError ? error.message : '加入购物车失败'
    showToast(message)
  } finally {
    addingId.value = null
  }
}

async function handleLogout() {
  try {
    await authStore.logout()
  } catch {
    // 即使登出接口失败，本地态也会清理
  }
  preferencesStore.clearPreferences()
  showToast('已退出登录')
  await router.replace('/login')
}

function goLogin() {
  router.push('/login')
}

onMounted(async () => {
  // 先拉偏好再拉列表，避免首屏用空偏好跳过辣度/口味排序
  await ensurePreferences()
  await fetchList()
})
</script>

<template>
  <div class="menu-page">
    <van-nav-bar title="今日菜单" fixed placeholder>
      <template #right>
        <van-icon name="shopping-cart-o" size="20" @click="goCart" />
      </template>
    </van-nav-bar>

    <!-- 分类筛选 -->
    <van-tabs
      v-model:active="activeCategory"
      shrink
      sticky
      offset-top="46"
      color="#1989fa"
      title-active-color="#1989fa"
      @change="fetchList"
    >
      <van-tab
        v-for="opt in CATEGORY_OPTIONS"
        :key="opt.value"
        :title="opt.text"
        :name="opt.value"
      />
    </van-tabs>

    <div class="content">
      <van-cell-group inset class="user-bar">
        <van-cell
          v-if="isLoggedIn"
          title="当前用户"
          :value="email || '已登录'"
        />
        <van-cell title="我的订单" is-link @click="goOrders" />
        <van-cell title="偏好设置" is-link @click="goPreferences" />
      </van-cell-group>

      <div class="mode-bar">
        <van-tabs v-model:active="viewMode" type="card" shrink>
          <van-tab title="全部" name="all" />
          <van-tab title="推荐" name="recommend" />
        </van-tabs>
      </div>

      <van-loading v-if="loading" class="state" vertical>加载中...</van-loading>

      <van-empty
        v-else-if="!displayList.length"
        :description="viewMode === 'recommend' ? '暂无符合偏好的菜品' : '今日暂无可订菜品'"
      />

      <div v-else class="card-list">
        <div
          v-for="item in displayList"
          :key="getDishId(item)"
          class="dish-card"
        >
          <div class="cover-wrap" @click="goDetail(item)">
            <van-image
              class="cover"
              width="100%"
              height="140"
              fit="cover"
              :src="item.image"
            >
              <template #error>
                <div class="img-fallback">暂无图片</div>
              </template>
              <template #loading>
                <div class="img-fallback">加载中</div>
              </template>
            </van-image>
          </div>

          <div class="info">
            <div class="row" @click="goDetail(item)">
              <h3 class="name">{{ item.name }}</h3>
              <span class="price">¥{{ formatPrice(item.price) }}</span>
            </div>
            <div
              v-if="item.allergens?.length"
              class="allergen-block"
              :class="{ 'allergen-block--risk': isAllergenRisk(item) }"
              @click.stop
            >
              <p v-if="isAllergenRisk(item)" class="allergen-tip">
                <svg
                  class="allergen-tip-icon"
                  viewBox="0 0 24 24"
                  aria-hidden="true"
                >
                  <path
                    fill="currentColor"
                    d="M12 2.5c.4 0 .8.2 1 .6l9.2 16.1c.4.7-.1 1.6-.9 1.6H2.7c-.8 0-1.3-.9-.9-1.6L11 3.1c.2-.4.6-.6 1-.6zm0 5.2c-.6 0-1 .4-1 1v5.2c0 .6.4 1 1 1s1-.4 1-1V8.7c0-.6-.4-1-1-1zm0 9.2c.7 0 1.2-.5 1.2-1.2S12.7 14.5 12 14.5s-1.2.5-1.2 1.2.5 1.2 1.2 1.2z"
                  />
                </svg>
                含你设置的过敏原：{{ matchedAllergenText(item) }}，请谨慎选择
              </p>
              <p v-else class="allergen-tip allergen-tip--muted">菜品过敏原</p>
              <div class="allergen-row">
                <span
                  v-for="code in item.allergens"
                  :key="code"
                  class="allergen-chip"
                  :class="{
                    'allergen-chip--match': isDishAllergenMatched(
                      code,
                      preferences.allergens,
                    ),
                  }"
                >
                  {{ getAllergenLabel(code) }}
                </span>
              </div>
            </div>
            <div class="footer">
              <van-tag plain type="primary">
                {{ getCategoryLabel(item.category) }}
              </van-tag>
              <van-button
                size="small"
                type="primary"
                round
                :loading="addingId === getDishId(item)"
                loading-text="添加中"
                @click.stop="handleAddToCart(item)"
              >
                加入购物车
              </van-button>
            </div>
          </div>
        </div>
      </div>

      <div class="actions">
        <van-button
          round
          block
          type="primary"
          plain
          hairline
          @click="goCart"
        >
          查看购物车
        </van-button>
        <van-button
          v-if="isLoggedIn"
          round
          block
          type="danger"
          plain
          hairline
          @click="handleLogout"
        >
          退出登录
        </van-button>
        <van-button
          v-else
          round
          block
          type="primary"
          @click="goLogin"
        >
          去登录
        </van-button>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.menu-page {
  min-height: 100vh;
  background: #f7f8fa;
  padding-bottom: 24px;
}

.content {
  padding: 12px 12px 0;
}

.user-bar {
  margin-bottom: 12px;
}

.mode-bar {
  margin: 0 4px 12px;
  display: flex;
  justify-content: center;
}

.actions {
  margin: 24px 4px 8px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.state {
  display: flex;
  justify-content: center;
  padding: 48px 0;
}

.card-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.dish-card {
  overflow: hidden;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.cover-wrap {
  position: relative;
  cursor: pointer;
}

.cover {
  display: block;
  background: #ebedf0;
}

.img-fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  color: #969799;
  font-size: 13px;
  background: #f2f3f5;
}

.info {
  padding: 12px 14px 14px;
}

.row {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  cursor: pointer;
}

.name {
  flex: 1;
  font-size: 16px;
  font-weight: 600;
  color: #323233;
  line-height: 1.4;
}

.price {
  flex-shrink: 0;
  font-size: 16px;
  font-weight: 700;
  color: #ee0a24;
}

.allergen-block {
  margin-top: 8px;
  padding: 8px 10px;
  border-radius: 8px;
  background: #f7f8fa;
}

.allergen-block--risk {
  background: #fff1f0;
}

.allergen-tip {
  margin: 0 0 6px;
  font-size: 12px;
  line-height: 1.5;
  color: #ee0a24;
  font-weight: 500;
}

.allergen-tip-icon {
  display: inline-block;
  width: 14px;
  height: 14px;
  margin-right: 4px;
  vertical-align: -2px;
  flex-shrink: 0;
}

.allergen-tip--muted {
  color: #969799;
  font-weight: 400;
}

.allergen-row {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.allergen-chip {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 11px;
  line-height: 1.4;
  color: #646566;
  background: #fff;
}

.allergen-chip--match {
  color: #fff;
  background: #ff1744;
  font-weight: 700;
}

.footer {
  margin-top: 10px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
</style>
