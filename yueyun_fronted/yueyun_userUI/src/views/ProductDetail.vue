<template>
  <div v-if="loading" class="loading-state">加载中...</div>
  <template v-else-if="product">
    <div class="detail-wrap">
      <div class="detail-gallery">
        <div class="main-img">
          <img v-if="mainImageUrl" :src="mainImageUrl" class="main-img-real" @error="onMainImgError" />
          <span v-if="!mainImageUrl" class="main-img-placeholder">{{ ['📱','💻','👕','🍪','🧴','🏺','📦'][(product.id || 0) % 7] }}</span>
          <span v-if="product.originalPrice && product.originalPrice > product.price * 1.3" class="badge-lg">🔥 省 ¥{{ (product.originalPrice - product.price).toFixed(0) }}</span>
        </div>
        <div class="thumb-row">
          <div v-for="(img, i) in product.images || []" :key="i" class="thumb" :class="{ active: i === activeImg }" @click="activeImg = i; mainImgFailed = false">
            <img :src="img" style="width:100%;height:100%;object-fit:cover;border-radius:4px;" @error="e => e.target.style.display='none'" />
          </div>
        </div>
      </div>
      <div class="detail-info">
        <h1>{{ product.name }}</h1>
        <div class="price-block">
          <span class="now">¥{{ formatNum(product.price) }}</span>
          <span v-if="product.originalPrice" class="old">¥{{ formatNum(product.originalPrice) }}</span>
          <span v-if="product.originalPrice && product.originalPrice > product.price" class="tag-discount">省 ¥{{ (product.originalPrice - product.price).toFixed(0) }}</span>
          <span style="margin-left:auto;font-size:12px;color:var(--text-light);">已售 {{ formatNum(product.sales) }}</span>
        </div>
        <div class="meta-list">
          <span>📦 库存 {{ product.stock || 0 }} 件</span>
          <span>⭐ {{ product.ratingAvg || 0 }} 分</span>
          <span>🚚 {{ product.price >= 99 ? '免运费' : '满99免运费' }}</span>
        </div>
        <div class="qty-selector">
          <span class="label">数量</span>
          <div class="qty-box">
            <button @click="decreaseQty">−</button>
            <span>{{ quantity }}</span>
            <button @click="increaseQty">+</button>
          </div>
          <span style="font-size:12px;color:var(--text-light);margin-left:8px;">库存 {{ product.stock || 0 }} 件</span>
        </div>
        <div class="btn-row">
          <button class="btn btn-cart" @click="addToCart">🛒 加入购物车</button>
          <button class="btn btn-buy" @click="buyNow">立即购买</button>
        </div>
      </div>
    </div>

    <div class="detail-desc" v-if="product.description">
      <h3>商品详情</h3>
      <p>{{ product.description }}</p>
    </div>

    <div class="review-section" v-if="reviews.length > 0">
      <h3>商品评价 ({{ reviewTotal }})</h3>
      <div class="review-summary" v-if="ratingStats">
        <div class="score">
          <div class="num">{{ ratingStats.avg }}</div>
          <div class="stars">{{ '★'.repeat(Math.round(ratingStats.avg)) }}{{ '☆'.repeat(5 - Math.round(ratingStats.avg)) }}</div>
        </div>
        <div class="bars">
          <div v-for="i in 5" :key="i" class="bar-row">
            <span>{{ 6 - i }}星</span>
            <div class="bar-track"><div class="bar-fill" :style="{ width: (ratingStats.dist[String(6 - i)] / Math.max(...Object.values(ratingStats.dist), 1) * 100) + '%' }"></div></div>
            <span>{{ ratingStats.dist[String(6 - i)] }}</span>
          </div>
        </div>
      </div>
      <div v-for="r in reviews" :key="r.id" class="review-item">
        <div class="review-avatar" :style="{ background: r.userId ? avatarColors[r.userId % avatarColors.length] : '#ccc' }">
          {{ (r.userNickname || '?').charAt(0) }}
        </div>
        <div class="review-body">
          <div class="review-top">
            <span class="review-name">{{ r.isAnonymous ? '匿名用户' : (r.userNickname || '用户') }}</span>
            <span class="review-stars">{{ '★'.repeat(r.rating) }}{{ '☆'.repeat(5 - r.rating) }}</span>
            <span class="review-date">{{ formatDate(r.createTime) }}</span>
          </div>
          <div class="review-text">{{ r.content }}</div>
        </div>
      </div>
    </div>
  </template>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getProductDetail, getReviewList, addCart } from '@/api'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from '@/utils/toast'
