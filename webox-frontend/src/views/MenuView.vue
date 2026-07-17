<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { showToast } from 'vant'
import 'vant/es/toast/style'
import { ApiError } from '@/api/http'
import { addCartItem } from '@/api/cart'
import { getMenuList } from '@/api/menu'
import { useAuthStore } from '@/stores/auth'
import {
  CATEGORY_OPTIONS,
  formatPrice,
  getCategoryLabel,
  getDishId,
} from '@/utils/menu'

const router = useRouter()
const authStore = useAuthStore()
const { email, isLoggedIn } = storeToRefs(authStore)
const loading = ref(false)
const list = ref([])
const activeCategory = ref('all')
const addingId = ref(null)

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
  showToast('已退出登录')
  await router.replace('/login')
}

function goLogin() {
  router.push('/login')
}

onMounted(fetchList)
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
      </van-cell-group>

      <van-loading v-if="loading" class="state" vertical>加载中...</van-loading>

      <van-empty
        v-else-if="!list.length"
        description="今日暂无可订菜品"
      />

      <div v-else class="card-list">
        <div
          v-for="item in list"
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
          class="cart-entry"
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

.footer {
  margin-top: 10px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
</style>
