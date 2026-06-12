import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login, getAdminInfo } from '@/api/admin'
import router from '@/router'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('admin_token') || '')
  const adminInfo = ref<any>(null)

  const isLoggedIn = computed(() => !!token.value)
  const role = computed(() => adminInfo.value?.role || '')

  async function doLogin(username: string, password: string) {
    const res = await login({ username, password })
    token.value = res.data.token
    localStorage.setItem('admin_token', res.data.token)
    adminInfo.value = res.data.adminInfo
    return res
  }

  async function fetchAdminInfo() {
    if (!token.value) return
    try {
      const res = await getAdminInfo()
      adminInfo.value = res.data
    } catch {
      doLogout()
    }
  }

  function doLogout() {
    token.value = ''
    adminInfo.value = null
    localStorage.removeItem('admin_token')
    router.push('/login')
  }

  return { token, adminInfo, isLoggedIn, role, doLogin, fetchAdminInfo, doLogout }
})