import { formatDate } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const product = ref<any>(null)
const reviews = ref<any[]>([])
const reviewTotal = ref(0)
const ratingStats = ref<any>(null)
const loading = ref(true)
const quantity = ref(1)
const activeImg = ref(0)
const avatarColors = ['#E85D3A', '#2A9D8F', '#8B5CF6', '#F59E0B', '#06B6D4', '#E63946']

const mainImgFailed = ref(false)

const mainImageUrl = computed(() => {
  if (mainImgFailed.value) return null
  const imgs = product.value?.images
  if (Array.isArray(imgs) && imgs.length > 0) return imgs[activeImg.value] || imgs[0]
  return product.value?.mainImage || null
})

function onMainImgError() {
  mainImgFailed.value = true
}

onMounted(async () => {
  const id = Number(route.params.id)
  try {
    const res = await getProductDetail(id)
    product.value = res.data
  } catch { /* ignore */ }
  try {
    const revRes = await getReviewList(id, { page: 1, size: 5 })
    reviews.value = revRes.data?.records || []
    reviewTotal.value = revRes.data?.total || 0
    ratingStats.value = { avg: revRes.data?.ratingAvg || 0, dist: revRes.data?.ratingDistribution || { 5: 0, 4: 0, 3: 0, 2: 0, 1: 0 } }
  } catch { /* ignore */ }
  finally { loading.value = false }
})

function formatNum(n: any) { return n?.toLocaleString?.() || n }

function decreaseQty() { if (quantity.value > 1) quantity.value-- }
function increaseQty() { if (quantity.value < (product.value?.stock || 99)) quantity.value++ }

async function addToCart() {
  if (!authStore.isLoggedIn) { router.push('/login'); return }
  try {
    await addCart({ productId: product.value.id, quantity: quantity.value })
    ElMessage('已加入购物车')
  } catch (e: any) { ElMessage(e.message || '加入失败') }
}

function buyNow() {
  if (!authStore.isLoggedIn) { router.push('/login'); return }
  addToCart()
}
</script>

