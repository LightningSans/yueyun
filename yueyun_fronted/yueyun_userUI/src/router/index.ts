import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', name: 'Login', component: () => import('@/views/Login.vue'), meta: { title: '登录' } },
    { path: '/register', name: 'Register', component: () => import('@/views/Register.vue'), meta: { title: '注册' } },
    { path: '/', name: 'Home', component: () => import('@/views/Home.vue'), meta: { title: '首页' } },
    { path: '/list', name: 'ProductList', component: () => import('@/views/ProductList.vue'), meta: { title: '商品列表', requiresAuth: true } },
    { path: '/detail/:id', name: 'ProductDetail', component: () => import('@/views/ProductDetail.vue'), meta: { title: '商品详情', requiresAuth: true } },
    { path: '/cart', name: 'Cart', component: () => import('@/views/Cart.vue'), meta: { title: '购物车', requiresAuth: true } },
    { path: '/checkout', name: 'Checkout', component: () => import('@/views/Checkout.vue'), meta: { title: '确认订单', requiresAuth: true } },
    { path: '/orders', name: 'OrderList', component: () => import('@/views/OrderList.vue'), meta: { title: '我的订单', requiresAuth: true } },
    { path: '/orders/:id', name: 'OrderDetail', component: () => import('@/views/OrderDetail.vue'), meta: { title: '订单详情', requiresAuth: true } },
    { path: '/profile', name: 'Profile', component: () => import('@/views/Profile.vue'), meta: { title: '个人中心', requiresAuth: true } },
    { path: '/pay/success', name: 'PaySuccess', component: () => import('@/views/PaySuccess.vue'), meta: { title: '支付成功', requiresAuth: true } },
  ]
})

/** 免登录的白名单路由 */
const publicPaths = ['/login', '/register', '/']

router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token')
  document.title = (to.meta.title as string || '悦选商城') + ' - 悦选商城'

  // 已登录用户访问登录/注册页 → 跳首页
  if (token && (to.path === '/login' || to.path === '/register')) {
    next('/')
    return
  }

  // 白名单路由无需登录
  if (publicPaths.includes(to.path)) {
    next()
    return
  }

  // 非白名单路由必须登录
  if (!token) {
    next('/login')
    return
  }

  next()
})

export default router
