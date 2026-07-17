<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showConfirmDialog, showToast } from 'vant'
import 'vant/es/toast/style'
import 'vant/es/dialog/style'
import { ApiError } from '@/api/http'
import { getCart, updateCartItem, deleteCartItem } from '@/api/cart'
import { getMenuList } from '@/api/menu'
import { formatPrice } from '@/utils/menu'
import {
  calcCartTotal,
  mergeCartWithMenu,
  normalizeCartItems,
} from '@/utils/cart'
import { useAuthStore } from '@/stores/auth'
import { usePreferencesStore } from '@/stores/preferences'

const router = useRouter()
const authStore = useAuthStore()
const preferencesStore = usePreferencesStore()

const loading = ref(false)
const items = ref([])
const busyId = ref(null)

const totalAmount = computed(() => calcCartTotal(items.value))

async function fetchCart() {
  if (!authStore.isLoggedIn) {
    items.value = []
    return
  }

  loading.value = true
  try {
    await preferencesStore.fetchPreferences().catch(() => {})
    const [cartData, menuData] = await Promise.all([
      getCart(),
      getMenuList(),
    ])
    const cartItems = normalizeCartItems(cartData)
    const menuList = Array.isArray(menuData) ? menuData : menuData?.list || []
    items.value = mergeCartWithMenu(
      cartItems,
      menuList,
      preferencesStore.preferences,
    )
  } catch (error) {
    items.value = []
    const message =
      error instanceof ApiError ? error.message : '加载购物车失败'
    showToast(message)
  } finally {
    loading.value = false
  }
}

async function onQuantityChange(item, quantity) {
  const next = Math.max(1, Number(quantity) || 1)
  if (next === item.quantity) return
  if (!item.id) {
    showToast('购物车项无效')
    return
  }

  const prev = item.quantity
  item.quantity = next
  item.lineAmount = (Number(item.price) || 0) * next

  busyId.value = item.id
  try {
    await updateCartItem(item.id, next)
  } catch (error) {
    item.quantity = prev
    item.lineAmount = (Number(item.price) || 0) * prev
    const message =
      error instanceof ApiError ? error.message : '更新数量失败'
    showToast(message)
  } finally {
    busyId.value = null
  }
}

async function onDelete(item) {
  if (!item.id) return
  try {
    await showConfirmDialog({
      title: '移除菜品',
      message: `确定将「${item.name}」移出购物车？`,
    })
  } catch {
    return
  }

  try {
    await deleteCartItem(item.id)
    items.value = items.value.filter((row) => row.id !== item.id)
    showToast('已移除')
  } catch (error) {
    const message =
      error instanceof ApiError ? error.message : '删除失败'
    showToast(message)
  }
}

function goBack() {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.replace('/menu')
  }
}

function goMenu() {
  router.push('/menu')
}

function goLogin() {
  router.push('/login')
}

onMounted(fetchCart)
</script>

<template>
  <div class="cart-page">
    <van-nav-bar
      title="购物车"
      left-arrow
      fixed
      placeholder
      @click-left="goBack"
    />

    <div class="content">
      <van-empty
        v-if="!authStore.isLoggedIn"
        description="登录后查看购物车"
      >
        <van-button round type="primary" size="small" @click="goLogin">
          去登录
        </van-button>
      </van-empty>

      <van-loading
        v-else-if="loading"
        class="state"
        vertical
      >
        加载中...
      </van-loading>

      <van-empty
        v-else-if="!items.length"
        description="购物车是空的"
      >
        <van-button round type="primary" size="small" @click="goMenu">
          去选菜
        </van-button>
      </van-empty>

      <div v-else class="list">
        <van-swipe-cell v-for="item in items" :key="item.id">
          <div class="cart-item">
            <van-image
              class="thumb"
              width="72"
              height="72"
              radius="8"
              fit="cover"
              :src="item.image"
            >
              <template #error>
                <div class="img-fallback">无图</div>
              </template>
            </van-image>

            <div class="meta">
              <div class="name">{{ item.name }}</div>
              <div class="price">¥{{ formatPrice(item.price) }}</div>
              <van-stepper
                :model-value="item.quantity"
                integer
                :min="1"
                :disabled="busyId === item.id"
                @change="(val) => onQuantityChange(item, val)"
              />
            </div>

            <div class="line-total">
              ¥{{ formatPrice(item.lineAmount) }}
            </div>
          </div>

          <template #right>
            <van-button
              square
              type="danger"
              class="delete-btn"
              text="删除"
              @click="onDelete(item)"
            />
          </template>
        </van-swipe-cell>
      </div>
    </div>

    <van-submit-bar
      v-if="authStore.isLoggedIn && items.length"
      :price="totalAmount"
      :decimal-length="2"
      label="合计："
      button-text="去结算"
      tip="左右滑动可删除菜品"
      @submit="router.push('/checkout')"
    />
  </div>
</template>

<style scoped lang="scss">
.cart-page {
  min-height: 100vh;
  background: #f7f8fa;
  padding-bottom: 60px;
}

.content {
  padding: 12px;
}

.state {
  display: flex;
  justify-content: center;
  padding: 48px 0;
}

.list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.cart-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #fff;
  border-radius: 12px;
}

.thumb {
  flex-shrink: 0;
  background: #f2f3f5;
}

.img-fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  color: #969799;
  font-size: 12px;
  background: #f2f3f5;
}

.meta {
  flex: 1;
  min-width: 0;
}

.name {
  font-size: 15px;
  font-weight: 600;
  color: #323233;
  line-height: 1.3;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.price {
  margin: 4px 0 8px;
  font-size: 14px;
  color: #ee0a24;
  font-weight: 600;
}

.line-total {
  flex-shrink: 0;
  align-self: flex-start;
  margin-top: 2px;
  font-size: 14px;
  font-weight: 700;
  color: #323233;
}

.delete-btn {
  height: 100%;
}
</style>
