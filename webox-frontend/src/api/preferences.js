import http from './http'

/** GET /api/users/me/preferences */
export function getPreferences() {
  return http.get('/api/users/me/preferences')
}

/**
 * PUT /api/users/me/preferences
 * body 小驼峰，对齐用户表字段
 */
export function updatePreferences(payload) {
  return http.put('/api/users/me/preferences', payload)
}
