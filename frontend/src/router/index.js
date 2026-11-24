import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('@/views/Home.vue'),
      meta: { title: '首页' },
    },
    {
      path: '/movies/:id',
      name: 'movie-detail',
      component: () => import('@/views/MovieDetail.vue'),
      meta: { title: '影片详情' },
    },
    {
      path: '/showtimes/:id/seats',
      name: 'seat-select',
      component: () => import('@/views/SeatSelect.vue'),
      meta: { title: '选座购票', requiresAuth: true },
    },
    {
      path: '/orders',
      name: 'orders',
      component: () => import('@/views/MyOrders.vue'),
      meta: { title: '我的票夹', requiresAuth: true },
    },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('@/views/Profile.vue'),
      meta: { title: '个人中心', requiresAuth: true },
    },
    {
      path: '/orders/:id',
      name: 'order-detail',
      component: () => import('@/views/OrderDetail.vue'),
      meta: { title: '订单详情', requiresAuth: true },
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/Login.vue'),
      meta: { title: '登录', guestOnly: true },
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/Register.vue'),
      meta: { title: '注册', guestOnly: true },
    },
    {
      path: '/admin',
      component: () => import('@/layouts/AdminLayout.vue'),
      meta: { requiresAdmin: true },
      children: [
        {
          path: '',
          name: 'admin-dashboard',
          component: () => import('@/views/admin/Dashboard.vue'),
          meta: { title: '票房统计' },
        },
        {
          path: 'movies',
          name: 'admin-movies',
          component: () => import('@/views/admin/MovieAdmin.vue'),
          meta: { title: '影片管理' },
        },
        {
          path: 'cinemas',
          name: 'admin-cinemas',
          component: () => import('@/views/admin/CinemaAdmin.vue'),
          meta: { title: '影院影厅' },
        },
        {
          path: 'showtimes',
          name: 'admin-showtimes',
          component: () => import('@/views/admin/ShowtimeAdmin.vue'),
          meta: { title: '排片管理' },
        },
      ],
    },
  ],
  scrollBehavior: () => ({ top: 0 }),
})

router.beforeEach((to) => {
  const userStore = useUserStore()

  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }

  if (to.meta.requiresAdmin && !userStore.isAdmin) {
    ElMessage.warning('需要管理员权限')
    return { path: '/' }
  }

  if (to.meta.guestOnly && userStore.isLoggedIn) {
    return { path: '/' }
  }
})

router.afterEach((to) => {
  document.title = to.meta.title ? `${to.meta.title} · 光影票务` : '光影票务'
})

export default router
