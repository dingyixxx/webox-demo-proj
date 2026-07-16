<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import 'vant/es/toast/style'
import { ApiError } from '@/api/http'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)

const form = reactive({
  email: '',
  password: '',
})

const emailRules = [
  { required: true, message: '请输入邮箱' },
  {
    pattern: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
    message: '请输入有效的邮箱地址',
  },
]

const passwordRules = [
  { required: true, message: '请输入密码' },
  {
    validator: (val) => val.length >= 6,
    message: '密码至少 6 位',
  },
]

async function onSubmit() {
  if (loading.value) return
  loading.value = true
  try {
    await authStore.register({
      email: form.email.trim(),
      password: form.password,
    })
    showToast('注册成功')
    await router.replace('/menu')
  } catch (error) {
    const message =
      error instanceof ApiError
        ? error.message
        : '注册失败，请稍后重试'
    showToast(message)
  } finally {
    loading.value = false
  }
}

function goLogin() {
  router.push('/login')
}
</script>

<template>
  <div class="auth-page">
    <van-nav-bar title="注册" fixed placeholder />

    <div class="auth-hero">
      <h1 class="brand">Webox</h1>
      <p class="subtitle">使用邮箱注册账号</p>
    </div>

    <van-form @submit="onSubmit">
      <van-cell-group inset>
        <van-field v-model="form.email" name="email" type="email" label="邮箱" placeholder="请输入邮箱" :rules="emailRules" />
        <van-field v-model="form.password" name="password" type="password" label="密码" placeholder="至少 6 位密码"
          :rules="passwordRules" />
      </van-cell-group>

      <div class="actions">
        <van-button round block type="primary" native-type="submit" :loading="loading" loading-text="注册中...">
          注册
        </van-button>
        <van-button round block plain hairline type="primary" class="secondary" @click="goLogin">
          已有账号？去登录
        </van-button>
      </div>
    </van-form>
  </div>
</template>

<style scoped lang="scss">
.auth-page {
  min-height: 100vh;
  background: #f7f8fa;
}

.auth-hero {
  padding: 32px 24px 12px;
  text-align: center;
}

.brand {
  font-size: 28px;
  font-weight: 700;
  color: #1989fa;
  letter-spacing: 0.04em;
}

.subtitle {
  margin-top: 8px;
  font-size: 14px;
  color: #969799;
}

.actions {
  margin: 24px 16px 0;
}

.secondary {
  margin-top: 12px;
}
</style>
