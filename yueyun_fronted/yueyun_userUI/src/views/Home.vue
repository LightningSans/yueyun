<template>
  <div>
    <div class="banner">
      <div v-for="(slide, i) in banners" :key="i" class="banner-slide" :class="{ active: currentSlide === i }">
        <div class="bg" :style="{ background: slide.bg }">
          <div class="banner-content">
            <div class="tag">{{ slide.tag }}</div>
            <h2>{{ slide.title }}</h2>
            <p>{{ slide.desc }}</p>
          </div>
        </div>
      </div>
      <div class="banner-dots">
        <span v-for="(_, i) in banners" :key="i" class="dot" :class="{ active: currentSlide === i }" @click="currentSlide = i"></span>
      </div>
    </div>

    <div class="section-header"><h2>全部分类</h2><router-link to="/list" class="more">查看更多 →</router-link></div>
    <div class="cat-grid">
      <div v-for="cat in categories" :key="cat.id" class="cat-item" @click="router.push({ path: '/list', query: { categoryId: cat.id } })">
        <div class="cat-icon" :style="{ background: catColors[cat.id % catColors.length] }">{{ catIcons[cat.id % catIcons.length] }}</div>
        <span class="cat-name">{{ cat.name }}</span>
      </div>
    </div>

    <div class="section-header"><h2>为你推荐</h2><router-link to="/list" class="more">查看更多 →</router-link></div>
    <div v-if="loading" class="loading-state">加载中...</div>
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
          <div class="tag-row"><span class="t" v-for="tag in tagList(p)">{{ tag }}</span></div>
          <div class="price-row">
            <span class="price">¥{{ formatNum(p.price) }}</span>
            <span v-if="p.originalPrice" class="old-price">¥{{ formatNum(p.originalPrice) }}</span>
          </div>
          <div class="meta"><span>已售 {{ formatNum(p.sales) }}</span><span v-if="p.ratingAvg">好评率 {{ Math.round(p.ratingAvg * 100) }}%</span></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { getCategoryList, getProductList } from '@/api'

const router = useRouter()

const banners = [
  { bg: 'linear-gradient(135deg,#E85D3A 0%,#F4A261 100%)', tag: '🔥 限时特惠', title: '夏日清凉节', desc: '精选好物低至5折 · 品质生活一站购齐' },
  { bg: 'linear-gradient(135deg,#2A9D8F 0%,#264653 100%)', tag: '🌱 新品首发', title: '有机生活馆', desc: '健康食材 · 绿色生活 · 从每一口开始' },
  { bg: 'linear-gradient(135deg,#E63946 0%,#E85D3A 100%)', tag: '🚀 品牌日', title: '数码狂欢周', desc: '大牌直降 · 最高省3000 · 12期免息' },
]
const catColors = ['#FFE8E0', '#E0F0E8', '#FFF3E0', '#E8E0FF', '#FFE8EC', '#E8F4F8', '#F0E8F8', '#F8F0E8']
const catIcons = ['📱', '👔', '🍎', '🏠', '💄', '📚', '🎮', '🔧']
const cardColors = ['#E8F0FE', '#FFF3E0', '#E0F0E8', '#F3E8FF', '#FFE8E0', '#E8F8F8', '#F0E8FF', '#E4F0E0']

const currentSlide = ref(0)
const categories = ref<any[]>([])
const products = ref<any[]>([])
const loading = ref(true)

let timer: any = null
onMounted(async () => {
  timer = setInterval(() => { currentSlide.value = (currentSlide.value + 1) % banners.length }, 4000)
  try {
    const [catRes, prodRes] = await Promise.all([getCategoryList(), getProductList({ page: 1, size: 8 })])
    categories.value = catRes.data || []
    products.value = prodRes.data?.records || []
  } catch { /* ignore */ }
  finally { loading.value = false }
})
onUnmounted(() => clearInterval(timer))

function formatNum(n: any) { return n?.toLocaleString?.() || n }
function tagList(p: any) {
  const tags: string[] = []
  if (p.brand) tags.push(p.brand)
  if (p.keywords) { const ks = p.keywords.split(',').slice(0, 2); tags.push(...ks) }
  return tags.slice(0, 2)
}
</script>

<style scoped>
.banner { position: relative; border-radius: var(--radius); overflow: hidden; height: 320px; margin-bottom: 32px; }
.banner-slide { position: absolute; inset: 0; opacity: 0; transition: opacity .5s ease; }
.banner-slide.active { opacity: 1; }
.banner-slide .bg { height: 100%; display: flex; align-items: center; padding: 0 48px; }
.banner-content { position: relative; z-index: 1; }
.banner-content .tag { display: inline-block; background: rgba(255,255,255,.2); backdrop-filter: blur(8px); color: #fff; padding: 4px 14px; border-radius: 20px; font-size: 12px; margin-bottom: 12px; font-weight: 500; }
.banner-content h2 { color: #fff; font-size: 36px; font-weight: 900; margin-bottom: 8px; line-height: 1.2; }
.banner-content p { color: rgba(255,255,255,.85); font-size: 15px; }
.banner-dots { position: absolute; bottom: 16px; left: 50%; transform: translateX(-50%); display: flex; gap: 6px; }
.banner-dots .dot { width: 8px; height: 8px; border-radius: 50%; background: rgba(255,255,255,.4); cursor: pointer; transition: all .3s; }
.banner-dots .dot.active { width: 28px; border-radius: 4px; background: #fff; }
.section-header { display: flex; align-items: baseline; justify-content: space-between; margin-bottom: 16px; }
.section-header h2 { font-size: 22px; font-weight: 700; }
.section-header .more { font-size: 13px; color: var(--text-light); cursor: pointer; transition: color .15s; }
.section-header .more:hover { color: var(--primary); }
.cat-grid { display: grid; grid-template-columns: repeat(8, 1fr); gap: 12px; margin-bottom: 36px; }
.cat-item { display: flex; flex-direction: column; align-items: center; gap: 8px; padding: 16px 8px; background: var(--bg-white); border-radius: var(--radius); cursor: pointer; transition: all .2s; border: 1px solid transparent; }
.cat-item:hover { border-color: var(--border); box-shadow: var(--shadow); transform: translateY(-2px); }
.cat-icon { width: 48px; height: 48px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 24px; }
.cat-name { font-size: 13px; font-weight: 500; color: var(--text-secondary); }
.product-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 36px; }
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
.product-name:hover { color: var(--primary); }
.tag-row { display: flex; gap: 4px; margin-top: 6px; flex-wrap: wrap; }
.tag-row .t { font-size: 10px; padding: 1px 6px; border-radius: 3px; background: var(--primary-light); color: var(--primary); font-weight: 500; }
.price-row { display: flex; align-items: baseline; gap: 6px; margin-top: 8px; }
.price { font-size: 22px; font-weight: 700; color: var(--danger); }
.old-price { font-size: 12px; color: var(--text-light); text-decoration: line-through; }
.meta { display: flex; justify-content: space-between; margin-top: 6px; font-size: 12px; color: var(--text-light); }
.loading-state { text-align: center; padding: 60px 0; color: var(--text-light); }
@media (max-width: 960px) { .product-grid { grid-template-columns: repeat(3, 1fr); } .cat-grid { grid-template-columns: repeat(4, 1fr); } }
@media (max-width: 640px) { .product-grid { grid-template-columns: repeat(2, 1fr); } .banner { height: 200px; } }
</style>
