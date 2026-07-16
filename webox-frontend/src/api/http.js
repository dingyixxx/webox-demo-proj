import axios from 'axios'

export class ApiError extends Error {
  constructor(code, message) {
    super(message)
    this.code = code
    this.name = 'ApiError'
  }
}

export const TOKEN_KEY = 'token'

const http = axios.create({
  baseURL: '/',
  timeout: 15000,
})

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
    const message =
      error.response?.data?.message ||
      error.message ||
      '网络异常，请稍后重试'
    const code = error.response?.data?.code || error.response?.status || 500
    return Promise.reject(new ApiError(code, message))
  },
)

export default http
