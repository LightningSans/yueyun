<template>
  <div>
    <div class="section-header"><h2>购物车</h2><span class="more" v-if="cartItems.length">共 <strong>{{ itemCount }}</strong> 件商品</span></div>
    <div v-if="loading" class="loading-state">加载中...</div>
    <div v-else-if="!cartItems.length" class="empty-state"><div class="empty-icon">🛒</div><p>购物车空空如也</p><button class="btn-empty" @click="$router.push('/list')">去逛逛</button></div>
    <div v-else class="cart-layout">
      <div class="cart-list">
        <div v-for="(group, gi) in cartGroups" :key="gi" class="cart-shop-group">
          <div class="cart-shop-header" @click="toggleShop(gi)">
            <div class="check" :class="{ checked: shopChecked(gi) }"></div>
            <span class="shop-name">{{ group.shopGroup }}</span>
          </div>
          <div v-for="item in group.items" :key="item.id" class="cart-row">
            <div class="check" :class="{ checked: item.selected }" @click="toggleItem(item)"></div>
            <div class="cart-img">
              <img v-if="item.productImage" :src="item.productImage" class="cart-img-real" @error="e => e.target.style.display='none'" />
              <span v-else style="font-size:28px;">📦</span>
            </div>
            <div class="cart-info">
              <div class="cart-name" @click="$router.push('/detail/' + item.productId)">{{ item.productName }}</div>
              <div class="cart-price">¥{{ formatNum(item.price) }}</div>
            </div>
            <div class="cart-qty">
              <button @click="changeQty(item, -1)" :disabled="item.quantity <= 1">−</button>
              <span>{{ item.quantity }}</span>
              <button @click="changeQty(item, 1)">+</button>
            </div>
            <div class="cart-subtotal">¥{{ formatNum(item.price * item.quantity) }}</div>
            <button class="cart-delete" @click="removeItem(item)">✕</button>
          </div>
        </div>
      </div>
      <div class="cart-sidebar">
        <div class="cart-summary-card">
          <h3>订单摘要</h3>
          <div class="summary-row"><span>商品总额</span><span>¥{{ formatNum(totalAmount) }}</span></div>
          <div class="summary-row"><span>运费</span><span v-if="totalAmount >= 99 || totalAmount === 0">¥0 <span style="color:var(--success);font-size:12px;">免运费</span></span><span v-else>¥3.00</span></div>
          <div class="summary-row total"><span>实付金额</span><span class="amount">¥{{ formatNum(totalAmount >= 99 || totalAmount === 0 ? totalAmount : totalAmount + 3) }}</span></div>
          <button class="btn-checkout" :disabled="!selectedCount" @click="checkout">结算 ({{ selectedCount }})</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getCartList, updateCart, deleteCart } from '@/api'
import { ElMessage } from '@/utils/toast'

const router = useRouter()
const cartItems = ref<any[]>([])
const loading = ref(true)

const cartGroups = computed(() => {
  const map: Record<string, any[]> = {}
  for (const item of cartItems.value) {
    const key = '悦选商城'
    if (!map[key]) map[key] = []
    map[key].push(item)
  }
  return Object.entries(map).map(([shopGroup, items]) => ({ shopGroup, items }))
})
const itemCount = computed(() => cartItems.value.reduce((s, i) => s + i.quantity, 0))
const selectedItems = computed(() => cartItems.value.filter(i => i.selected))
const selectedCount = computed(() => selectedItems.value.reduce((s, i) => s + i.quantity, 0))
const totalAmount = computed(() => selectedItems.value.reduce((s, i) => s + i.price * i.quantity, 0))

onMounted(() => fetchData())
async function fetchData() { loading.value = true; try { const res = await getCartList(); cartItems.value = (res.data?.[0]?.items || []).map((i: any) => ({ ...i, selected: true })) } catch { /* ignore */ } finally { loading.value = false } }

async function toggleItem(item: any) {
  item.selected = !item.selected
  try { await updateCart({ id: item.id, selected: item.selected }) } catch { item.selected = !item.selected }
}
function toggleShop(gi: number) {
  const group = cartGroups.value[gi]
  const allSelected = group.items.every(i => i.selected)
  group.items.forEach(i => { i.selected = !allSelected; updateCart({ id: i.id, selected: !allSelected }).catch(() => {}) })
}
function shopChecked(gi: number) { return cartGroups.value[gi]?.items.every(i => i.selected) }

async function changeQty(item: any, delta: number) {
  const newQty = item.quantity + delta
  if (newQty < 1) return
  item.quantity = newQty
  try { await updateCart({ id: item.id, quantity: newQty }) } catch { item.quantity -= delta }
}
async function removeItem(item: any) {
  try { await deleteCart(item.id); cartItems.value = cartItems.value.filter(i => i.id !== item.id); ElMessage('已删除') } catch { /* ignore */ }
}
function checkout() {
  if (!selectedCount) return ElMessage('请选择商品')
  const ids = selectedItems.value.map(i => i.id)
  router.push({ path: '/checkout', query: { ids: ids.join(',') } })
}
function formatNum(n: any) { return Number(n).toFixed(2) }
</script>

