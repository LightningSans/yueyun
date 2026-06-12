<template>
  <div>
    <h2 class="page-title">订单管理 <span class="sub">查看 / 处理订单</span></h2>

    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="query" size="small">
        <el-form-item label="订单号"><el-input v-model="query.orderNo" placeholder="订单号" clearable style="width:160px" /></el-form-item>
        <el-form-item label="用户名"><el-input v-model="query.username" placeholder="用户名" clearable style="width:120px" /></el-form-item>
        <el-form-item label="状态"><el-select v-model="query.status" placeholder="全部状态" clearable style="width:130px">
          <el-option v-for="s in orderStatuses" :key="s.value" :label="s.label" :value="s.value" />
        </el-select></el-form-item>
        <el-form-item><el-button type="primary" @click="handleSearch">搜索</el-button><el-button @click="handleReset">重置</el-button></el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table :data="tableData" stripe v-loading="loading" @row-click="goDetail">
        <el-table-column prop="orderNo" label="订单号" min-width="150" />
        <el-table-column prop="receiverName" label="用户" width="80" />
        <el-table-column label="实付" width="100"><template #default="{ row }"><strong>¥{{ row.payAmount?.toFixed(2) }}</strong></template></el-table-column>
        <el-table-column prop="paymentMethod" label="支付" width="80">
          <template #default="{ row }"><el-tag :type="row.paymentMethod === 'MOCK_PAY' ? 'info' : 'warning'" size="small">{{ row.paymentMethod === 'MOCK_PAY' ? '模拟' : row.paymentMethod === 'BALANCE' ? '余额' : '-' }}</el-tag></template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }"><el-tag :type="statusTag(row.status)" size="small">{{ statusText(row.status) }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" width="130" />
        <el-table-column label="配送" width="70"><template #default="{ row }">{{ row.courierId ? '已指派' : '未指派' }}</template></el-table-column>
        <el-table-column label="操作" width="145">
          <template #default="{ row }">
            <el-button text type="primary" size="small" style="padding:0 5px" @click.stop="goDetail(row)">详情</el-button>
            <el-button v-if="row.status === 'PENDING_DELIVERY'" text type="primary" size="small" style="padding:0 5px" @click.stop="handleAssign(row)">指派</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="query.page" v-model:page-size="query.size" :total="total" layout="total, sizes, prev, pager, next" background @change="fetchData" style="margin-top:16px;justify-content:flex-end" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { getOrderList } from '@/api/admin'

const router = useRouter()
const loading = ref(false); const tableData = ref<any[]>([]); const total = ref(0)
const query = reactive({ page: 1, size: 10, orderNo: '', username: '', status: '' })
const orderStatuses = [
  { value: 'PENDING_PAYMENT', label: '待支付' }, { value: 'PENDING_DELIVERY', label: '待发货' },
  { value: 'ASSIGNED', label: '已指派' }, { value: 'IN_TRANSIT', label: '配送中' },
  { value: 'DELIVERED', label: '已送达' }, { value: 'COMPLETED', label: '已完成' }, { value: 'CANCELLED', label: '已取消' }
]

onMounted(() => fetchData())
async function fetchData() { loading.value = true; try { const res = await getOrderList(query); tableData.value = res.data.records; total.value = res.data.total } finally { loading.value = false } }
function handleSearch() { query.page = 1; fetchData() }
function handleReset() { query.orderNo = ''; query.username = ''; query.status = ''; query.page = 1; fetchData() }
function goDetail(row: any) { router.push('/orders/' + row.id) }
function statusTag(s: string) { const m: Record<string, string> = { PENDING_PAYMENT: 'warning', PENDING_DELIVERY: 'info', ASSIGNED: 'info', IN_TRANSIT: 'primary', DELIVERED: 'success', COMPLETED: 'default', CANCELLED: 'danger' }; return m[s] || 'info' }
function statusText(s: string) { const m: Record<string, string> = { PENDING_PAYMENT: '待支付', PENDING_DELIVERY: '待发货', ASSIGNED: '已指派', IN_TRANSIT: '配送中', DELIVERED: '已送达', COMPLETED: '已完成', CANCELLED: '已取消' }; return m[s] || s }

function handleAssign(row: any) {
  router.push('/orders/' + row.id)
}
</script>
<style scoped>
.page-title { font-size: 20px; font-weight: 700; margin-bottom: 20px; color: #1a1a2e; }
.page-title .sub { font-size: 13px; font-weight: 400; color: #9e9eaf; }
.search-card { margin-bottom: 16px; }
</style>
