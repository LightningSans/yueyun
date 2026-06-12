<template>
  <div>
    <div class="section-header"><h2>{{ keyword ? '搜索: ' + keyword : '全部商品' }}</h2><span class="more">共 <strong>{{ total }}</strong> 件商品</span></div>
    <div class="filter-toolbar">
      <span class="label">排序：</span>
      <button v-for="s in sorts" :key="s.key" class="sort-btn" :class="{ active: activeSort === s.key }" @click="setSort(s.key)">{{ s.label }}</button>
      <span class="divider"></span>
      <button v-for="c in categories" :key="c.id" class="chip" :class="{ active: query.categoryId === c.id }" @click="toggleCategory(c.id)">{{ c.name }}</button>
      <span class="result-count">共 {{ total }} 件商品</span>
    </div>
    <div v-if="loading" class="loading-state">加载中...</div>
    <div v-else-if="!products.length" class="empty-state"><div class="empty-icon">📦</div><p>暂无商品</p></div>
    <div v-else class="product-grid">
      <div v-for="p in products" :key="p.id" class="product-card" @click="router.push('/detail/' + p.id)">
        <div class="product-img" :style="{ background: p.mainImage ? '#f5f5f5' : cardColors[p.id % cardColors.length] }">
          <img v-if="p.mainImage" :src="p.mainImage" class="product-img-real" @error="e => e.target.style.display='none'" />
          <span v-if="!p.mainImage" class="product-img-placeholder">{{ ['📱','💻','👕','🍪','🧴','🏺','📦'][p.id % 7] }}</span>
          <span v-if="p.sales > 500" class="badge hot">HOT</span>
          <span v-if="p.originalPrice && p.originalPrice > p.price * 1.3" class="badge discount">-{{ Math.round((1 - p.price / p.originalPrice) * 100) }}%</span>
        </div>
        <div class="product-info">
          <div class="product-name">{{ p.name }}</div>
          <div class="price-row"><span class="price">¥{{ formatNum(p.price) }}</span><span v-if="p.originalPrice" class="old-price">¥{{ formatNum(p.originalPrice) }}</span></div>
          <div class="meta"><span>已售 {{ formatNum(p.sales) }}</span></div>
        </div>
      </div>
    </div>
    <div v-if="pages > 1" class="pagination">
      <button :disabled="query.page <= 1" @click="query.page--; fetchData()">上一页</button>
      <span>{{ query.page }} / {{ pages }}</span>
      <button :disabled="query.page >= pages" @click="query.page++; fetchData()">下一页</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getProductList, getCategoryList } from '@/api'

const route = useRoute()
const router = useRouter()
const products = ref<any[]>([])
const categories = ref<any[]>([])
const total = ref(0)
const pages = ref(1)
const loading = ref(true)
const activeSort = ref('default')
const cardColors = ['#E8F0FE', '#FFF3E0', '#E0F0E8', '#F3E8FF', '#FFE8E0', '#E8F8F8', '#F0E8FF', '#E4F0E0']
const keyword = ref('')
const sorts = [
  { key: 'default', label: '综合' }, { key: 'sales', label: '销量' },
  { key: 'price_asc', label: '价格 ↑' }, { key: 'price_desc', label: '价格 ↓' },
]

const query = reactive({ page: 1, size: 12, categoryId: undefined as number | undefined, keyword: undefined as string | undefined, sort: undefined as string | undefined, order: undefined as string | undefined })

onMounted(async () => {
  if (route.query.keyword) { keyword.value = route.query.keyword as string; query.keyword = route.query.keyword as string }
  if (route.query.categoryId) query.categoryId = Number(route.query.categoryId)
  try { const res = await getCategoryList(); categories.value = res.data || [] } catch { /* ignore */ }
  fetchData()
})

watch(() => route.query, (q) => {
  keyword.value = q.keyword as string || ''
  query.keyword = q.keyword as string || undefined
  query.categoryId = q.categoryId ? Number(q.categoryId) : undefined
  query.page = 1
  fetchData()
})

async function fetchData() { loading.value = true; try { const res = await getProductList(query); products.value = res.data.records || []; total.value = res.data.total || 0; pages.value = res.data.pages || 1 } catch { /* ignore */ } finally { loading.value = false } }

