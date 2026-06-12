<template>
  <div class="auth-page">
    <div class="auth-box">
      <h2>欢迎回来</h2>
      <p class="sub">登录您的悦选商城账号</p>
      <div class="field">
        <label>手机号 / 用户名</label>
        <input v-model="form.username" type="text" placeholder="请输入用户名或手机号" />
      </div>
      <div class="field">
        <label>密码</label>
        <input v-model="form.password" type="password" placeholder="请输入密码" @keyup.enter="handleLogin" />
      </div>
      <button class="btn-login" :disabled="loading" @click="handleLogin">{{ loading ? '登录中...' : '登 录' }}</button>
      <div class="extra">
        <router-link to="/register">还没有账号？立即注册</router-link>
      </div>
      <div class="test-tip">测试账号: zhangsan / admin123</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { login } from '@/api'
import { ElMessage } from '@/utils/toast'

const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)
const form = reactive({ username: 'zhangsan', password: 'admin123' })

async function handleLogin() {
  if (!form.username || !form.password) { ElMessage('请输入用户名和密码'); return }
  loading.value = true
  try {
    const res = await login(form)
    authStore.setToken(res.data.token)
    authStore.setUserInfo(res.data.userInfo)
    ElMessage('登录成功')
    router.push('/')
  } catch (e: any) {
    ElMessage(e.message || '登录失败')
  } finally { loading.value = false }
}
</script>

<style scoped>
.auth-page { display: flex; justify-content: center; align-items: center; min-height: calc(100vh - var(--nav-h) - 60px); padding: 40px 0; }
.auth-box { width: 400px; background: var(--bg-white); border-radius: var(--radius); border: 1px solid var(--border-light); padding: 36px; box-shadow: var(--shadow-lg); }
.auth-box h2 { font-size: 24px; font-weight: 700; margin-bottom: 4px; }
.auth-box .sub { font-size: 13px; color: var(--text-light); margin-bottom: 24px; }
.auth-box .field { margin-bottom: 16px; }
.auth-box .field label { display: block; font-size: 13px; font-weight: 500; margin-bottom: 4px; color: var(--text-secondary); }
.auth-box .field input { width: 100%; height: 44px; padding: 0 14px; border: 1.5px solid var(--border); border-radius: var(--radius-xs); font-size: 14px; outline: none; transition: all .2s; font-family: inherit; }
.auth-box .field input:focus { border-color: var(--primary); box-shadow: 0 0 0 3px rgba(232,93,58,.1); }
.auth-box .btn-login { width: 100%; height: 46px; border: none; border-radius: var(--radius-xs); background: var(--primary-gradient); color: #fff; font-size: 15px; font-weight: 600; cursor: pointer; transition: all .15s; font-family: inherit; }
.auth-box .btn-login:hover { box-shadow: 0 4px 15px rgba(232,93,58,.3); }
.auth-box .btn-login:disabled { opacity: .5; cursor: not-allowed; }
.auth-box .extra { display: flex; justify-content: center; margin-top: 14px; font-size: 13px; }
.auth-box .extra a { color: var(--primary); cursor: pointer; }
.auth-box .extra a:hover { text-decoration: underline; }
.auth-box .test-tip { margin-top: 16px; padding: 10px; background: var(--primary-light); border-radius: var(--radius-xs); font-size: 12px; color: var(--text-secondary); text-align: center; }
</style>
