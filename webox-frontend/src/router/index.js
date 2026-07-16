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
  ],
})

router.afterEach((to) => {
  const title = to.meta.title || 'Webox'
  document.title = title
})

export default router