function setSort(key: string) {
  activeSort.value = key
  if (key === 'price_asc') { query.sort = 'price'; query.order = 'asc' }
  else if (key === 'price_desc') { query.sort = 'price'; query.order = 'desc' }
  else if (key === 'sales') { query.sort = 'sales'; query.order = undefined }
  else { query.sort = undefined; query.order = undefined }
  query.page = 1; fetchData()
}
function toggleCategory(id: number) { query.categoryId = query.categoryId === id ? undefined : id; query.page = 1; fetchData() }
function formatNum(n: any) { return n?.toLocaleString?.() || n }
</script>

<style scoped>
.section-header { display: flex; align-items: baseline; justify-content: space-between; margin-bottom: 16px; }
.section-header h2 { font-size: 22px; font-weight: 700; }
.section-header .more { font-size: 13px; color: var(--text-light); }
.filter-toolbar { display: flex; align-items: center; gap: 12px; padding: 14px 18px; background: var(--bg-white); border-radius: var(--radius-sm); border: 1px solid var(--border-light); margin-bottom: 16px; flex-wrap: wrap; }
.filter-toolbar .label { font-size: 13px; color: var(--text-light); font-weight: 500; }
.sort-btn { padding: 6px 14px; border: 1px solid var(--border); border-radius: 6px; font-size: 12px; cursor: pointer; background: var(--bg-white); color: var(--text-secondary); transition: all .15s; font-family: inherit; }
.sort-btn:hover { border-color: var(--primary); color: var(--primary); }
.sort-btn.active { background: var(--primary); border-color: var(--primary); color: #fff; }
.divider { width: 1px; height: 20px; background: var(--border); }
.chip { padding: 5px 12px; border-radius: 20px; font-size: 12px; cursor: pointer; background: var(--bg); color: var(--text-secondary); transition: all .15s; border: none; font-family: inherit; }
.chip:hover { background: var(--primary-light); color: var(--primary); }
.chip.active { background: var(--primary); color: #fff; }
.result-count { margin-left: auto; font-size: 12px; color: var(--text-light); }
.product-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
.product-card { background: var(--bg-white); border-radius: var(--radius); overflow: hidden; cursor: pointer; transition: all .2s; border: 1px solid var(--border-light); }
.product-card:hover { box-shadow: var(--shadow-lg); transform: translateY(-4px); }
.product-img { height: 200px; display: flex; align-items: center; justify-content: center; font-size: 56px; position: relative; overflow: hidden; }
.product-img-real { width: 100%; height: 100%; object-fit: cover; }
.product-img-placeholder { font-size: 56px; }
.product-img .badge { position: absolute; top: 10px; left: 10px; padding: 3px 10px; border-radius: 4px; font-size: 11px; font-weight: 600; }
.product-img .badge { position: absolute; top: 10px; left: 10px; padding: 3px 10px; border-radius: 4px; font-size: 11px; font-weight: 600; }
.product-img .badge.hot { background: var(--danger); color: #fff; }
.product-img .badge.discount { background: var(--accent); color: #fff; }
.product-info { padding: 14px 16px 16px; }
.product-name { font-size: 14px; font-weight: 500; line-height: 1.5; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; min-height: 42px; }
.price-row { display: flex; align-items: baseline; gap: 6px; margin-top: 8px; }
.price { font-size: 22px; font-weight: 700; color: var(--danger); }
.old-price { font-size: 12px; color: var(--text-light); text-decoration: line-through; }
.meta { margin-top: 6px; font-size: 12px; color: var(--text-light); }
.loading-state { text-align: center; padding: 60px 0; color: var(--text-light); }
.empty-state { text-align: center; padding: 80px 20px; }
.empty-icon { font-size: 64px; opacity: .3; margin-bottom: 16px; }
.pagination { display: flex; justify-content: center; align-items: center; gap: 16px; margin-top: 32px; }
.pagination button { padding: 8px 20px; border: 1px solid var(--border); border-radius: 6px; background: var(--bg-white); cursor: pointer; font-family: inherit; font-size: 13px; }
.pagination button:hover { border-color: var(--primary); color: var(--primary); }
.pagination button:disabled { opacity: .4; cursor: not-allowed; }
@media (max-width: 960px) { .product-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 640px) { .product-grid { grid-template-columns: repeat(2, 1fr); } }
</style>