<style scoped>
.section-header { display: flex; align-items: baseline; justify-content: space-between; margin-bottom: 16px; }
.section-header h2 { font-size: 22px; font-weight: 700; }
.section-header .more { font-size: 13px; color: var(--text-light); }
.loading-state { text-align: center; padding: 60px 0; color: var(--text-light); }
.empty-state { text-align: center; padding: 80px 20px; }
.empty-icon { font-size: 64px; opacity: .3; margin-bottom: 16px; }
.btn-empty { margin-top: 16px; padding: 10px 28px; border: none; border-radius: var(--radius-xs); background: var(--primary); color: #fff; font-size: 14px; cursor: pointer; font-family: inherit; }
.cart-layout { display: grid; grid-template-columns: 1fr 320px; gap: 24px; }
.cart-shop-group { background: var(--bg-white); border-radius: var(--radius); border: 1px solid var(--border-light); margin-bottom: 12px; overflow: hidden; }
.cart-shop-header { display: flex; align-items: center; gap: 10px; padding: 14px 18px; border-bottom: 1px solid var(--border-light); background: var(--bg); cursor: pointer; }
.cart-shop-header .check { width: 18px; height: 18px; border-radius: 50%; border: 2px solid var(--border); cursor: pointer; display: flex; align-items: center; justify-content: center; flex-shrink: 0; transition: all .15s; }
.cart-shop-header .check.checked { background: var(--primary); border-color: var(--primary); }
.cart-shop-header .check.checked::after { content: '✓'; color: #fff; font-size: 10px; font-weight: 700; }
.cart-shop-header .shop-name { font-size: 14px; font-weight: 600; }
.cart-row { display: flex; align-items: center; gap: 12px; padding: 14px 18px; border-bottom: 1px solid var(--border-light); }
.cart-row:last-child { border-bottom: none; }
.cart-row .check { width: 18px; height: 18px; border-radius: 50%; border: 2px solid var(--border); cursor: pointer; display: flex; align-items: center; justify-content: center; flex-shrink: 0; transition: all .15s; }
.cart-row .check.checked { background: var(--primary); border-color: var(--primary); }
.cart-row .check.checked::after { content: '✓'; color: #fff; font-size: 10px; font-weight: 700; }
.cart-img { width: 72px; height: 72px; border-radius: var(--radius-xs); display: flex; align-items: center; justify-content: center; background: var(--bg); flex-shrink: 0; overflow: hidden; }
.cart-img-real { width: 100%; height: 100%; object-fit: cover; }
.cart-info { flex: 1; min-width: 0; }
.cart-name { font-size: 14px; font-weight: 500; cursor: pointer; }
.cart-name:hover { color: var(--primary); }
.cart-price { font-size: 16px; font-weight: 700; color: var(--danger); margin-top: 4px; }
.cart-qty { display: flex; align-items: center; border: 1px solid var(--border); border-radius: 6px; overflow: hidden; }
.cart-qty button { width: 30px; height: 30px; border: none; background: none; font-size: 14px; cursor: pointer; color: var(--text); }
.cart-qty button:hover { background: var(--bg); }
.cart-qty button:disabled { opacity: .3; cursor: not-allowed; }
.cart-qty span { width: 36px; text-align: center; font-size: 13px; font-weight: 500; border-left: 1px solid var(--border); border-right: 1px solid var(--border); height: 30px; line-height: 30px; }
.cart-subtotal { width: 100px; text-align: right; font-size: 15px; font-weight: 600; color: var(--danger); }
.cart-delete { background: none; border: none; color: var(--text-light); cursor: pointer; font-size: 16px; padding: 8px; border-radius: 4px; }
.cart-delete:hover { color: var(--danger); background: #FEF2F2; }
.cart-summary-card { background: var(--bg-white); border-radius: var(--radius); border: 1px solid var(--border-light); padding: 20px; position: sticky; top: calc(var(--nav-h) + var(--subnav-h) + 20px); }
.cart-summary-card h3 { font-size: 16px; font-weight: 600; margin-bottom: 16px; padding-bottom: 12px; border-bottom: 1px solid var(--border-light); }
.summary-row { display: flex; justify-content: space-between; font-size: 14px; padding: 6px 0; color: var(--text-secondary); }
.summary-row.total { font-size: 16px; font-weight: 700; color: var(--text); padding-top: 12px; margin-top: 8px; border-top: 1px solid var(--border-light); }
.summary-row.total .amount { color: var(--danger); font-size: 22px; }
.btn-checkout { width: 100%; height: 46px; border: none; border-radius: var(--radius-xs); background: var(--primary-gradient); color: #fff; font-size: 15px; font-weight: 600; cursor: pointer; margin-top: 16px; transition: all .15s; font-family: inherit; }
.btn-checkout:hover { box-shadow: 0 4px 15px rgba(232,93,58,.3); }
.btn-checkout:disabled { opacity: .4; cursor: not-allowed; box-shadow: none; }
@media (max-width: 960px) { .cart-layout { grid-template-columns: 1fr; } }
</style>
