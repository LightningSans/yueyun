<template>
  <div class="sidebar">
    <div class="sidebar-logo">
      <div class="logo-icon">E</div>
      <span v-show="!isCollapse" class="logo-text">悦选商城</span>
    </div>
    <el-menu
      :default-active="activeMenu"
      :collapse="isCollapse"
      :router="true"
      background-color="#001529"
      text-color="rgba(255,255,255,0.65)"
      active-text-color="#fff"
      class="sidebar-menu"
    >
      <template v-for="item in menuItems" :key="item.path">
        <el-menu-item v-if="!item.children || !item.children.length" :index="item.path">
          <el-icon><component :is="item.meta?.icon as string" /></el-icon>
          <template #title>{{ item.meta?.title }}</template>
        </el-menu-item>
        <el-sub-menu v-else :index="item.path">
          <template #title>
            <el-icon><component :is="item.meta?.icon as string" /></el-icon>
            <span>{{ item.meta?.title }}</span>
          </template>
          <el-menu-item v-for="child in item.children" :key="child.path" :index="child.path">
            {{ child.meta?.title }}
          </el-menu-item>
        </el-sub-menu>
      </template>
    </el-menu>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const props = defineProps<{ isCollapse: boolean }>()
const route = useRoute()
const auth = useAuthStore()

const activeMenu = computed(() => route.path)

const menuItems = computed(() => {
  const allItems = [
    { path: '/dashboard', meta: { title: '工作台', icon: 'Odometer' } },
    { path: '/users', meta: { title: '用户管理', icon: 'User' } },
    { path: '/admins', meta: { title: '管理员管理', icon: 'Lock', roles: ['SUPER_ADMIN'] } },
    { path: '/categories', meta: { title: '分类管理', icon: 'Folder' } },
    { path: '/products', meta: { title: '商品管理', icon: 'Goods' } },
    { path: '/orders', meta: { title: '订单管理', icon: 'List' } },
    { path: '/couriers', meta: { title: '配送员管理', icon: 'Van' } },
    { path: '/payments', meta: { title: '支付记录', icon: 'Money' } },
    { path: '/reviews', meta: { title: '评价管理', icon: 'Star' } },
    { path: '/statistics', meta: { title: '数据统计', icon: 'DataAnalysis' } }
  ]
  return allItems.filter(item => {
    if (item.meta?.roles && !item.meta.roles.includes(auth.role)) return false
    return true
  })
})
</script>

<style scoped>
.sidebar {
  height: 100%;
  display: flex;
  flex-direction: column;
}
.sidebar-logo {
  height: 56px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  flex-shrink: 0;
}
.logo-icon {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #1677FF, #6950F0);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
  font-weight: 800;
  flex-shrink: 0;
}
.logo-text {
  color: #fff;
  font-size: 16px;
  font-weight: 700;
  white-space: nowrap;
}
.sidebar-menu {
  flex: 1;
  border-right: none;
  overflow-y: auto;
}
.sidebar-menu:not(.el-menu--collapse) {
  width: 220px;
}
</style>
