import http from './http'

/** GET /api/menu — 当日菜单列表，可选 ?category= */
export function getMenuList(params = {}) {
  return http.get('/api/menu', { params })
}

/** GET /api/menu/:id — 菜品详情 */
export function getMenuDetail(id) {
  return http.get(`/api/menu/${id}`)
}
