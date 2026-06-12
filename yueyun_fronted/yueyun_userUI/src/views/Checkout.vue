<template>
  <div>
    <div class="section-header"><h2>确认订单</h2></div>
    <div v-if="loading" class="loading-state">加载中...</div>
    <div v-else class="checkout-wrap">
      <div class="checkout-card">
        <div class="card-title">📍 收货地址</div>
        <div class="card-body">
          <div v-if="!addresses.length" class="no-addr">请先添加收货地址</div>
          <div v-for="addr in addresses" :key="addr.id" class="address-display" :class="{ selected: selectedAddr?.id === addr.id }" @click="selectedAddr = addr">
            <span class="addr-icon">📍</span>
            <div class="addr-info">
              <div class="addr-name">{{ addr.receiverName }} <span class="addr-phone">{{ addr.receiverPhone }}</span><span v-if="addr.isDefault" class="tag-default">默认</span></div>
              <div class="addr-detail">{{ addr.provinceName }}{{ addr.cityName }}{{ addr.districtName }} {{ addr.detailAddress }}</div>
            </div>
          </div>
        </div>
      </div>
      <div class="checkout-card">
        <div class="card-title">📦 商品信息</div>
        <div class="card-body">
          <div v-for="item in items" :key="item.id" class="checkout-item">
            <div class="checkout-img">
              <img v-if="item.productImage" :src="item.productImage" class="checkout-img-real" @error="e => e.target.style.display='none'" />
              <span v-else style="font-size:24px;">📦</span>
            </div>
            <div class="checkout-info"><div class="checkout-name">{{ item.productName }}</div></div>
            <div class="checkout-right"><div class="checkout-price">¥{{ item.price.toFixed(2) }}</div><div class="checkout-qty">x{{ item.quantity }}</div></div>
          </div>
        </div>
      </div>
      <div class="checkout-card">
        <div class="card-title">💳 支付方式</div>
        <div class="card-body">
          <div class="pay-method">
            <div class="pay-option" :class="{ active: payMethod === 'MOCK_PAY' }" @click="payMethod = 'MOCK_PAY'">
              <div class="pay-icon">💰</div><div class="pay-name">模拟支付</div><div class="pay-desc">快速到账</div>
            </div>
            <div class="pay-option" :class="{ active: payMethod === 'BALANCE' }" @click="payMethod = 'BALANCE'">
              <div class="pay-icon">💳</div><div class="pay-name">余额支付</div><div class="pay-desc">账户余额</div>
            </div>
          </div>
        </div>
      </div>
      <div class="price-line"><span class="lbl">商品金额</span><span>¥{{ items.reduce((s, i) => s + i.price * i.quantity, 0).toFixed(2) }}</span></div>
      <div class="price-line"><span class="lbl">运费</span><span>{{ freight > 0 ? '¥' + freight.toFixed(2) : '免运费' }}</span></div>
      <div class="price-line total"><span class="lbl">实付金额</span><span class="val">¥{{ payAmount.toFixed(2) }}</span></div>
      <div class="checkout-bar"><span class="total-text">合计：<strong>¥{{ payAmount.toFixed(2) }}</strong></span><button class="btn-submit" :disabled="submitting" @click="submitOrder">{{ submitting ? '提交中...' : '提交订单' }}</button></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getCartList, getAddressList, createOrder, payOrder } from '@/api'
import { ElMessage } from '@/utils/toast'

const router = useRouter()
const route = useRoute()
const items = ref<any[]>([])
const addresses = ref<any[]>([])
const selectedAddr = ref<any>(null)
const payMethod = ref('MOCK_PAY')
const loading = ref(true)
const submitting = ref(false)

const freight = computed(() => {
  const total = items.value.reduce((s, i) => s + i.price * i.quantity, 0)
  return total >= 99 || total === 0 ? 0 : 3
})
const payAmount = computed(() => {
  const total = items.value.reduce((s, i) => s + i.price * i.quantity, 0)
  return total + freight.value
})

onMounted(async () => {
  try {
    const ids = (route.query.ids as string || '').split(',').map(Number).filter(Boolean)
    if (!ids.length) { router.push('/cart'); return }
    const [cartRes, addrRes] = await Promise.all([getCartList(), getAddressList()])
    const allItems = cartRes.data?.[0]?.items || []
    items.value = allItems.filter((i: any) => ids.includes(i.id))
    addresses.value = addrRes.data || []
    selectedAddr.value = addresses.value.find((a: any) => a.isDefault) || addresses.value[0]
  } catch { /* ignore */ }
  finally { loading.value = false }
})

