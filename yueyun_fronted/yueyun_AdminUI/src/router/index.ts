import type { RouteRecordRaw } from 'vue-router'
import { createRouter, createWebHistory } from 'vue-router'

// 公共路由（无需登录）
const publicRoutes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  }
]

// 主布局路由（需要登录）
const mainRoutes: RouteRecordRaw = {
  path: '/',
  component: () => import('@/layouts/MainLayout.vue'),
  redirect: '/dashboard',
  meta: { requiresAuth: true },
  children: [
    {
      path: 'dashboard',
      name: 'Dashboard',
      component: () => import('@/views/dashboard/Index.vue'),
      meta: { title: '工作台', icon: 'Odometer' }
    },
    {
      path: 'users',
      name: 'Users',
      component: () => import('@/views/user/Index.vue'),
      meta: { title: '用户管理', icon: 'User' }
    },
    {
      path: 'admins',
      name: 'Admins',
      component: () => import('@/views/admin/Index.vue'),
      meta: { title: '管理员管理', icon: 'Lock', roles: ['SUPER_ADMIN'] }
    },
    {
      path: 'categories',
      name: 'Categories',
      component: () => import('@/views/category/Index.vue'),
      meta: { title: '分类管理', icon: 'Folder' }
    },
    {
      path: 'products',
      name: 'Products',
      component: () => import('@/views/product/Index.vue'),
      meta: { title: '商品管理', icon: 'Goods' }
    },
    {
      path: 'orders',
      name: 'Orders',
      component: () => import('@/views/order/Index.vue'),
      meta: { title: '订单管理', icon: 'List' }
    },
    {
      path: 'orders/:id',
      name: 'OrderDetail',
      component: () => import('@/views/order/Detail.vue'),
      meta: { title: '订单详情', hidden: true }
    },
    {
      path: 'couriers',
      name: 'Couriers',
      component: () => import('@/views/courier/Index.vue'),
      meta: { title: '配送员管理', icon: 'Van' }
    },
    {
      path: 'payments',
      name: 'Payments',
      component: () => import('@/views/payment/Index.vue'),
      meta: { title: '支付记录', icon: 'Money' }
    },
    {
      path: 'reviews',
      name: 'Reviews',
      component: () => import('@/views/review/Index.vue'),
      meta: { title: '评价管理', icon: 'Star' }
    },
    {
      path: 'statistics',
      name: 'Statistics',
      component: () => import('@/views/statistics/Index.vue'),
      meta: { title: '数据统计', icon: 'DataAnalysis' }
    }
  ]
}

const router = createRouter({
  history: createWebHistory(),
  routes: [...publicRoutes, mainRoutes]
})

// 路由守卫
router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('admin_token')
  // 检查当前路由或其祖先路由是否有 requiresAuth
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth)

  if (requiresAuth && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/dashboard')
  } else {
    document.title = ((to.meta.title as string) || '悦选商城') + ' - 悦选商城'
    next()
  }
})

export default router
