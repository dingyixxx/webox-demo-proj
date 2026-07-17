<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import 'vant/es/toast/style'
import { ApiError } from '@/api/http'
import { useAuthStore } from '@/stores/auth'
import { usePreferencesStore } from '@/stores/preferences'
import {
  ALLERGEN_OPTIONS,
  CUISINE_OPTIONS,
  SPICINESS_OPTIONS,
  TASTE_OPTIONS,
  createEmptyPreferences,
  priceToYuan,
  yuanToPrice,
} from '@/utils/preferences'

const router = useRouter()
const authStore = useAuthStore()
const preferencesStore = usePreferencesStore()

const loading = ref(false)
const saving = ref(false)

/** 表单：价格用「元」编辑，提交时再转成元×100 */
const form = reactive({
  ...createEmptyPreferences(),
  priceMinYuan: '',
  priceMaxYuan: '',
})

function applyToForm(prefs) {
  form.allergens = [...(prefs.allergens || [])]
  form.cuisinePreferences = [...(prefs.cuisinePreferences || [])]
  form.spicinessLevel = prefs.spicinessLevel || 0
  form.tasteLevel = prefs.tasteLevel || 0
  form.proteinLevel = prefs.proteinLevel || 0
  form.fatLevel = prefs.fatLevel || 0
  form.preferredMinPrice = prefs.preferredMinPrice
  form.preferredMaxPrice = prefs.preferredMaxPrice
  form.priceMinYuan = priceToYuan(prefs.preferredMinPrice)
  form.priceMaxYuan = priceToYuan(prefs.preferredMaxPrice)
}

async function loadPreferences() {
  if (!authStore.isLoggedIn) return
  loading.value = true
  try {
    const prefs = await preferencesStore.fetchPreferences()
    applyToForm(prefs)
  } catch (error) {
    const message =
      error instanceof ApiError ? error.message : '加载偏好失败'
    showToast(message)
  } finally {
    loading.value = false
  }
}

function validateAndBuildPayload() {
  const minYuan =
    form.priceMinYuan === '' || form.priceMinYuan == null
      ? null
      : Number(form.priceMinYuan)
  const maxYuan =
    form.priceMaxYuan === '' || form.priceMaxYuan == null
      ? null
      : Number(form.priceMaxYuan)

  if (minYuan != null && (Number.isNaN(minYuan) || minYuan < 0)) {
    showToast('最低价格不合法')
    return null
  }
  if (maxYuan != null && (Number.isNaN(maxYuan) || maxYuan < 0)) {
    showToast('最高价格不合法')
    return null
  }
  if (minYuan != null && maxYuan != null && minYuan > maxYuan) {
    showToast('最低价不能高于最高价')
    return null
  }

  return {
    allergens: [...form.allergens],
    cuisinePreferences: [...form.cuisinePreferences],
    spicinessLevel: form.spicinessLevel || 0,
    tasteLevel: form.tasteLevel || 0,
    proteinLevel: form.proteinLevel || 0,
    fatLevel: form.fatLevel || 0,
    preferredMinPrice: yuanToPrice(minYuan),
    preferredMaxPrice: yuanToPrice(maxYuan),
  }
}

