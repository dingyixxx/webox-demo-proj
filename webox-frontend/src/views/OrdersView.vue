<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import 'vant/es/toast/style'
import { ApiError } from '@/api/http'
import { getOrders } from '@/api/order'
import { useAuthStore } from '@/stores/auth'
import { formatPrice } from '@/utils/menu'
import {
  formatDeliveryDate,
  getMealPeriodLabel,
  getOrderId,
  getStatusLabel,
  getStatusType,
  normalizeOrderList,
} from '@/utils/order'

const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)
const orders = ref([])

async function fetchOrders() {
  if (!authStore.isLoggedIn) {
    orders.value = []
    return
  }

  loading.value = true
  try {
    const data = await getOrders()
    orders.value = normalizeOrderList(data)
  } catch (error) {
    orders.value = []
    const message =
      error instanceof ApiError ? error.message : '加载订单失败'
    showToast(message)
  } finally {
    loading.value = false
  }
}

function goDetail(order) {
  const id = getOrderId(order)
  if (id == null) return
  router.push(`/orders/${id}`)
}

function goBack() {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.replace('/menu')
  }
}

function goLogin() {
  router.push('/login')
}

function goMenu() {
  router.push('/menu')
}

onMounted(fetchOrders)
</script>

<template>
  <div class="orders-page">
    <van-nav-bar
      title="我的订单"
      left-arrow
      fixed
      placeholder
      @click-left="goBack"
    />

    <div class="content">
      <van-empty
        v-if="!authStore.isLoggedIn"
        description="登录后查看历史订单"
      >
        <van-button round type="primary" size="small" @click="goLogin">
          去登录
        </van-button>
      </van-empty>

      <van-loading v-else-if="loading" class="state" vertical>
        加载中...
      </van-loading>

      <van-empty
        v-else-if="!orders.length"
        description="暂无订单记录"
      >
        <van-button round type="primary" size="small" @click="goMenu">
          去点餐
        </van-button>
      </van-empty>

      <div v-else class="list">
        <div
          v-for="order in orders"
          :key="getOrderId(order)"
          class="order-card"
          @click="goDetail(order)"
        >
          <div class="card-head">
            <span class="order-id">订单 #{{ getOrderId(order) }}</span>
            <van-tag :type="getStatusType(order.status)" plain>
              {{ getStatusLabel(order.status) }}
            </van-tag>
          </div>
          <div class="card-row">
            <span>配送日期</span>
            <span>{{ formatDeliveryDate(order.deliveryDate) }}</span>
          </div>
          <div class="card-row">
            <span>餐次</span>
            <span>{{ getMealPeriodLabel(order.mealPeriod) }}</span>
          </div>
          <div class="card-foot">
            <span class="amount">
              ¥{{ formatPrice(order.totalAmount) }}
            </span>
            <van-icon name="arrow" color="#969799" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.orders-page {
  min-height: 100vh;
  background: #f7f8fa;
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

.order-card {
  padding: 14px 16px;
  background: #fff;
  border-radius: 12px;
}

.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.order-id {
  font-size: 15px;
  font-weight: 600;
  color: #323233;
}

.card-row {
  display: flex;
  justify-content: space-between;
  margin-top: 6px;
  font-size: 13px;
  color: #646566;
}

.card-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 12px;
  padding-top: 10px;
  border-top: 1px solid #f5f6f7;
}

.amount {
  font-size: 16px;
  font-weight: 700;
  color: #ee0a24;
}
</style>
