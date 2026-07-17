<script setup>
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import 'vant/es/toast/style'
import { ApiError } from '@/api/http'
import { getCart } from '@/api/cart'
import { getMenuList } from '@/api/menu'
import { createOrder, getOrders } from '@/api/order'
import { useAuthStore } from '@/stores/auth'
import { usePreferencesStore } from '@/stores/preferences'
import { formatPrice } from '@/utils/menu'
import {
  calcCartTotal,
  mergeCartWithMenu,
  normalizeCartItems,
} from '@/utils/cart'
import {
  formatDateYmd,
  formatDeliveryDate,
  getMealPeriodLabel,
  normalizeOrderList,
} from '@/utils/order'

const router = useRouter()
const authStore = useAuthStore()
const preferencesStore = usePreferencesStore()

const loading = ref(false)
const submitting = ref(false)
const items = ref([])
const showCalendar = ref(false)
const showDupDialog = ref(false)
const confirmCountdown = ref(0)

let countdownTimer = null

const form = reactive({
  deliveryDate: formatDateYmd(),
  mealPeriod: 'lunch',
  deliveryAddress: '',
})

const totalAmount = computed(() => calcCartTotal(items.value))
const confirmButtonText = computed(() =>
  confirmCountdown.value > 0
    ? `仍要下单（${confirmCountdown.value}s）`
    : '仍要下单',
)
const minDate = new Date()
const maxDate = (() => {
  const d = new Date()
  d.setDate(d.getDate() + 20)
  return d
})()

function clearCountdown() {
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
  confirmCountdown.value = 0
}

function startConfirmCountdown() {
  clearCountdown()
  confirmCountdown.value = 2
  countdownTimer = setInterval(() => {
    confirmCountdown.value -= 1
    if (confirmCountdown.value <= 0) {
      clearCountdown()
    }
  }, 1000)
}

async function fetchCartPreview() {
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
      error instanceof ApiError ? error.message : '加载订单预览失败'
    showToast(message)
  } finally {
    loading.value = false
  }
}

function onConfirmDate(value) {
  form.deliveryDate = formatDateYmd(value)
  showCalendar.value = false
}

/** 是否已有同日同餐次、未删除的订单（列表接口默认应已过滤删除单） */
async function hasSameSlotOrder() {
  const data = await getOrders()
  const orders = normalizeOrderList(data)
  return orders.some((order) => {
    if (order.status === 'cancelled') return false
    const date = formatDeliveryDate(order.deliveryDate)
    return (
      date === form.deliveryDate && order.mealPeriod === form.mealPeriod
    )
  })
}

function openDupDialog() {
  showDupDialog.value = true
  startConfirmCountdown()
}

function closeDupDialog() {
  showDupDialog.value = false
  clearCountdown()
}

async function submitOrder() {
  submitting.value = true
  try {
    const data = await createOrder({
      deliveryDate: form.deliveryDate,
      mealPeriod: form.mealPeriod,
      deliveryAddress: form.deliveryAddress.trim(),
    })
    const orderId = data?.id
    showToast('下单成功')
    await router.replace({
      path: '/order/success',
      query: orderId != null ? { id: String(orderId) } : {},
    })
  } catch (error) {
    const message =
      error instanceof ApiError ? error.message : '提交订单失败'
    showToast(message)
  } finally {
    submitting.value = false
  }
}

async function onSubmit() {
  if (!authStore.isLoggedIn) {
    showToast('请先登录')
    router.push('/login')
    return
  }
  if (!items.value.length) {
    showToast('购物车为空，无法下单')
    return
  }
  if (!form.deliveryDate) {
    showToast('请选择配送日期')
    return
  }
  if (!form.mealPeriod) {
    showToast('请选择餐次')
    return
  }
  if (!form.deliveryAddress.trim()) {
    showToast('请填写配送地址')
    return
  }

  submitting.value = true
  try {
    const duplicated = await hasSameSlotOrder()
    if (duplicated) {
      submitting.value = false
      openDupDialog()
      return
    }
    await submitOrder()
  } catch (error) {
    submitting.value = false
    const message =
      error instanceof ApiError ? error.message : '校验订单失败'
    showToast(message)
  }
}

async function onConfirmDupOrder() {
  if (confirmCountdown.value > 0 || submitting.value) return
  closeDupDialog()
  await submitOrder()
}

function goBack() {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.replace('/cart')
  }
}

function goLogin() {
  router.push('/login')
}

function goCart() {
  router.push('/cart')
}

onMounted(fetchCartPreview)
onUnmounted(clearCountdown)
</script>

