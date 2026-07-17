import http from './http'

/** GET /api/cart */
export function getCart() {
  return http.get('/api/cart')
}

/** POST /api/cart/items  body: { menuItemId, quantity } */
export function addCartItem({ menuItemId, quantity = 1 }) {
  return http.post('/api/cart/items', {
    menuItemId,
    quantity,
  })
}

/** PUT /api/cart/items/:id */
export function updateCartItem(id, quantity) {
  return http.put(`/api/cart/items/${id}`, { quantity })
}

/** DELETE /api/cart/items/:id */
export function deleteCartItem(id) {
  return http.delete(`/api/cart/items/${id}`)
}
