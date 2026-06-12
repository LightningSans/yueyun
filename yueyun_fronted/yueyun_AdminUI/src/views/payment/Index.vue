<template>
  <div>
    <h2 class="page-title">支付记录 <span class="sub">支付流水查询，只读</span></h2>

    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="query" size="small">
        <el-form-item label="订单号"><el-input v-model="query.orderNo" placeholder="订单号" clearable style="width:160px" /></el-form-item>
        <el-form-item label="支付方式"><el-select v-model="query.paymentMethod" placeholder="全部" clearable style="width:120px"><el-option label="模拟支付" value="MOCK_PAY" /><el-option label="余额支付" value="BALANCE" /></el-select></el-form-item>
        <el-form-item label="状态"><el-select v-model="query.payStatus" placeholder="全部" clearable style="width:110px"><el-option label="成功" :value="1" /><el-option label="失败" :value="0" /></el-select></el-form-item>
        <el-form-item><el-button type="primary" @click="handleSearch">搜索</el-button><el-button @click="handleReset">重置</el-button></el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="orderNo" label="订单号" width="170" />
        <el-table-column label="支付金额" width="120"><template #default="{ row }">¥{{ row.payAmount?.toFixed(2) }}</template></el-table-column>
        <el-table-column prop="paymentMethod" label="支付方式" width="120">
          <template #default="{ row }"><el-tag :type="row.paymentMethod === 'MOCK_PAY' ? 'info' : 'warning'" size="small">{{ row.paymentMethod === 'MOCK_PAY' ? '模拟支付' : '余额支付' }}</el-tag></template>
        </el-table-column>
        <el-table-column label="支付状态" width="100">
          <template #default="{ row }"><el-tag :type="row.payStatus === 1 ? 'success' : 'danger'" size="small">{{ row.payStatus === 1 ? '成功 ✔' : '失败' }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="payTime" label="支付时间" width="170" />
        <el-table-column prop="remark" label="备注" min-width="150" />
      </el-table>
      <el-pagination v-model:current-page="query.page" v-model:page-size="query.size" :total="total" layout="total, sizes, prev, pager, next" background @change="fetchData" style="margin-top:16px;justify-content:flex-end" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { getPaymentList } from '@/api/admin'

const loading = ref(false); const tableData = ref<any[]>([]); const total = ref(0)
const query = reactive({ page: 1, size: 10, orderNo: '', paymentMethod: null as string | null, payStatus: null as number | null })

onMounted(() => fetchData())
async function fetchData() { loading.value = true; try { const res = await getPaymentList(query); tableData.value = res.data.records; total.value = res.data.total } finally { loading.value = false } }
function handleSearch() { query.page = 1; fetchData() }
function handleReset() { query.orderNo = ''; query.paymentMethod = null; query.payStatus = null; query.page = 1; fetchData() }
</script>
<style scoped>
.page-title { font-size: 20px; font-weight: 700; margin-bottom: 20px; color: #1a1a2e; }
.page-title .sub { font-size: 13px; font-weight: 400; color: #9e9eaf; }
.search-card { margin-bottom: 16px; }
</style>
