<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-header">
        <div class="logo-icon">E</div>
        <h2>悦选商城 · 管理后台</h2>
        <p>电商管理系统</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" class="login-form" @keyup.enter="handleLogin">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="请输入管理员账号" :prefix-icon="User" size="large" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" :prefix-icon="Lock" size="large" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" :loading="loading" class="login-btn" @click="handleLogin">
            {{ loading ? '登录中...' : '登 录' }}
          </el-button>
        </el-form-item>
      </el-form>
      <div class="login-footer">
        <span>基于 Spring Boot 3.3 + Vue 3 + Element Plus</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const auth = useAuthStore()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  username: 'superadmin',
  password: 'admin123'
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入管理员账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await auth.doLogin(form.username, form.password)
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch {
    // 错误已由拦截器处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0F1A2E 0%, #1A2942 50%, #0F1A2E 100%);
}
.login-card {
  width: 400px;
  background: #fff;
  border-radius: 12px;
  padding: 40px 36px 28px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}
.login-header {
  text-align: center;
  margin-bottom: 32px;
}
.logo-icon {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #1677FF, #6950F0);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 24px;
  font-weight: 800;
  margin: 0 auto 12px;
}
.login-header h2 {
  font-size: 20px;
  font-weight: 700;
  color: #1a1a2e;
  margin-bottom: 4px;
}
.login-header p {
  font-size: 13px;
  color: #9e9eaf;
}
.login-form {
  margin-bottom: 16px;
}
.login-btn {
  width: 100%;
  height: 44px;
  font-size: 15px;
  font-weight: 600;
}
.login-footer {
  text-align: center;
  font-size: 11px;
  color: #c4c4d0;
}
</style>
