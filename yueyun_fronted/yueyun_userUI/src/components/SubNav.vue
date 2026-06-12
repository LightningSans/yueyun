<template>
  <div class="sub-nav">
    <a v-for="cat in categories" :key="cat.id" @click="goList(cat.id)">{{ cat.name }}</a>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getCategoryList } from '@/api'

const router = useRouter()
const categories = ref<any[]>([])

onMounted(async () => {
  try {
    const res = await getCategoryList()
    categories.value = res.data?.slice(0, 8) || []
  } catch { /* ignore */ }
})

function goList(id?: number) {
  router.push({ path: '/list', query: id ? { categoryId: id } : {} })
}
</script>

<style scoped>
.sub-nav { max-width: 1280px; margin: 0 auto; height: var(--subnav-h); display: flex; align-items: center; gap: 2px; padding: 0 32px; border-bottom: 1px solid var(--border-light); background: var(--bg-white); }
.sub-nav a { padding: 6px 14px; font-size: 12px; color: var(--text-light); border-radius: 4px; cursor: pointer; transition: all .15s; white-space: nowrap; }
.sub-nav a:hover { color: var(--text); background: var(--bg); }
</style>