<style scoped>
.loading-state { text-align: center; padding: 60px 0; color: var(--text-light); }
.detail-wrap { display: grid; grid-template-columns: 1fr 1fr; gap: 40px; margin-bottom: 40px; }
.main-img { height: 480px; border-radius: var(--radius); display: flex; align-items: center; justify-content: center; font-size: 96px; position: relative; overflow: hidden; background: linear-gradient(135deg, #E8F0FE, #F0F6FF); }
.main-img-real { width: 100%; height: 100%; object-fit: contain; }
.main-img-placeholder { font-size: 96px; }
.main-img .badge-lg { position: absolute; top: 16px; left: 16px; background: var(--danger); color: #fff; padding: 6px 16px; border-radius: 6px; font-size: 14px; font-weight: 700; }
.thumb-row { display: flex; gap: 8px; margin-top: 10px; }
.thumb { width: 72px; height: 72px; border-radius: var(--radius-xs); border: 2px solid transparent; cursor: pointer; display: flex; align-items: center; justify-content: center; font-size: 28px; background: var(--bg); transition: all .15s; }
.thumb.active { border-color: var(--primary); }
.detail-info h1 { font-size: 22px; font-weight: 700; line-height: 1.4; }
.price-block { background: var(--primary-light); border-radius: var(--radius-sm); padding: 16px 20px; margin: 16px 0; display: flex; align-items: baseline; gap: 12px; }
.price-block .now { font-size: 32px; font-weight: 700; color: var(--danger); }
.price-block .old { font-size: 14px; color: var(--text-light); text-decoration: line-through; }
.price-block .tag-discount { padding: 3px 10px; background: var(--danger); color: #fff; border-radius: 4px; font-size: 12px; font-weight: 600; }
.meta-list { display: flex; gap: 24px; margin: 12px 0 20px; font-size: 13px; color: var(--text-secondary); }
.meta-list span { display: flex; align-items: center; gap: 4px; }
.qty-selector { display: flex; align-items: center; gap: 10px; margin: 20px 0; }
.qty-selector .label { font-size: 13px; font-weight: 600; }
.qty-box { display: flex; align-items: center; border: 1.5px solid var(--border); border-radius: var(--radius-xs); overflow: hidden; }
.qty-box button { width: 36px; height: 36px; border: none; background: none; font-size: 16px; cursor: pointer; color: var(--text); transition: all .1s; }
.qty-box button:hover { background: var(--bg); }
.qty-box span { width: 48px; text-align: center; font-size: 14px; font-weight: 500; border-left: 1px solid var(--border); border-right: 1px solid var(--border); height: 36px; line-height: 36px; }
.btn-row { display: flex; gap: 12px; margin-top: 24px; }
.btn-row .btn { height: 48px; padding: 0 32px; border: none; border-radius: var(--radius-xs); font-size: 15px; font-weight: 600; cursor: pointer; transition: all .15s; font-family: inherit; }
.btn-row .btn:active { transform: scale(.97); }
.btn-row .btn-cart { background: var(--bg-white); border: 2px solid var(--primary); color: var(--primary); }
.btn-row .btn-cart:hover { background: var(--primary-light); }
.btn-row .btn-buy { background: var(--primary-gradient); color: #fff; }
.btn-row .btn-buy:hover { box-shadow: 0 4px 15px rgba(232,93,58,.3); }
.detail-desc { background: var(--bg-white); border-radius: var(--radius); padding: 24px; margin-bottom: 24px; border: 1px solid var(--border-light); }
.detail-desc h3 { font-size: 16px; font-weight: 600; margin-bottom: 12px; }
.detail-desc p { font-size: 14px; color: var(--text-secondary); line-height: 1.8; }
.review-section { margin: 32px 0; }
.review-section h3 { font-size: 18px; font-weight: 600; margin-bottom: 16px; }
.review-summary { display: flex; align-items: center; gap: 24px; padding: 16px 20px; background: var(--bg-white); border-radius: var(--radius-sm); border: 1px solid var(--border-light); margin-bottom: 16px; }
.review-summary .score { text-align: center; }
.review-summary .score .num { font-size: 36px; font-weight: 700; color: var(--warning); }
.review-summary .score .stars { font-size: 16px; color: var(--warning); }
.review-summary .bars { flex: 1; display: flex; flex-direction: column; gap: 4px; }
.review-summary .bars .bar-row { display: flex; align-items: center; gap: 8px; font-size: 12px; }
.review-summary .bars .bar-row .bar-track { flex: 1; height: 6px; background: var(--bg); border-radius: 3px; overflow: hidden; }
.review-summary .bars .bar-row .bar-fill { height: 100%; background: var(--warning); border-radius: 3px; }
.review-item { padding: 16px 0; border-bottom: 1px solid var(--border-light); display: flex; gap: 12px; }
.review-item:last-child { border: none; }
.review-avatar { width: 36px; height: 36px; border-radius: 50%; display: flex; align-items: center; justify-content: center; color: #fff; font-size: 14px; font-weight: 700; flex-shrink: 0; }
.review-body { flex: 1; }
.review-top { display: flex; align-items: center; gap: 8px; margin-bottom: 4px; }
.review-name { font-size: 13px; font-weight: 500; }
.review-stars { font-size: 13px; color: var(--warning); }
.review-date { font-size: 11px; color: var(--text-light); margin-left: auto; }
.review-text { font-size: 14px; color: var(--text-secondary); line-height: 1.6; }
@media (max-width: 960px) { .detail-wrap { grid-template-columns: 1fr; } }
</style>
