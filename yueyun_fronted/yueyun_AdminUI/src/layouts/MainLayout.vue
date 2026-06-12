<template>
  <el-container class="layout-container">
    <el-aside :width="isCollapse ? '64px' : '220px'" class="layout-aside">
      <Sidebar :isCollapse="isCollapse" />
    </el-aside>
    <el-container>
      <el-header class="layout-header">
        <Header @toggle-collapse="isCollapse = !isCollapse" :isCollapse="isCollapse" />
      </el-header>
      <el-main class="layout-main">
        <router-view v-slot="{ Component, route }">
          <!-- 使用 :key 强制重新渲染，不用 mode="out-in" 防止死锁 -->
          <transition name="fade">
            <component :is="Component" :key="route.path" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import Sidebar from './Sidebar.vue'
import Header from './Header.vue'

const auth = useAuthStore()
const isCollapse = ref(false)

// 页面刷新后重新加载管理员信息
onMounted(() => {
  if (auth.token) {
    auth.fetchAdminInfo()
  }
})
</script>

<style scoped>
.layout-container {
  height: 100vh;
}
.layout-aside {
  background: #001529;
  overflow: hidden;
  transition: width 0.25s;
}
.layout-header {
  background: #fff;
  padding: 0;
  height: 56px;
  border-bottom: 1px solid #e8e8f0;
  display: flex;
  align-items: center;
}
.layout-main {
  background: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}
</style>
