<template>
  <div>
    <h2 class="page-title">订单详情 <span class="sub">{{ order?.orderNo }}</span></h2>

    <el-card shadow="never" class="detail-card">
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span>订单信息</span>
          <el-tag v-if="order" :type="statusTag(order.status)" size="large">{{ statusText(order.status) }}</el-tag>
        </div>
      </template>
      <el-descriptions :column="2" border v-if="order">
        <el-descriptions-item label="订单号">{{ order.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="下单时间">{{ order.createTime }}</el-descriptions-item>
        <el-descriptions-item label="支付方式">{{ order.paymentMethod === 'MOCK_PAY' ? '模拟支付' : order.paymentMethod === 'BALANCE' ? '余额支付' : '-' }}</el-descriptions-item>
        <el-descriptions-item label="支付时间">{{ order.payTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="收货人">{{ order.receiverName }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ order.receiverPhone }}</el-descriptions-item>
        <el-descriptions-item label="收货地址" :span="2">{{ order.receiverProvince }}{{ order.receiverCity }}{{ order.receiverDistrict }}{{ order.receiverDetail }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card shadow="never" class="detail-card">
      <template #header>商品明细</template>
      <el-table :data="items" border v-if="items.length">
        <el-table-column prop="productName" label="商品" min-width="180" />
        <el-table-column label="规格" width="160"><template #default="{ row }">{{ row.specInfo || '-' }}</template></el-table-column>
        <el-table-column label="单价" width="100"><template #default="{ row }">¥{{ row.price?.toFixed(2) }}</template></el-table-column>
        <el-table-column prop="quantity" label="数量" width="70" />
        <el-table-column label="小计" width="100"><template #default="{ row }">¥{{ row.totalPrice?.toFixed(2) }}</template></el-table-column>
      </el-table>
      <div v-if="order" style="margin-top:12px;text-align:right;font-size:14px;">
        <div>商品总金额：<strong>¥{{ order.totalAmount?.toFixed(2) }}</strong></div>
        <div>优惠：<span style="color:#EF4444;">-¥{{ order.discountAmount?.toFixed(2) }}</span></div>
        <div>运费：¥{{ order.freightAmount?.toFixed(2) }}</div>
        <div style="font-size:16px;margin-top:4px;">实付金额：<strong style="color:#E85D3A;">¥{{ order.payAmount?.toFixed(2) }}</strong></div>
      </div>
    </el-card>

    <el-card shadow="never" class="detail-card" v-if="order?.status === 'PENDING_DELIVERY'">
      <template #header>🚚 指派配送员</template>
      <div style="display:flex;align-items:center;gap:12px;">
        <el-select v-model="selectedCourier" placeholder="请选择配送员" style="width:250px">
          <el-option v-for="c in couriers" :key="c.id" :label="c.nickName + ' (' + c.phone + ')'" :value="c.id" />
        </el-select>
        <el-button type="primary" :loading="assigning" @click="handleAssign">确认指派</el-button>
      </div>
    </el-card>

    <div style="margin-top:16px;">
      <el-button @click="router.back()">返回列表</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getOrderDetail, assignCourier, getCourierList } from '@/api/admin'
import { ElMessage } from 'element-plus'

const route = useRoute(); const router = useRouter()
const order = ref<any>(null); const items = ref<any[]>([])
const couriers = ref<any[]>([]); const selectedCourier = ref<number | null>(null); const assigning = ref(false)

onMounted(async () => {
  const id = Number(route.params.id)
  try {
    const res = await getOrderDetail(id)
    order.value = res.data.order; items.value = res.data.items || []
  } catch { ElMessage.error('订单不存在'); router.push('/orders') }
  try { const res = await getCourierList({ page: 1, size: 100 }); couriers.value = res.data.records } catch { /* ignore */ }
})

async function handleAssign() {
  if (!selectedCourier.value) return ElMessage.warning('请选择配送员')
  assigning.value = true
  try {
    await assignCourier(order.value.id, selectedCourier.value)
    ElMessage.success('指派成功')
    // 刷新订单
    const res = await getOrderDetail(order.value.id)
    order.value = res.data.order
  } finally { assigning.value = false }
}

function statusTag(s: string) { const m: Record<string, string> = { PENDING_PAYMENT: 'warning', PENDING_DELIVERY: 'info', ASSIGNED: 'info', IN_TRANSIT: 'primary', DELIVERED: 'success', COMPLETED: 'default', CANCELLED: 'danger' }; return m[s] || 'info' }
function statusText(s: string) { const m: Record<string, string> = { PENDING_PAYMENT: '待支付', PENDING_DELIVERY: '待发货', ASSIGNED: '已指派', IN_TRANSIT: '配送中', DELIVERED: '已送达', COMPLETED: '已完成', CANCELLED: '已取消' }; return m[s] || s }
</script>
<style scoped>
.page-title { font-size: 20px; font-weight: 700; margin-bottom: 20px; color: #1a1a2e; }
.page-title .sub { font-size: 13px; font-weight: 400; color: #9e9eaf; }
.detail-card { margin-bottom: 16px; }
</style>
