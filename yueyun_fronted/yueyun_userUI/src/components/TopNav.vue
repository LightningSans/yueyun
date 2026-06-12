<template>
  <nav class="top-nav">
    <div class="top-nav-inner">
      <router-link to="/" class="logo">Yue<span>Xuan</span></router-link>
      <div class="nav-links">
        <router-link to="/">首页</router-link>
        <router-link to="/list">全部商品</router-link>
      </div>
      <div class="nav-search">
        <span class="icon">🔍</span>
        <input v-model="keyword" placeholder="搜索商品名称、品牌..." @keyup.enter="doSearch" />
      </div>
      <div class="nav-actions">
        <template v-if="authStore.isLoggedIn">
          <router-link to="/profile" class="user-info">
            <div class="avatar">{{ avatarText }}</div>
            <span class="name">{{ authStore.userInfo?.nickname || authStore.userInfo?.username }}</span>
          </router-link>
        </template>
        <template v-else>
          <router-link to="/login" class="btn btn-text">登录</router-link>
          <router-link to="/register" class="btn btn-primary">注册</router-link>
        </template>
        <router-link to="/cart" class="btn btn-cart">
          🛒<span v-if="cartCount > 0" class="badge">{{ cartCount > 99 ? '99+' : cartCount }}</span>
        </router-link>
      </div>
    </div>
  </nav>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const keyword = ref('')
const cartCount = ref(0)

const avatarText = computed(() => {
  const name = authStore.userInfo?.nickname || authStore.userInfo?.username || ''
  return name.charAt(0) || 'U'
})

function doSearch() {
  if (keyword.value.trim()) {
    router.push({ path: '/list', query: { keyword: keyword.value.trim() } })
  }
}
</script>

<style scoped>
.top-nav { position: sticky; top: 0; z-index: 100; background: var(--bg-white); border-bottom: 1px solid var(--border); }
.top-nav-inner { max-width: 1280px; margin: 0 auto; height: var(--nav-h); display: flex; align-items: center; gap: 24px; padding: 0 32px; }
.logo { font-family: 'DM Serif Display', serif; font-size: 26px; font-weight: 700; color: var(--primary); font-style: italic; cursor: pointer; flex-shrink: 0; letter-spacing: -.5px; }
.logo span { color: var(--text); font-style: normal; }
.nav-links { display: flex; align-items: center; gap: 4px; flex-shrink: 0; }
.nav-links a { padding: 8px 16px; font-size: 14px; font-weight: 500; color: var(--text-secondary); border-radius: var(--radius-xs); transition: all .15s; white-space: nowrap; }
.nav-links a:hover { color: var(--primary); background: var(--primary-light); }
.nav-links a.router-link-active { color: var(--primary); font-weight: 600; background: var(--primary-light); }
.nav-search { flex: 1; max-width: 420px; position: relative; }
.nav-search input { width: 100%; height: 40px; padding: 0 16px 0 40px; border: 1.5px solid var(--border); border-radius: 20px; font-size: 13px; outline: none; transition: all .2s; font-family: inherit; background: var(--bg); color: var(--text); }
.nav-search input:focus { border-color: var(--primary); box-shadow: 0 0 0 3px rgba(232,93,58,.1); background: var(--bg-white); }
.nav-search .icon { position: absolute; left: 14px; top: 50%; transform: translateY(-50%); color: var(--text-light); font-size: 14px; pointer-events: none; }
.nav-actions { display: flex; align-items: center; gap: 6px; margin-left: auto; flex-shrink: 0; }
.nav-actions .btn { height: 36px; padding: 0 16px; border: none; border-radius: var(--radius-xs); font-size: 13px; font-weight: 500; cursor: pointer; transition: all .15s; font-family: inherit; display: flex; align-items: center; gap: 6px; white-space: nowrap; }
.nav-actions .btn-text { background: none; color: var(--text-secondary); }
.nav-actions .btn-text:hover { color: var(--primary); background: var(--primary-light); }
.nav-actions .btn-primary { background: var(--primary); color: #fff; }
.nav-actions .btn-primary:hover { background: var(--primary-dark); }
.nav-actions .btn-cart { position: relative; background: none; color: var(--text-secondary); font-size: 18px; padding: 0 10px; text-decoration: none; }
.nav-actions .btn-cart:hover { color: var(--primary); }
.nav-actions .btn-cart .badge { position: absolute; top: -2px; right: -2px; background: var(--danger); color: #fff; font-size: 10px; padding: 1px 6px; border-radius: 8px; font-weight: 600; min-width: 16px; text-align: center; }
.nav-actions .user-info { display: flex; align-items: center; gap: 8px; cursor: pointer; padding: 4px 10px 4px 4px; border-radius: var(--radius-xs); transition: all .15s; text-decoration: none; }
.nav-actions .user-info:hover { background: var(--bg); }
.nav-actions .user-info .avatar { width: 28px; height: 28px; border-radius: 50%; background: var(--primary-gradient); display: flex; align-items: center; justify-content: center; color: #fff; font-size: 12px; font-weight: 700; }
.nav-actions .user-info .name { font-size: 13px; font-weight: 500; color: var(--text); }
</style>
