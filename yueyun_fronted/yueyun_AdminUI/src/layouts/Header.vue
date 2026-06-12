<template>
  <div class="header">
    <div class="header-left">
      <el-icon class="collapse-btn" @click="$emit('toggleCollapse')" :size="20">
        <Fold v-if="!isCollapse" />
        <Expand v-else />
      </el-icon>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item v-if="route.meta.title">{{ route.meta.title }}</el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    <div class="header-right">
      <el-icon :size="18" class="header-icon"><Bell /></el-icon>
      <el-dropdown trigger="click" @command="handleCommand">
        <span class="user-info">
          <el-avatar :size="28" style="background: #1677FF;">
            {{ auth.adminInfo?.realName?.charAt(0) || '张' }}
          </el-avatar>
          <span class="user-name">{{ auth.adminInfo?.realName || '管理员' }}</span>
          <el-icon><CaretBottom /></el-icon>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">
              <el-icon><User /></el-icon>个人信息
            </el-dropdown-item>
            <el-dropdown-item divided command="logout">
              <el-icon><SwitchButton /></el-icon>退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

defineProps<{ isCollapse: boolean }>()
defineEmits<{ toggleCollapse: [] }>()

const route = useRoute()
const auth = useAuthStore()

function handleCommand(cmd: string) {
  if (cmd === 'logout') {
    auth.doLogout()
  }
}
</script>

<style scoped>
.header {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}
.collapse-btn {
  cursor: pointer;
  color: #5a5a7a;
}
.collapse-btn:hover {
  color: #1677FF;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}
.header-icon {
  cursor: pointer;
  color: #8c8ca1;
}
.header-icon:hover {
  color: #1677FF;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 6px;
  transition: background 0.15s;
}
.user-info:hover {
  background: #f5f5f5;
}
.user-name {
  font-size: 13px;
  font-weight: 500;
  color: #1a1a2e;
}
</style>
