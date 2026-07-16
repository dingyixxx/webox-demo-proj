import http from './http'

/** POST /api/auth/register */
export function register(params) {
  return http.post('/api/auth/register', params)
}

/** POST /api/auth/login */
export function login(params) {
  return http.post('/api/auth/login', params)
}

/** POST /api/auth/logout */
export function logout() {
  return http.post('/api/auth/logout')
}
