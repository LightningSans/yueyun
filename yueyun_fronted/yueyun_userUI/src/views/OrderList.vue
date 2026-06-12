<template>
  <div>
    <div class="section-header"><h2>我的订单</h2></div>
    <div class="order-tabs">
      <button v-for="tab in tabs" :key="tab.key" class="order-tab" :class="{ active: activeTab === tab.key }" @click="switchTab(tab.key)">{{ tab.label }}</button>
    </div>
    <div v-if="loading" class="loading-state">加载中...</div>
    <div v-else-if="!orders.length" class="empty-state"><div class="empty-icon">📋</div><p>暂无订单</p></div>
    <div v-else>
      <div v-for="order in orders" :key="order.id" class="order-card" @click="$router.push('/orders/' + order.id)">
        <div class="order-top">
          <span class="order-no">订单号：{{ order.orderNo }}</span>
          <span class="order-status" :style="{ color: statusColor(order.status) }">{{ order.statusText }}</span>
        </div>
        <div class="order-items">
          <div v-for="item in order.orderItems" :key="item.productId" class="order-item-row">
            <div class="order-item-img">
              <img v-if="item.productImage" :src="item.productImage" class="order-item-img-real" @error="e => e.target.style.display='none'" />
              <span v-else style="font-size:22px;">📦</span>
            </div>
            <div class="order-item-info">
              <div class="order-item-name">{{ item.productName }}</div>
              <div class="order-item-price">¥{{ item.price.toFixed(2) }} x {{ item.quantity }}</div>
            </div>
          </div>
        </div>
        <div class="order-bottom">
          <span class="order-total">共 {{ order.orderItems?.length }} 件 · 合计 <strong>¥{{ order.payAmount.toFixed(2) }}</strong></span>
          <div class="order-actions">
            <button v-if="order.status === 'PENDING_PAYMENT' || order.status === 'PENDING_DELIVERY'" class="btn primary" @click.stop="cancelOrderAction(order)">{{ order.status === 'PENDING_DELIVERY' ? '退单' : '取消' }}</button>
            <button v-if="order.status === 'DELIVERED'" class="btn primary" @click.stop="confirmReceive(order)">确认收货</button>
            <button v-if="order.status === 'COMPLETED'" class="btn primary" @click.stop="$router.push('/orders/' + order.id)">去评价</button>
            <button v-if="order.status === 'COMPLETED' || order.status === 'CANCELLED' || order.status === 'REFUNDING'" class="btn danger" @click.stop="removeOrder(order)">删除</button>
            <button class="btn" @click.stop="$router.push('/orders/' + order.id)">详情</button>
          </div>
        </div>
      </div>
      <div v-if="pages > 1" class="pagination">
        <button :disabled="query.page <= 1" @click="query.page--; fetchData()">上一页</button>
        <span>{{ query.page }} / {{ pages }}</span>
        <button :disabled="query.page >= pages" @click="query.page++; fetchData()">下一页</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getOrderList, cancelOrder, confirmOrder, deleteOrder } from '@/api'
import { ElMessage } from '@/utils/toast'

const orders = ref<any[]>([])
const loading = ref(true)
const pages = ref(1)
const activeTab = ref('')
const tabs = [
  { key: '', label: '全部' }, { key: 'PENDING_PAYMENT', label: '待支付' },
  { key: 'PENDING_DELIVERY', label: '待发货' }, { key: 'ASSIGNED', label: '已指派' },
  { key: 'IN_TRANSIT', label: '配送中' }, { key: 'DELIVERED', label: '已送达' },
  { key: 'COMPLETED', label: '已完成' }, { key: 'REFUNDING', label: '退款中' }, { key: 'CANCELLED', label: '已取消' },
]

const query = reactive({ page: 1, size: 10, status: '' as string | undefined })

onMounted(() => fetchData())
async function fetchData() { loading.value = true; try { const res = await getOrderList(query); orders.value = res.data?.records || []; pages.value = res.data?.pages || 1 } catch { /* ignore */ } finally { loading.value = false } }