async function onSave() {
  if (!authStore.isLoggedIn) {
    showToast('请先登录')
    router.push('/login')
    return
  }

  const payload = validateAndBuildPayload()
  if (!payload) return

  saving.value = true
  try {
    const saved = await preferencesStore.savePreferences(payload)
    applyToForm(saved)
    showToast('偏好已保存')
  } catch (error) {
    const message =
      error instanceof ApiError ? error.message : '保存失败'
    showToast(message)
  } finally {
    saving.value = false
  }
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

onMounted(loadPreferences)
</script>

<template>
  <div class="prefs-page">
    <van-nav-bar
      title="偏好设置"
      left-arrow
      fixed
      placeholder
      @click-left="goBack"
    />

    <div class="content">
      <van-empty
        v-if="!authStore.isLoggedIn"
        description="登录后设置饮食偏好"
      >
        <van-button round type="primary" size="small" @click="goLogin">
          去登录
        </van-button>
      </van-empty>

      <van-loading v-else-if="loading" class="state" vertical>
        加载中...
      </van-loading>

      <template v-else>
        <div class="section">
          <h2 class="section-title">过敏原设置</h2>
          <p class="hint">多选；菜单中含对应过敏原的菜品会标警告</p>
          <van-checkbox-group v-model="form.allergens" direction="horizontal">
            <van-checkbox
              v-for="opt in ALLERGEN_OPTIONS"
              :key="opt.value"
              :name="opt.value"
              shape="square"
            >
              {{ opt.text }}
            </van-checkbox>
          </van-checkbox-group>
        </div>

        <div class="section">
          <h2 class="section-title">菜系偏好</h2>
          <p class="hint">多选；推荐模式下优先展示这些菜系</p>
          <van-checkbox-group
            v-model="form.cuisinePreferences"
            direction="horizontal"
          >
            <van-checkbox
              v-for="opt in CUISINE_OPTIONS"
              :key="opt.value"
              :name="opt.value"
              shape="square"
            >
              {{ opt.text }}
            </van-checkbox>
          </van-checkbox-group>
        </div>

        <div class="section">
          <h2 class="section-title">口味偏好</h2>
          <div class="sub-block">
            <div class="sub-label">辣度</div>
            <van-radio-group
              v-model="form.spicinessLevel"
              direction="horizontal"
            >
              <van-radio
                v-for="opt in SPICINESS_OPTIONS"
                :key="opt.value"
                :name="opt.value"
              >
                {{ opt.text }}
              </van-radio>
            </van-radio-group>
          </div>
          <div class="sub-block">
            <div class="sub-label">口味轻重</div>
            <van-radio-group
              v-model="form.tasteLevel"
              direction="horizontal"
            >
              <van-radio
                v-for="opt in TASTE_OPTIONS"
                :key="opt.value"
                :name="opt.value"
              >
                {{ opt.text }}
              </van-radio>
            </van-radio-group>
          </div>
        </div>

        <div class="section">
          <h2 class="section-title">价格区间</h2>
          <p class="hint">单餐预算（元），保存时按「元×100」提交给后端</p>
          <div class="price-row">
            <van-field
              v-model="form.priceMinYuan"
              type="number"
              placeholder="最低"
              input-align="center"
            />
            <span class="price-sep">—</span>
            <van-field
              v-model="form.priceMaxYuan"
              type="number"
              placeholder="最高"
              input-align="center"
            />
          </div>
        </div>

        <div class="actions">
          <van-button
            round
            block
            type="primary"
            :loading="saving"
            loading-text="保存中..."
            @click="onSave"
          >
            保存偏好
          </van-button>
        </div>
      </template>
    </div>
  </div>
</template>

<style scoped lang="scss">
.prefs-page {
  min-height: 100vh;
  background: #f7f8fa;
  padding-bottom: 24px;
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
  font-size: 15px;
  font-weight: 600;
  color: #323233;
}

.hint {
  margin: 6px 0 12px;
  font-size: 12px;
  color: #969799;
  line-height: 1.5;
}

.sub-block {
  margin-top: 12px;
}

.sub-label {
  margin-bottom: 8px;
  font-size: 13px;
  color: #646566;
}

.price-row {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #f7f8fa;
  border-radius: 8px;
  overflow: hidden;
}

.price-sep {
  flex-shrink: 0;
  color: #969799;
}

.actions {
  margin-top: 20px;
}

:deep(.van-checkbox) {
  margin: 0 12px 10px 0;
}

:deep(.van-radio) {
  margin-right: 16px;
}

:deep(.price-row .van-cell) {
  background: transparent;
  padding: 8px 12px;
}
</style>
