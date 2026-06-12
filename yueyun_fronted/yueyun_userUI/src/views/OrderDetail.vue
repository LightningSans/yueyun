<template>
  <div v-if="loading" class="loading-state">加载中...</div>
  <div v-else-if="order" class="order-detail-wrap">
    <div class="od-card">
      <div class="od-header">📋 订单信息</div>
      <div class="od-body">
        <div class="order-timeline">
          <div class="tl-step" :class="{ active: step >= 0, current: currentStatus === 'PENDING_PAYMENT' }">
            <div class="tl-dot"></div><div class="tl-line"></div>
            <div class="tl-info"><div class="tl-title">下单</div><div class="tl-time">{{ formatDate(order.createTime) }}</div></div>
          </div>
          <div class="tl-step" :class="{ active: step >= 1, current: currentStatus === 'PENDING_DELIVERY' }">
            <div class="tl-dot"></div><div class="tl-line"></div>
            <div class="tl-info"><div class="tl-title">{{ order.payTime ? '已支付' : '待支付' }}</div><div class="tl-time">{{ formatDate(order.payTime) }}</div></div>
          </div>
          <div class="tl-step" :class="{ active: step >= 2, current: currentStatus === 'ASSIGNED' || currentStatus === 'IN_TRANSIT' }">
            <div class="tl-dot"></div><div class="tl-line"></div>
            <div class="tl-info"><div class="tl-title">{{ order.deliveryTime ? '已发货' : '待发货' }}</div><div class="tl-time">{{ formatDate(order.deliveryTime) }}</div></div>
          </div>
          <div class="tl-step" :class="{ active: step >= 3, current: currentStatus === 'DELIVERED' || currentStatus === 'COMPLETED' }">
            <div class="tl-dot"></div><div class="tl-line"></div>
            <div class="tl-info"><div class="tl-title">{{ order.status === 'COMPLETED' ? '已完成' : (order.status === 'DELIVERED' ? '已送达' : '配送中') }}</div></div>
          </div>
        </div>
      </div>
    </div>
    <div class="od-card">
      <div class="od-header">📍 收货地址</div>
      <div class="od-body">
        <div class="od-row"><span class="lbl">收货人</span><span>{{ order.receiverName }} {{ order.receiverPhone }}</span></div>
        <div class="od-row"><span class="lbl">地址</span><span>{{ order.receiverProvince }}{{ order.receiverCity }}{{ order.receiverDistrict }} {{ order.receiverDetail }}</span></div>
      </div>
    </div>
    <div class="od-card">
      <div class="od-header">📦 商品信息</div>
      <div class="od-body">
        <div v-for="item in order.orderItems" :key="item.productId" class="od-item">
          <div class="od-item-img">
            <img v-if="item.productImage" :src="item.productImage" class="od-item-img-real" @error="e => e.target.style.display='none'" />
            <span v-else style="font-size:22px;">📦</span>
          </div>
          <div class="od-item-info"><div class="od-item-name">{{ item.productName }}</div></div>
          <div class="od-item-right">¥{{ item.price.toFixed(2) }} x {{ item.quantity }}</div>
        </div>
      </div>
    </div>
    <div class="od-card">
      <div class="od-header">💰 金额明细</div>
      <div class="od-body">
        <div class="od-row"><span class="lbl">商品金额</span><span>¥{{ order.totalAmount.toFixed(2) }}</span></div>
        <div class="od-row"><span class="lbl">运费</span><span>{{ order.freightAmount > 0 ? '¥' + order.freightAmount.toFixed(2) : '免运费' }}</span></div>
        <div class="od-row total"><span class="lbl">实付金额</span><span class="val">¥{{ order.payAmount.toFixed(2) }}</span></div>
      </div>
    </div>
    <div class="od-actions">
      <button v-if="order.status === 'PENDING_PAYMENT'" class="btn primary" @click="payNow">立即支付</button>
      <button v-if="order.status === 'PENDING_DELIVERY'" class="btn danger" @click="cancelOrderFun">退单退款</button>
      <button v-if="order.status === 'DELIVERED'" class="btn primary" @click="confirmReceive">确认收货</button>
      <button v-if="order.status === 'COMPLETED' && !showReview" class="btn primary" @click="openReview">去评价</button>
      <button v-if="order.status === 'COMPLETED' || order.status === 'CANCELLED' || order.status === 'REFUNDING'" class="btn danger" @click="removeOrder">删除订单</button>
      <button class="btn" @click="$router.push('/orders')">返回订单列表</button>
    </div>

    <!-- ═══ 评价弹窗 ═══ -->
    <div v-if="showReview" class="modal-overlay" @click.self="showReview = false">
      <div class="modal-box">
        <div class="modal-header"><h3>评价商品</h3><button class="modal-close" @click="showReview = false">✕</button></div>
        <div class="modal-body">
          <div v-for="(item, idx) in order.orderItems" :key="item.productId" class="review-item-block">
            <div class="review-item-header">
              <span class="review-product-name">{{ item.productName }}</span>
              <div class="star-rating">
                <span v-for="s in 5" :key="s" class="star" :class="{ filled: s <= (reviewForms[idx]?.rating || 5) }" @click="reviewForms[idx].rating = s">★</span>
                <span class="rating-text">{{ ['', '很差', '较差', '一般', '满意', '非常满意'][reviewForms[idx]?.rating || 5] }}</span>
              </div>
            </div>
            <textarea class="review-textarea" v-model="reviewForms[idx].content" placeholder="说说使用感受..." maxlength="500"></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-cancel" @click="showReview = false">取消</button>
          <button class="btn btn-primary" :disabled="submitting" @click="submitReviews">{{ submitting ? '提交中...' : '提交评价' }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getOrderDetail, payOrder, confirmOrder, createReview, cancelOrder, deleteOrder } from '@/api'
