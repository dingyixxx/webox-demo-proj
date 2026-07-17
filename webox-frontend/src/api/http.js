import axios from 'axios'
import { showToast } from 'vant'
import 'vant/es/toast/style'

export class ApiError extends Error {
  constructor(code, message) {
    super(message)
    this.code = code
    this.name = 'ApiError'
  }
}

export const TOKEN_KEY = 'token'
export const EMAIL_KEY = 'email'

const http = axios.create({
  baseURL: '/',
  timeout: 15000,
})

let handling401 = false

/** token 失效：本地退出并跳转登录页 */
async function handleUnauthorized(message) {
  if (handling401) return
  handling401 = true

  try {
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(EMAIL_KEY)

    try {
      const { useAuthStore } = await import('@/stores/auth')
      useAuthStore().clearAuth()
    } catch {
      // pinia 尚未就绪时忽略
    }

    try {
      const { usePreferencesStore } = await import('@/stores/preferences')
      usePreferencesStore().clearPreferences()
    } catch {
      // ignore
    }

    showToast(message || '登录已过期，请重新登录')

    if (!window.location.pathname.startsWith('/login')) {
      window.location.replace('/login')
    }
  } finally {
    handling401 = false
  }
}

function isUnauthorized(code) {
  return Number(code) === 401
}

// 请求拦截器：自动从 localStorage 读取 token 并放入 headers
http.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem(TOKEN_KEY)
    if (token) {
      config.headers.token = token
    }
    return config
  },
  (error) => Promise.reject(error),
)

// 响应拦截器：统一处理业务错误码
http.interceptors.response.use(
  (response) => {
    const result = response.data
    if (result == null || typeof result !== 'object') {
      return result
    }

    // 兼容 Result { code, message, data }
    if ('code' in result) {
      if (isUnauthorized(result.code)) {
        handleUnauthorized(result.message)
        return Promise.reject(
          new ApiError(result.code, result.message || 'token已过期'),
        )
      }
      if (result.code !== 200) {
        return Promise.reject(
          new ApiError(result.code, result.message || '请求失败'),
        )
      }
      return result.data
    }

    return result
  },
  (error) => {
    const payload = error.response?.data
    const message =
      payload?.message || error.message || '网络异常，请稍后重试'
    const code = payload?.code || error.response?.status || 500

    if (isUnauthorized(code) || error.response?.status === 401) {
      handleUnauthorized(message)
    }

    return Promise.reject(new ApiError(code, message))
  },
)

export default http
