<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import 'vant/es/toast/style'
import { ApiError } from '@/api/http'
import { getOrderDetail } from '@/api/order'
import { useAuthStore } from '@/stores/auth'
import { formatPrice } from '@/utils/menu'
import {
  formatDeliveryDate,
  getItemSubtotal,
  getMealPeriodLabel,
  getOrderId,
  getStatusLabel,
  getStatusType,
  normalizeOrderItems,
} from '@/utils/order'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const order = ref(null)

const items = computed(() => normalizeOrderItems(order.value))

async function fetchDetail() {
  if (!authStore.isLoggedIn) {
    order.value = null
    return
  }

  loading.value = true
  try {
    order.value = await getOrderDetail(route.params.id)
  } catch (error) {
    order.value = null
    const message =
      error instanceof ApiError ? error.message : '加载订单详情失败'
    showToast(message)
  } finally {
    loading.value = false
  }
}

function goBack() {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.replace('/orders')
  }
}

function goLogin() {
  router.push('/login')
}

function goOrders() {
  router.replace('/orders')
}

onMounted(fetchDetail)
watch(() => route.params.id, fetchDetail)
</script>

<template>
  <div class="detail-page">
    <van-nav-bar
      title="订单详情"
      left-arrow
      fixed
      placeholder
      @click-left="goBack"
    />

    <div class="content">
      <van-empty
        v-if="!authStore.isLoggedIn"
        description="登录后查看订单详情"
      >
        <van-button round type="primary" size="small" @click="goLogin">
          去登录
        </van-button>
      </van-empty>

      <van-loading v-else-if="loading" class="state" vertical>
        加载中...
      </van-loading>

      <van-empty
        v-else-if="!order"
        description="未找到该订单"
      >
        <van-button round type="primary" size="small" @click="goOrders">
          返回订单列表
        </van-button>
      </van-empty>

      <template v-else>
        <div class="section">
          <div class="head">
            <span class="order-id">订单 #{{ getOrderId(order) }}</span>
            <van-tag :type="getStatusType(order.status)" plain>
              {{ getStatusLabel(order.status) }}
            </van-tag>
          </div>
          <van-cell-group :border="false">
            <van-cell
              title="配送日期"
              :value="formatDeliveryDate(order.deliveryDate)"
            />
            <van-cell
              title="餐次"
              :value="getMealPeriodLabel(order.mealPeriod)"
            />
            <van-cell
              title="配送地址"
              :label="order.deliveryAddress || '—'"
            />
            <van-cell
              title="订单总额"
              :value="`¥${formatPrice(order.totalAmount)}`"
            />
          </van-cell-group>
        </div>

        <div class="section">
          <h2 class="section-title">菜品明细</h2>
          <van-empty
            v-if="!items.length"
            image="search"
            description="暂无明细"
          />
          <div
            v-for="(item, index) in items"
            :key="item.id || index"
            class="line-item"
          >
            <div class="line-main">
              <span class="name">{{ item.name }}</span>
              <span class="qty">x{{ item.quantity }}</span>
            </div>
            <div class="line-price">
              ¥{{ formatPrice(getItemSubtotal(item)) }}
            </div>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<style scoped lang="scss">
.detail-page {
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

.section {
  margin-bottom: 12px;
  padding: 14px 4px 8px;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
}

.head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px 8px;
}

.order-id {
  font-size: 16px;
  font-weight: 700;
  color: #323233;
}

.section-title {
  padding: 0 16px 8px;
  font-size: 15px;
  font-weight: 600;
  color: #323233;
}

.line-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 16px;
  border-top: 1px solid #f5f6f7;
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
</style>