import { ElMessage } from '@/utils/toast'
import { formatDate } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const order = ref<any>(null)
const loading = ref(true)
const currentStatus = ref('')
const showReview = ref(false)
const submitting = ref(false)
const reviewForms = ref<any[]>([])

const step = computed(() => {
  const m: Record<string, number> = { PENDING_PAYMENT: 0, PENDING_DELIVERY: 1, ASSIGNED: 2, IN_TRANSIT: 2, DELIVERED: 3, COMPLETED: 3, CANCELLED: -1 }
  return m[currentStatus.value] ?? -1
})

onMounted(async () => {
  try {
    const res = await getOrderDetail(Number(route.params.id))
    order.value = res.data
    currentStatus.value = res.data.status
  } catch { /* ignore */ }
  finally { loading.value = false }
})

async function payNow() {
  try {
    await payOrder({ orderId: order.value.id, paymentMethod: 'MOCK_PAY' })
    ElMessage('支付成功')
    currentStatus.value = 'PENDING_DELIVERY'
    order.value.status = 'PENDING_DELIVERY'
    order.value.payTime = new Date().toISOString()
    order.value.statusText = '待发货'
  } catch (e: any) { ElMessage(e.message || '支付失败') }
}
async function cancelOrderFun() {
  if (!confirm('确定退单退款？')) return
  try { await cancelOrder(order.value.id, '用户申请退单'); ElMessage('退单成功'); currentStatus.value = 'CANCELLED'; order.value.status = 'CANCELLED' } catch { /* ignore */ }
}
async function confirmReceive() {
  try { await confirmOrder(order.value.id); ElMessage('已确认收货'); currentStatus.value = 'COMPLETED'; order.value.status = 'COMPLETED' } catch { /* ignore */ }
}
async function removeOrder() {
  if (!confirm('确定删除该订单？删除后不可恢复')) return
  try { await deleteOrder(order.value.id); ElMessage('已删除'); currentStatus.value = 'DELETED'; order.value.status = 'DELETED' } catch { /* ignore */ }
}
function openReview() {
  reviewForms.value = (order.value.orderItems || []).map((item: any) => ({
    productId: item.productId,
    orderId: order.value.id,
    rating: 5,
    content: '',
    isAnonymous: 0   // 是否匿名: 0-否 1-是
  }))
  showReview.value = true
}
async function submitReviews() {
  submitting.value = true
  try {
    for (const form of reviewForms.value) {
      await createReview(form)
    }
    ElMessage('评价成功，感谢您的反馈！')
    showReview.value = false
  } catch (e: any) {
    ElMessage(e.message || '提交失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.loading-state { text-align: center; padding: 60px 0; color: var(--text-light); }
.order-detail-wrap { max-width: 860px; }
.od-card { background: var(--bg-white); border-radius: var(--radius); border: 1px solid var(--border-light); margin-bottom: 16px; overflow: hidden; }
.od-header { padding: 14px 20px; font-size: 15px; font-weight: 600; border-bottom: 1px solid var(--border-light); display: flex; align-items: center; gap: 8px; }
.od-body { padding: 16px 20px; }
.od-row { display: flex; padding: 6px 0; font-size: 14px; }
.od-row .lbl { width: 80px; color: var(--text-light); flex-shrink: 0; }
.od-row.total { font-size: 16px; font-weight: 700; padding-top: 12px; margin-top: 8px; border-top: 1px solid var(--border-light); }
.od-row.total .val { color: var(--danger); }
.od-item { display: flex; gap: 12px; padding: 8px 0; border-bottom: 1px solid var(--border-light); align-items: center; }
.od-item:last-child { border-bottom: none; }
.od-item-img { width: 52px; height: 52px; border-radius: 6px; display: flex; align-items: center; justify-content: center; background: var(--bg); flex-shrink: 0; overflow: hidden; }
.od-item-img-real { width: 100%; height: 100%; object-fit: cover; }
.od-item-info { flex: 1; }
.od-item-name { font-size: 14px; font-weight: 500; }
.od-item-right { font-size: 13px; color: var(--text-light); }
.order-timeline { display: flex; flex-direction: column; gap: 0; }
.tl-step { display: flex; gap: 14px; padding-bottom: 20px; position: relative; }
.tl-step:last-child { padding-bottom: 0; }
.tl-dot { width: 12px; height: 12px; border-radius: 50%; background: var(--border); flex-shrink: 0; margin-top: 3px; position: relative; z-index: 1; }
.tl-step.active .tl-dot { background: var(--success); box-shadow: 0 0 0 4px rgba(34,197,94,.15); }
.tl-step.current .tl-dot { background: var(--primary); box-shadow: 0 0 0 4px rgba(232,93,58,.15); }
.tl-line { position: absolute; left: 5.5px; top: 20px; bottom: 0; width: 2px; background: var(--border); }
.tl-step:last-child .tl-line { display: none; }
.tl-info { flex: 1; }
.tl-title { font-size: 14px; font-weight: 500; }
.tl-time { font-size: 12px; color: var(--text-light); margin-top: 2px; }
.tl-step.current .tl-title { color: var(--primary); font-weight: 600; }
.od-actions { display: flex; gap: 12px; margin-top: 24px; }
.od-actions .btn { padding: 10px 28px; border-radius: 6px; border: 1px solid var(--border); font-size: 14px; font-weight: 500; cursor: pointer; font-family: inherit; background: var(--bg-white); }
.od-actions .btn:hover { border-color: var(--primary); color: var(--primary); }
.od-actions .btn.primary { background: var(--primary); color: #fff; border-color: var(--primary); }
.od-actions .btn.primary:hover { background: var(--primary-dark); }
.od-actions .btn.danger { background: #EF4444; color: #fff; border-color: #EF4444; }
.od-actions .btn.danger:hover { background: #DC2626; }

/* ─── 评价弹窗 ─── */
.review-item-block { padding: 12px 0; border-bottom: 1px solid var(--border-light); }
.review-item-block:last-child { border-bottom: none; }
.review-item-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.review-product-name { font-size: 14px; font-weight: 500; }
.star-rating { display: flex; align-items: center; gap: 2px; }
.star { font-size: 22px; cursor: pointer; color: var(--border); transition: color .15s; }
.star.filled { color: #F59E0B; }
.star:hover { color: #F59E0B; }
.rating-text { font-size: 12px; color: var(--text-light); margin-left: 6px; min-width: 56px; }
.review-textarea { width: 100%; height: 72px; border: 1.5px solid var(--border); border-radius: var(--radius-xs); padding: 10px 12px; font-size: 13px; font-family: inherit; resize: none; outline: none; box-sizing: border-box; }
.review-textarea:focus { border-color: var(--primary); box-shadow: 0 0 0 3px rgba(232,93,58,.1); }
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,.4); z-index: 200; display: flex; align-items: center; justify-content: center; backdrop-filter: blur(4px); }
.modal-box { background: var(--bg-white); border-radius: var(--radius); width: 480px; max-height: 80vh; overflow-y: auto; box-shadow: 0 20px 60px rgba(0,0,0,.15); }
.modal-header { padding: 18px 20px; border-bottom: 1px solid var(--border); display: flex; align-items: center; justify-content: space-between; }
.modal-header h3 { font-size: 16px; font-weight: 600; }
.modal-close { width: 30px; height: 30px; border: none; background: none; font-size: 18px; cursor: pointer; color: var(--text-light); border-radius: 4px; }
.modal-close:hover { background: var(--bg); }
.modal-body { padding: 20px; }
.modal-footer { padding: 12px 20px; border-top: 1px solid var(--border); display: flex; justify-content: flex-end; gap: 8px; }
.btn { padding: 8px 20px; border-radius: 6px; font-size: 13px; font-weight: 500; cursor: pointer; font-family: inherit; }
.btn-cancel { border: 1px solid var(--border); background: var(--bg-white); }
.btn-primary { background: var(--primary); color: #fff; border: none; }
.btn-primary:hover { background: var(--primary-dark); }
.btn-primary:disabled { opacity: .5; cursor: not-allowed; }
</style>