function switchTab(key: string) { activeTab.value = key; query.status = key || undefined; query.page = 1; fetchData() }
function statusColor(s: string) { const m: Record<string, string> = { PENDING_PAYMENT: '#F59E0B', CANCELLED: '#EF4444', COMPLETED: '#22C55E', DELIVERED: '#22C55E', IN_TRANSIT: '#E85D3A', REFUNDING: '#8B5CF6' }; return m[s] || 'var(--text-secondary)' }
async function cancelOrderAction(order: any) { try { await cancelOrder(order.id, order.status === 'PENDING_DELIVERY' ? '用户退单' : undefined); ElMessage('已取消'); fetchData() } catch { /* ignore */ } }
async function confirmReceive(order: any) { try { await confirmOrder(order.id); ElMessage('已确认收货'); fetchData() } catch { /* ignore */ } }
async function removeOrder(order: any) { if (!confirm('确定删除该订单？删除后不可恢复')) return; try { await deleteOrder(order.id); ElMessage('已删除'); fetchData() } catch { /* ignore */ } }
</script>

<style scoped>
.section-header { margin-bottom: 16px; }
.section-header h2 { font-size: 22px; font-weight: 700; }
.loading-state { text-align: center; padding: 60px 0; color: var(--text-light); }
.empty-state { text-align: center; padding: 80px 20px; }
.empty-icon { font-size: 64px; opacity: .3; margin-bottom: 16px; }
.order-tabs { display: flex; gap: 0; margin-bottom: 20px; border-bottom: 2px solid var(--border); overflow-x: auto; }
.order-tab { padding: 12px 24px; font-size: 14px; font-weight: 500; cursor: pointer; color: var(--text-light); border: none; background: none; border-bottom: 2px solid transparent; margin-bottom: -2px; transition: all .15s; font-family: inherit; white-space: nowrap; }
.order-tab:hover { color: var(--text); }
.order-tab.active { color: var(--primary); border-bottom-color: var(--primary); font-weight: 600; }
.order-card { background: var(--bg-white); border-radius: var(--radius); border: 1px solid var(--border-light); margin-bottom: 12px; overflow: hidden; cursor: pointer; transition: all .15s; }
.order-card:hover { box-shadow: var(--shadow); }
.order-top { display: flex; justify-content: space-between; align-items: center; padding: 14px 20px; background: var(--bg); border-bottom: 1px solid var(--border-light); }
.order-no { font-size: 13px; color: var(--text-secondary); font-weight: 500; }
.order-status { font-size: 13px; font-weight: 600; }
.order-items { padding: 14px 20px; }
.order-item-row { display: flex; gap: 10px; padding: 6px 0; }
.order-item-img { width: 52px; height: 52px; border-radius: 6px; display: flex; align-items: center; justify-content: center; background: var(--bg); flex-shrink: 0; overflow: hidden; }
.order-item-img-real { width: 100%; height: 100%; object-fit: cover; }
.order-item-info { flex: 1; min-width: 0; display: flex; justify-content: space-between; align-items: center; }
.order-item-name { font-size: 14px; font-weight: 500; }
.order-item-price { font-size: 13px; color: var(--text-light); }
.order-bottom { display: flex; justify-content: space-between; align-items: center; padding: 12px 20px; border-top: 1px solid var(--border-light); }
.order-total { font-size: 13px; color: var(--text-secondary); }
.order-total strong { font-size: 16px; color: var(--text); }
.order-actions { display: flex; gap: 8px; }
.order-actions .btn { padding: 6px 18px; border-radius: 6px; border: 1px solid var(--border); font-size: 12px; cursor: pointer; background: var(--bg-white); font-family: inherit; transition: all .1s; }
.order-actions .btn:hover { border-color: var(--primary); color: var(--primary); }
.order-actions .btn.primary { background: var(--primary); color: #fff; border-color: var(--primary); }
.order-actions .btn.primary:hover { background: var(--primary-dark); }
.order-actions .btn.danger { background: #EF4444; color: #fff; border-color: #EF4444; }
.order-actions .btn.danger:hover { background: #DC2626; }
.pagination { display: flex; justify-content: center; align-items: center; gap: 16px; margin-top: 32px; }
.pagination button { padding: 8px 20px; border: 1px solid var(--border); border-radius: 6px; background: var(--bg-white); cursor: pointer; font-family: inherit; font-size: 13px; }
.pagination button:hover { border-color: var(--primary); color: var(--primary); }
.pagination button:disabled { opacity: .4; cursor: not-allowed; }
</style>
