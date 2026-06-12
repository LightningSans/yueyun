<template>
  <div id="app-root">
    <TopNav v-if="showNav" />
    <SubNav v-if="showNav && authStore.isLoggedIn" />
    <main :class="['main-content', { 'has-nav': showNav }]">
      <router-view />
    </main>
    <SiteFooter v-if="showNav" />
    <AIChatDialog />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import TopNav from '@/components/TopNav.vue'
import SubNav from '@/components/SubNav.vue'
import SiteFooter from '@/components/SiteFooter.vue'
import AIChatDialog from '@/components/AIChatDialog.vue'

const route = useRoute()
const authStore = useAuthStore()
const showNav = computed(() => !['Login', 'Register'].includes(route.name as string))
</script>

<style scoped>
#app-root { min-height: 100vh; display: flex; flex-direction: column; }
.main-content { flex: 1; }
.main-content.has-nav { max-width: 1280px; margin: 0 auto; padding: 20px 32px 60px; width: 100%; }
@media (max-width: 640px) { .main-content.has-nav { padding: 16px; } }
</style>
