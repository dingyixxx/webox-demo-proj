<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import 'vant/es/toast/style'
import { ApiError } from '@/api/http'
import { getRecommendations } from '@/api/recommendations'
import { addCartItem } from '@/api/cart'
import { useAuthStore } from '@/stores/auth'
import { formatPrice, getCategoryLabel, getDishId } from '@/utils/menu'

const router = useRouter()
const authStore = useAuthStore()

const recommending = ref(false)
const addingAll = ref(false)
const addingId = ref(null)
const query = ref('')
const items = ref([])
const hasSearched = ref(false)

const examples = [
  '我想吃点清淡的中餐',
  '想要辣的日料，口味重一点',
  '午餐来点低脂蛋白的轻食',
  '推荐几个适合健身的高蛋白低脂餐',
]

async function handleRecommend() {
  const prompt = query.value.trim()
  if (!prompt) {
    showToast('请输入想吃的描述')
    return
  }
  if (!authStore.isLoggedIn) {
    showToast('请先登录')
    router.push('/login')
    return
  }

  recommending.value = true
  hasSearched.value = true
  try {
    const data = await getRecommendations(prompt)
    items.value = Array.isArray(data) ? data : data?.list || []
    if (!items.value.length) {
      showToast('暂无推荐结果，请调整描述或偏好设置')
    }
  } catch (error) {
    items.value = []
    const message =
      error instanceof ApiError ? error.message : 'AI 推荐失败，请稍后重试'
    showToast(message)
  } finally {
    recommending.value = false
  }
}

function useExample(text) {
  query.value = text
}

async function handleAddOne(item) {
  const menuItemId = getDishId(item)
  if (menuItemId == null) {
    showToast('菜品无效')
    return
  }

  addingId.value = menuItemId
  try {
    await addCartItem({ menuItemId, quantity: 1 })
    showToast(`已加入：${item.name}`)
  } catch (error) {
    const message =
      error instanceof ApiError ? error.message : '加入购物车失败'
    showToast(message)
  } finally {
    addingId.value = null
  }
}

async function handleAddAll() {
  if (!items.value.length) return

  addingAll.value = true
  let ok = 0
  try {
    for (const item of items.value) {
      const menuItemId = getDishId(item)
      if (menuItemId == null) continue
      await addCartItem({ menuItemId, quantity: 1 })
      ok += 1
    }
    showToast(ok ? `已将 ${ok} 道菜加入购物车` : '加入失败')
  } catch (error) {
    const message =
      error instanceof ApiError ? error.message : '批量加入购物车失败'
    showToast(message)
  } finally {
    addingAll.value = false
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

function goBack() {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.replace('/menu')
  }
}
</script>

<template>
  <div class="ai-page">
    <van-nav-bar
      title="AI 智能推荐"
      left-arrow
      fixed
      placeholder
      @click-left="goBack"
    >
      <template #right>
        <van-icon name="shopping-cart-o" size="20" @click="goCart" />
      </template>
    </van-nav-bar>

    <div class="content">
      <van-notice-bar
        wrapable
        :scrollable="false"
        color="#1989fa"
        background="#ecf9ff"
        text="描述想吃的口味，后端将结合您的偏好设置自动排除过敏原，并推荐 3-5 道菜"
      />

      <div class="section">
        <h2 class="section-title">你想吃什么？</h2>
        <van-field
          v-model="query"
          rows="3"
          autosize
          type="textarea"
          maxlength="200"
          show-word-limit
          placeholder="例如：我想吃点清淡的中餐；或想要辣的日料，口味重一点"
        />
        <div class="examples">
          <span class="examples-label">试试：</span>
          <van-tag
            v-for="(text, idx) in examples"
            :key="idx"
            plain
            type="primary"
            class="example-tag"
            @click="useExample(text)"
          >
            {{ text }}
          </van-tag>
        </div>
        <van-button
          block
          round
          type="primary"
          :loading="recommending"
          loading-text="AI 分析中..."
          @click="handleRecommend"
        >
          开始推荐
        </van-button>
      </div>

      <template v-if="hasSearched">
        <van-empty
          v-if="!recommending && !items.length"
          description="暂无推荐结果，请调整描述或偏好设置"
        />

        <div v-else-if="items.length" class="result-list">
          <div class="result-head">
            <h2 class="section-title">推荐结果（{{ items.length }}）</h2>
            <van-button
              size="small"
              type="primary"
              round
              :loading="addingAll"
              loading-text="加入中"
              @click="handleAddAll"
            >
              一键加购物车
            </van-button>
          </div>

          <div
            v-for="(item, index) in items"
            :key="getDishId(item)"
            class="result-card"
          >
            <div class="card-main" @click="goDetail(item)">
              <div class="rank">{{ index + 1 }}</div>
              <div class="info">
                <div class="name-row">
                  <h3 class="name">{{ item.name }}</h3>
                  <span class="price">¥{{ formatPrice(item.price) }}</span>
                </div>
                <div class="tags">
                  <van-tag plain type="primary" size="medium">
                    {{ getCategoryLabel(item.category) }}
                  </van-tag>
                </div>
                <p v-if="item.reason" class="reason">{{ item.reason }}</p>
              </div>
            </div>
            <van-button
              size="small"
              type="primary"
              plain
              round
              :loading="addingId === getDishId(item)"
              loading-text="添加中"
              @click.stop="handleAddOne(item)"
            >
              加入购物车
            </van-button>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<style scoped lang="scss">
.ai-page {
  min-height: 100vh;
  background: #f7f8fa;
  padding-bottom: 24px;
}

.content {
  padding: 12px 0 0;
}

.section {
  margin: 12px;
  padding: 16px;
  background: #fff;
  border-radius: 12px;
}

.section-title {
  margin-bottom: 12px;
  font-size: 16px;
  font-weight: 600;
  color: #323233;
}

.examples {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin: 12px 0 16px;
  align-items: center;
}

.examples-label {
  font-size: 12px;
  color: #969799;
}

.example-tag {
  cursor: pointer;
}

.result-list {
  margin: 0 12px 12px;
}

.result-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
  padding: 0 4px;
}

.result-card {
  margin-bottom: 12px;
  padding: 14px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.card-main {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
  cursor: pointer;
}

.rank {
  flex-shrink: 0;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #1989fa;
  color: #fff;
  font-size: 14px;
  font-weight: 700;
  line-height: 28px;
  text-align: center;
}

.info {
  flex: 1;
  min-width: 0;
}

.name-row {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 8px;
}

.name {
  font-size: 16px;
  font-weight: 600;
  color: #323233;
}

.price {
  flex-shrink: 0;
  font-size: 15px;
  font-weight: 700;
  color: #ee0a24;
}

.tags {
  margin-top: 6px;
}

.reason {
  margin-top: 8px;
  font-size: 13px;
  line-height: 1.6;
  color: #646566;
}
</style>
