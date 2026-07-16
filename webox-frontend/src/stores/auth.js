import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import {
  login as loginApi,
  logout as logoutApi,
  register as registerApi,
} from '@/api/auth'
import { TOKEN_KEY, EMAIL_KEY } from '@/api/http'

export { EMAIL_KEY }

/** 从登录/注册响应中取出 token（兼容 data 为字符串或对象） */
function extractToken(payload) {
  if (!payload) return ''
  if (typeof payload === 'string') return payload
  return payload.token || ''
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem(TOKEN_KEY) || '')
  const email = ref(localStorage.getItem(EMAIL_KEY) || '')

  const isLoggedIn = computed(() => Boolean(token.value))

  function setToken(nextToken) {
    token.value = nextToken || ''
    if (token.value) {
      localStorage.setItem(TOKEN_KEY, token.value)
    } else {
      localStorage.removeItem(TOKEN_KEY)
    }
  }

  function setEmail(nextEmail) {
    email.value = nextEmail || ''
    if (email.value) {
      localStorage.setItem(EMAIL_KEY, email.value)
    } else {
      localStorage.removeItem(EMAIL_KEY)
    }
  }

  function clearAuth() {
    setToken('')
    setEmail('')
  }

  async function register(params) {
    const data = await registerApi(params)
    const nextToken = extractToken(data)
    if (nextToken) {
      setToken(nextToken)
    }
    if (params?.email) {
      setEmail(params.email.trim())
    }
    return data
  }

  async function login(params) {
    const data = await loginApi(params)
    const nextToken = extractToken(data)
    if (nextToken) {
      setToken(nextToken)
    }
    // 邮箱作为用户唯一识别信息，登录成功后写入 localStorage
    if (params?.email) {
      setEmail(params.email.trim())
    }
    return data
  }

  async function logout() {
    try {
      await logoutApi()
    } finally {
      clearAuth()
    }
  }

  return {
    token,
    email,
    isLoggedIn,
    setToken,
    setEmail,
    clearAuth,
    register,
    login,
    logout,
  }
})
