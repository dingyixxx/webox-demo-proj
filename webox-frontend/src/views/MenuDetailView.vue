<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { showToast } from 'vant'
import 'vant/es/toast/style'
import { ApiError } from '@/api/http'
import { getMenuDetail } from '@/api/menu'
import { useAuthStore } from '@/stores/auth'
import { usePreferencesStore } from '@/stores/preferences'
import {
  formatPrice,
  getAllergenLabel,
  getCategoryLabel,
} from '@/utils/menu'
import {
  getMatchedUserAllergens,
  hasAllergenConflict,
  isDishAllergenMatched,
} from '@/utils/preferences'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const preferencesStore = usePreferencesStore()
const { preferences } = storeToRefs(preferencesStore)
const loading = ref(false)
const detail = ref(null)

const allergenRisk = computed(() =>
  hasAllergenConflict(detail.value?.allergens, preferences.value.allergens),
)

const matchedCodes = computed(() =>
  getMatchedUserAllergens(
    detail.value?.allergens,
    preferences.value.allergens,
  ),
)

const matchedAllergenLabels = computed(() =>
  matchedCodes.value.map(getAllergenLabel),
)

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

async function ensurePreferences() {
  if (!authStore.isLoggedIn) return
  try {
    await preferencesStore.fetchPreferences()
  } catch {
    // ignore
  }
}

function goBack() {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.replace('/menu')
  }
}

onMounted(async () => {
  await ensurePreferences()
  await fetchDetail()
})
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
      <div class="hero-wrap">
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
      </div>

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

        <div
          class="section allergen-section"
          :class="{ 'allergen-section--risk': allergenRisk }"
        >
          <h2 class="section-title">过敏原信息</h2>
          <template v-if="detail.allergens && detail.allergens.length">
            <p v-if="allergenRisk" class="allergen-tip">
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
              含你设置的过敏原：{{ matchedAllergenLabels.join('、') }}，请谨慎选择。红色标签为与你偏好匹配的项目。
            </p>
            <p v-else class="allergen-tip allergen-tip--muted">
              以下为菜品标注的过敏原；若与你的偏好重合会以红色标出。
            </p>
            <div class="allergens">
              <span
                v-for="code in detail.allergens"
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
          </template>
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

.hero-wrap {
  position: relative;
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
  margin-top: 12px;
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
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
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

.allergen-section--risk {
  background: #fff1f0;
}

.allergen-tip {
  margin: 0 0 10px;
  font-size: 13px;
  line-height: 1.6;
  color: #ee0a24;
  font-weight: 500;
}

.allergen-tip-icon {
  display: inline-block;
  width: 15px;
  height: 15px;
  margin-right: 4px;
  vertical-align: -2px;
  flex-shrink: 0;
}

.allergen-tip--muted {
  color: #969799;
  font-weight: 400;
}

.allergens {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.allergen-chip {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 13px;
  line-height: 1.4;
  color: #646566;
  background: #f2f3f5;
}

.allergen-section--risk .allergen-chip {
  background: #fff;
}

.allergen-section--risk .allergen-chip--match,
.allergen-chip--match {
  color: #fff;
  background: #ff1744;
  font-weight: 700;
}
</style>
