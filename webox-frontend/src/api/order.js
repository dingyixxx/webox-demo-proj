import http from './http'

/**
 * POST /api/orders
 * body: { deliveryDate, mealPeriod, deliveryAddress }
 */
export function createOrder({ deliveryDate, mealPeriod, deliveryAddress }) {
  return http.post('/api/orders', {
    deliveryDate,
    mealPeriod,
    deliveryAddress,
  })
}

/** GET /api/orders */
export function getOrders() {
  return http.get('/api/orders')
}

/** GET /api/orders/:id */
export function getOrderDetail(id) {
  return http.get(`/api/orders/${id}`)
}
