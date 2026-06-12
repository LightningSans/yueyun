<template>
  <div class="auth-page">
    <div class="auth-box">
      <h2>创建账号</h2>
      <p class="sub">注册悦选商城，开启品质生活</p>
      <div class="field"><label>用户名 *</label><input v-model="form.username" type="text" placeholder="请输入用户名" /></div>
      <div class="field"><label>密码 *</label><input v-model="form.password" type="password" placeholder="请输入密码" /></div>
      <div class="field"><label>昵称</label><input v-model="form.nickname" placeholder="请输入昵称" /></div>
      <div class="field"><label>手机号</label><input v-model="form.phone" placeholder="请输入手机号" /></div>
      <div class="field"><label>邮箱</label><input v-model="form.email" type="email" placeholder="请输入邮箱" /></div>
      <button class="btn-login" :disabled="loading" @click="handleRegister">{{ loading ? '注册中...' : '注 册' }}</button>
      <div class="extra"><router-link to="/login">已有账号？立即登录</router-link></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { register } from '@/api'
import { ElMessage } from '@/utils/toast'

const router = useRouter()
const loading = ref(false)
const form = reactive({ username: '', password: '', nickname: '', phone: '', email: '' })

async function handleRegister() {
  if (!form.username || !form.password) { ElMessage('用户名和密码不能为空'); return }
  loading.value = true
  try {
    await register(form)
    ElMessage('注册成功，请登录')
    router.push('/login')
  } catch (e: any) { ElMessage(e.message || '注册失败') }
  finally { loading.value = false }
}
</script>

<style scoped>
.auth-page { display: flex; justify-content: center; align-items: center; min-height: calc(100vh - var(--nav-h) - 60px); padding: 40px 0; }
.auth-box { width: 400px; background: var(--bg-white); border-radius: var(--radius); border: 1px solid var(--border-light); padding: 36px; box-shadow: var(--shadow-lg); }
.auth-box h2 { font-size: 24px; font-weight: 700; margin-bottom: 4px; }
.auth-box .sub { font-size: 13px; color: var(--text-light); margin-bottom: 24px; }
.auth-box .field { margin-bottom: 14px; }
.auth-box .field label { display: block; font-size: 13px; font-weight: 500; margin-bottom: 4px; color: var(--text-secondary); }
.auth-box .field input { width: 100%; height: 42px; padding: 0 14px; border: 1.5px solid var(--border); border-radius: var(--radius-xs); font-size: 14px; outline: none; transition: all .2s; font-family: inherit; }
.auth-box .field input:focus { border-color: var(--primary); box-shadow: 0 0 0 3px rgba(232,93,58,.1); }
.auth-box .btn-login { width: 100%; height: 46px; border: none; border-radius: var(--radius-xs); background: var(--primary-gradient); color: #fff; font-size: 15px; font-weight: 600; cursor: pointer; transition: all .15s; font-family: inherit; margin-top: 4px; }
.auth-box .btn-login:hover { box-shadow: 0 4px 15px rgba(232,93,58,.3); }
.auth-box .btn-login:disabled { opacity: .5; cursor: not-allowed; }
.auth-box .extra { display: flex; justify-content: center; margin-top: 14px; font-size: 13px; }
.auth-box .extra a { color: var(--primary); cursor: pointer; }
.auth-box .extra a:hover { text-decoration: underline; }
</style>
