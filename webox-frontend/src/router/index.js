import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/menu',
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/RegisterView.vue'),
      meta: { title: '注册' },
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue'),
      meta: { title: '登录' },
    },
    {
      path: '/menu',
      name: 'menu',
      component: () => import('@/views/MenuView.vue'),
      meta: { title: '菜单' },
    },
    {
      path: '/menu/:id',
      name: 'menu-detail',
      component: () => import('@/views/MenuDetailView.vue'),
      meta: { title: '菜品详情' },
    },
    {
      path: '/cart',
      name: 'cart',
      component: () => import('@/views/CartView.vue'),
      meta: { title: '购物车' },
    },
    {
      path: '/checkout',
      name: 'checkout',
      component: () => import('@/views/CheckoutView.vue'),
      meta: { title: '确认订单' },
    },
    {
      path: '/order/success',
      name: 'order-success',
      component: () => import('@/views/OrderSuccessView.vue'),
      meta: { title: '下单成功' },
    },
    {
      path: '/orders',
      name: 'orders',
      component: () => import('@/views/OrdersView.vue'),
      meta: { title: '我的订单' },
    },
    {
      path: '/orders/:id',
      name: 'order-detail',
      component: () => import('@/views/OrderDetailView.vue'),
      meta: { title: '订单详情' },
    },
    {
      path: '/preferences',
      name: 'preferences',
      component: () => import('@/views/PreferencesView.vue'),
      meta: { title: '偏好设置' },
    },
  ],
})

router.afterEach((to) => {
  const title = to.meta.title || 'Webox'
  document.title = title
})

export default router
