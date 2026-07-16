<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import 'vant/es/toast/style'
import { ApiError } from '@/api/http'
import { getMenuDetail } from '@/api/menu'
import {
  formatPrice,
  getAllergenLabel,
  getCategoryLabel,
} from '@/utils/menu'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const detail = ref(null)

async function fetchDetail() {
  loading.value = true
  try {
    const data = await getMenuDetail(route.params.id)
    detail.value = data || null
  } catch (error) {
    detail.value = null
    const message =
      error instanceof ApiError ? error.message : '加载详情失败'
    showToast(message)
  } finally {
    loading.value = false
  }
}

function goBack() {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.replace('/menu')
  }
}

onMounted(fetchDetail)
</script>

<template>
  <div class="detail-page">
    <van-nav-bar
      title="菜品详情"
      left-arrow
      fixed
      placeholder
      @click-left="goBack"
    />

    <van-loading v-if="loading" class="state" vertical>加载中...</van-loading>

    <van-empty
      v-else-if="!detail"
      description="未找到该菜品"
    >
      <van-button round type="primary" size="small" @click="goBack">
        返回菜单
      </van-button>
    </van-empty>

    <template v-else>
      <van-image
        class="hero"
        width="100%"
        height="220"
        fit="cover"
        :src="detail.image"
      >
        <template #error>
          <div class="img-fallback">暂无图片</div>
        </template>
      </van-image>

      <div class="panel">
        <div class="title-row">
          <h1 class="name">{{ detail.name }}</h1>
          <span class="price">¥{{ formatPrice(detail.price) }}</span>
        </div>

        <div class="meta">
          <van-tag plain type="primary">
            {{ getCategoryLabel(detail.category) }}
          </van-tag>
        </div>

        <van-cell-group inset class="block">
          <van-cell title="价格" :value="`¥${formatPrice(detail.price)}`" />
          <van-cell title="分类" :value="getCategoryLabel(detail.category)" />
        </van-cell-group>

        <div class="section">
          <h2 class="section-title">描述</h2>
          <p class="desc">
            {{ detail.description || '暂无描述' }}
          </p>
        </div>

        <div class="section">
          <h2 class="section-title">过敏原信息</h2>
          <div
            v-if="detail.allergens && detail.allergens.length"
            class="allergens"
          >
            <van-tag
              v-for="code in detail.allergens"
              :key="code"
              type="warning"
              plain
            >
              {{ getAllergenLabel(code) }}
            </van-tag>
          </div>
          <p v-else class="desc muted">未标注过敏原</p>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped lang="scss">
.detail-page {
  min-height: 100vh;
  background: #f7f8fa;
  padding-bottom: 32px;
}

.state {
  display: flex;
  justify-content: center;
  padding: 64px 0;
}

.hero {
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

.panel {
  margin-top: -12px;
  padding: 16px 12px 0;
  position: relative;
  z-index: 1;
}

.title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  padding: 16px;
  background: #fff;
  border-radius: 12px;
}

.name {
  flex: 1;
  font-size: 20px;
  font-weight: 700;
  color: #323233;
  line-height: 1.35;
}

.price {
  flex-shrink: 0;
  font-size: 20px;
  font-weight: 700;
  color: #ee0a24;
}

.meta {
  margin-top: 12px;
  padding: 0 4px;
}

.block {
  margin-top: 12px;
}

.section {
  margin-top: 12px;
  padding: 16px;
  background: #fff;
  border-radius: 12px;
}

.section-title {
  margin-bottom: 10px;
  font-size: 15px;
  font-weight: 600;
  color: #323233;
}

.desc {
  font-size: 14px;
  line-height: 1.7;
  color: #646566;
  white-space: pre-wrap;
}

.muted {
  color: #969799;
}

.allergens {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
</style>
