<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const orderId = computed(() => route.query.id || '')

function goOrders() {
  router.replace('/orders')
}

function goOrderDetail() {
  if (orderId.value) {
    router.replace(`/orders/${orderId.value}`)
  } else {
    goOrders()
  }
}

function goMenu() {
  router.replace('/menu')
}
</script>

<template>
  <div class="success-page">
    <van-nav-bar title="下单成功" fixed placeholder />

    <div class="content">
      <van-icon name="checked" class="icon" color="#07c160" />
      <h1 class="title">订单提交成功</h1>
      <p class="desc">
        {{ orderId ? `订单号：${orderId}` : '我们已收到你的订单' }}
      </p>

      <div class="actions">
        <van-button
          v-if="orderId"
          round
          block
          type="primary"
          @click="goOrderDetail"
        >
          查看订单详情
        </van-button>
        <van-button round block type="primary" plain hairline @click="goOrders">
          我的订单
        </van-button>
        <van-button round block plain hairline @click="goMenu">
          返回菜单
        </van-button>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.success-page {
  min-height: 100vh;
  background: #f7f8fa;
}

.content {
  padding: 48px 24px 24px;
  text-align: center;
}

.icon {
  font-size: 64px;
}

.title {
  margin-top: 16px;
  font-size: 22px;
  font-weight: 700;
  color: #323233;
}

.desc {
  margin-top: 8px;
  font-size: 14px;
  color: #969799;
}

.actions {
  margin-top: 32px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
</style>