<template>
  <div class="checkout-page">
    <van-nav-bar
      title="确认订单"
      left-arrow
      fixed
      placeholder
      @click-left="goBack"
    />

    <div class="content">
      <van-empty
        v-if="!authStore.isLoggedIn"
        description="登录后才能下单"
      >
        <van-button round type="primary" size="small" @click="goLogin">
          去登录
        </van-button>
      </van-empty>

      <template v-else>
        <van-loading v-if="loading" class="state" vertical>
          加载中...
        </van-loading>

        <van-empty
          v-else-if="!items.length"
          description="购物车是空的"
        >
          <van-button round type="primary" size="small" @click="goCart">
            返回购物车
          </van-button>
        </van-empty>

        <template v-else>
          <div class="section">
            <h2 class="section-title">菜品清单</h2>
            <div
              v-for="item in items"
              :key="item.id"
              class="line-item"
            >
              <div class="line-main">
                <span class="name">{{ item.name }}</span>
                <span class="qty">x{{ item.quantity }}</span>
              </div>
              <div class="line-price">
                ¥{{ formatPrice(item.lineAmount) }}
              </div>
            </div>
            <div class="total-row">
              <span>合计</span>
              <span class="total">¥{{ formatPrice(totalAmount) }}</span>
            </div>
          </div>

          <van-cell-group inset class="section-form">
            <van-field
              v-model="form.deliveryDate"
              is-link
              readonly
              label="配送日期"
              placeholder="选择日期"
              @click="showCalendar = true"
            />
            <van-field label="餐次" label-align="top">
              <template #input>
                <van-radio-group
                  v-model="form.mealPeriod"
                  direction="horizontal"
                >
                  <van-radio name="lunch">午餐 Lunch</van-radio>
                  <van-radio name="dinner">晚餐 Dinner</van-radio>
                </van-radio-group>
              </template>
            </van-field>
            <van-field
              v-model="form.deliveryAddress"
              rows="2"
              autosize
              type="textarea"
              label="配送地址"
              placeholder="请输入配送地址"
              maxlength="200"
              show-word-limit
            />
          </van-cell-group>
        </template>
      </template>
    </div>

    <van-submit-bar
      v-if="authStore.isLoggedIn && items.length"
      :price="totalAmount"
      :decimal-length="2"
      :loading="submitting"
      label="应付："
      button-text="提交订单"
      @submit="onSubmit"
    />

    <van-calendar
      v-model:show="showCalendar"
      :min-date="minDate"
      :max-date="maxDate"
      :default-date="new Date(form.deliveryDate)"
      @confirm="onConfirmDate"
    />

    <van-dialog
      v-model:show="showDupDialog"
      title="温馨提示"
      :show-confirm-button="false"
      :show-cancel-button="false"
      @closed="clearCountdown"
    >
      <div class="dup-message">
        您在
        <strong>{{ form.deliveryDate }}</strong>
        的
        <strong>{{ getMealPeriodLabel(form.mealPeriod) }}</strong>
        已有订单。这是软性提示，确认无误后可继续下单。
      </div>
      <div class="dup-actions">
        <van-button
          size="small"
          plain
          hairline
          @click="closeDupDialog"
        >
          取消
        </van-button>
        <van-button
          size="small"
          type="primary"
          :disabled="confirmCountdown > 0 || submitting"
          :loading="submitting"
          @click="onConfirmDupOrder"
        >
          {{ confirmButtonText }}
        </van-button>
      </div>
    </van-dialog>
  </div>
</template>

<style scoped lang="scss">
.checkout-page {
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

.section {
  margin-bottom: 12px;
  padding: 14px 16px;
  background: #fff;
  border-radius: 12px;
}

.section-title {
  margin-bottom: 12px;
  font-size: 15px;
  font-weight: 600;
  color: #323233;
}

.line-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 8px 0;
  border-bottom: 1px solid #f5f6f7;
}

.line-main {
  display: flex;
  align-items: baseline;
  gap: 8px;
  min-width: 0;
}

.name {
  font-size: 14px;
  color: #323233;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.qty {
  flex-shrink: 0;
  font-size: 13px;
  color: #969799;
}

.line-price {
  flex-shrink: 0;
  font-size: 14px;
  font-weight: 600;
  color: #323233;
}

.total-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 12px;
  font-size: 14px;
  color: #646566;
}

.total {
  font-size: 18px;
  font-weight: 700;
  color: #ee0a24;
}

.section-form {
  margin-top: 12px;
}

.dup-message {
  padding: 16px 24px 8px;
  font-size: 14px;
  line-height: 1.6;
  color: #646566;
}

.dup-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 8px 16px 16px;
}

:deep(.van-radio) {
  margin-right: 16px;
}
</style>