async function submitOrder() {
  if (!selectedAddr.value) { ElMessage('请选择收货地址'); return }
  if (!items.value.length) { ElMessage('没有可结算的商品'); return }
  submitting.value = true
  try {
    const orderRes = await createOrder({ addressId: selectedAddr.value.id, cartItemIds: items.value.map(i => i.id) })
    const orderId = orderRes.data.orderId
    await payOrder({ orderId, paymentMethod: payMethod.value })
    router.push('/pay/success?orderId=' + orderId + '&amount=' + payAmount.value.toFixed(2))
  } catch (e: any) { ElMessage(e.message || '下单失败') }
  finally { submitting.value = false }
}
</script>

<style scoped>
.section-header { margin-bottom: 16px; }
.section-header h2 { font-size: 22px; font-weight: 700; }
.loading-state { text-align: center; padding: 60px 0; color: var(--text-light); }
.checkout-wrap { max-width: 860px; }
.checkout-card { background: var(--bg-white); border-radius: var(--radius); border: 1px solid var(--border-light); margin-bottom: 16px; overflow: hidden; }
.card-title { padding: 14px 20px; font-size: 15px; font-weight: 600; border-bottom: 1px solid var(--border-light); display: flex; align-items: center; gap: 8px; }
.card-body { padding: 16px 20px; }
.no-addr { color: var(--text-light); font-size: 14px; padding: 8px 0; }
.address-display { display: flex; align-items: flex-start; gap: 12px; cursor: pointer; padding: 12px; border-radius: 8px; border: 2px solid transparent; transition: all .15s; }
.address-display.selected { border-color: var(--primary); background: var(--primary-light); }
.address-display .addr-icon { font-size: 22px; }
.address-display .addr-info { flex: 1; }
.address-display .addr-name { font-size: 15px; font-weight: 600; }
.address-display .addr-phone { font-size: 13px; color: var(--text-secondary); margin-left: 8px; font-weight: 400; }
.address-display .addr-detail { font-size: 13px; color: var(--text-secondary); margin-top: 2px; }
.address-display .tag-default { font-size: 10px; background: var(--primary-light); color: var(--primary); padding: 1px 8px; border-radius: 4px; font-weight: 500; margin-left: 6px; }
.checkout-item { display: flex; gap: 12px; padding: 10px 0; border-bottom: 1px solid var(--border-light); align-items: center; }
.checkout-item:last-child { border-bottom: none; }
.checkout-img { width: 56px; height: 56px; border-radius: 6px; display: flex; align-items: center; justify-content: center; background: var(--bg); flex-shrink: 0; overflow: hidden; }
.checkout-img-real { width: 100%; height: 100%; object-fit: cover; }
.checkout-info { flex: 1; }
.checkout-name { font-size: 14px; font-weight: 500; }
.checkout-right { text-align: right; }
.checkout-price { font-size: 14px; font-weight: 600; }
.checkout-qty { font-size: 12px; color: var(--text-light); }
.pay-method { display: flex; gap: 12px; }
.pay-option { flex: 1; padding: 14px; border: 2px solid var(--border); border-radius: var(--radius-sm); cursor: pointer; text-align: center; transition: all .15s; }
.pay-option:hover { border-color: var(--primary); }
.pay-option.active { border-color: var(--primary); background: var(--primary-light); }
.pay-option .pay-icon { font-size: 24px; }
.pay-option .pay-name { font-size: 13px; font-weight: 500; margin-top: 4px; }
.pay-option .pay-desc { font-size: 11px; color: var(--text-light); margin-top: 2px; }
.price-line { display: flex; justify-content: space-between; padding: 6px 0; font-size: 14px; }
.price-line .lbl { color: var(--text-secondary); }
.price-line.total { font-size: 18px; font-weight: 700; padding-top: 12px; margin-top: 8px; border-top: 1px solid var(--border-light); }
.price-line.total .val { color: var(--danger); font-size: 24px; }
.checkout-bar { display: flex; align-items: center; justify-content: flex-end; gap: 16px; padding: 16px 0; }
.checkout-bar .total-text { font-size: 15px; color: var(--text-secondary); }
.checkout-bar .total-text strong { font-size: 24px; color: var(--danger); font-weight: 700; }
.btn-submit { height: 48px; padding: 0 40px; border: none; border-radius: var(--radius-xs); background: var(--primary-gradient); color: #fff; font-size: 16px; font-weight: 600; cursor: pointer; transition: all .15s; font-family: inherit; }
.btn-submit:hover { box-shadow: 0 4px 15px rgba(232,93,58,.3); }
.btn-submit:disabled { opacity: .5; cursor: not-allowed; }
</style>
