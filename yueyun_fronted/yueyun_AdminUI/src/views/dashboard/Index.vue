<template>
  <div class="dashboard">
    <h2 class="page-title">工作台 <span class="sub">今日运营数据总览</span></h2>

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :span="6" v-for="stat in stats" :key="stat.label">
        <el-card shadow="never" class="stat-card" @click="$message.info('查看详情')">
          <div class="stat-icon" :style="{ background: stat.bg, color: stat.color }">{{ stat.icon }}</div>
          <div class="stat-body">
            <div class="stat-label">{{ stat.label }}</div>
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-trend" :class="stat.trend > 0 ? 'up' : 'down'">
              {{ stat.trend > 0 ? '↑' : '↓' }} {{ Math.abs(stat.trend) }}% 较昨日
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表 -->
    <el-row :gutter="16">
      <el-col :span="14">
        <el-card shadow="never" class="chart-card">
          <template #header>近 7 日订单趋势</template>
          <div ref="trendChartRef" style="height: 240px"></div>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="never" class="chart-card">
          <template #header>订单状态分布</template>
          <div ref="pieChartRef" style="height: 240px"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 待处理订单 -->
    <el-card shadow="never" class="order-card">
      <template #header>待处理订单 <span class="sub">最近 5 条</span></template>
      <el-table :data="pendingOrders" stripe style="width: 100%" @row-click="goOrderDetail">
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="userName" label="用户" width="100" />
        <el-table-column prop="payAmount" label="金额" width="120">
          <template #default="{ row }">¥{{ row.payAmount?.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="时间" width="160" />
        <el-table-column label="操作" width="100">
          <template #default>
            <el-button text type="primary" size="small">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { getDashboardStat, getOrderTrend, getOrderDist } from '@/api/admin'
import * as echarts from 'echarts'

const router = useRouter()

// 统计卡片数据
const stats = ref([
  { label: '今日订单', value: '—', trend: 0, icon: '📦', bg: '#E6F4FF', color: '#1677FF' },
  { label: '待发货', value: '—', trend: 0, icon: '🚚', bg: '#ECFDF5', color: '#22C55E' },
  { label: '待处理', value: '—', trend: 0, icon: '⚠️', bg: '#FFFBEB', color: '#F59E0B' },
  { label: '今日收入', value: '—', trend: 0, icon: '💰', bg: '#F3E8FF', color: '#8B5CF6' }
])

const pendingOrders = ref([])

// 图表实例
const trendChartRef = ref<HTMLElement>()
const pieChartRef = ref<HTMLElement>()
let trendChart: echarts.ECharts | null = null
let pieChart: echarts.ECharts | null = null

onMounted(async () => {
  try {
    const res = await getDashboardStat()
    stats.value[0].value = res.data.todayOrderCount
    stats.value[1].value = res.data.pendingDeliveryCount
    stats.value[2].value = res.data.pendingOrderCount
    stats.value[3].value = '¥' + (res.data.todayIncome || 0).toLocaleString()
    stats.value[0].trend = parseFloat(res.data.todayOrderTrend?.replace('↑', '').replace('↓', '-').replace('%', '') || '0')
  } catch { /* 加载失败使用默认值 */ }

  // 折线图
  try {
    const trendRes = await getOrderTrend()
    trendChart = echarts.init(trendChartRef.value!)
    trendChart.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { type: 'category', data: trendRes.data.map((d: any) => d.date), axisLabel: { fontSize: 11 } },
      yAxis: { type: 'value', minInterval: 1 },
      series: [{
        data: trendRes.data.map((d: any) => d.count),
        type: 'line',
        smooth: true,
        lineStyle: { color: '#1677FF', width: 3 },
        areaStyle: { color: 'rgba(22,119,255,0.08)' },
        itemStyle: { color: '#1677FF' }
      }]
    })
  } catch { /* ignore */ }

  // 饼图
  try {
    const distRes = await getOrderDist()
    const colorMap: Record<string, string> = {
      pendingPayment: '#1677FF', completed: '#22C55E',
      pendingDelivery: '#F59E0B', cancelled: '#EF4444'
    }
    const nameMap: Record<string, string> = {
      pendingPayment: '待支付', completed: '已完成',
      pendingDelivery: '待发货', cancelled: '已取消'
    }
    pieChart = echarts.init(pieChartRef.value!)
    pieChart.setOption({
      tooltip: { trigger: 'item' },
      series: [{
        type: 'pie',
        radius: ['40%', '65%'],
        center: ['50%', '50%'],
        data: Object.entries(distRes.data).map(([k, v]) => ({
          name: nameMap[k] || k, value: v,
          itemStyle: { color: colorMap[k] || '#999' }
        })),
        label: { fontSize: 11 },
        emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0,0,0,0.2)' } }
      }]
    })
  } catch { /* ignore */ }
})

// 窗口自适应
const onResize = () => { trendChart?.resize(); pieChart?.resize() }
window.addEventListener('resize', onResize)

onUnmounted(() => {
  trendChart?.dispose()
  pieChart?.dispose()
  window.removeEventListener('resize', onResize)
})

function statusTag(status: string) {
  const map: Record<string, string> = { PENDING_PAYMENT: 'warning', PENDING_DELIVERY: 'info', DELIVERED: 'success', COMPLETED: 'default', CANCELLED: 'danger' }
  return map[status] || 'info'
}
function statusText(status: string) {
  const map: Record<string, string> = { PENDING_PAYMENT: '待支付', PENDING_DELIVERY: '待发货', DELIVERED: '已送达', COMPLETED: '已完成', CANCELLED: '已取消' }
  return map[status] || status
}
function goOrderDetail(row: any) {
  if (row.id) router.push('/orders/' + row.id)
}
</script>

<style scoped>
.page-title { font-size: 20px; font-weight: 700; margin-bottom: 20px; color: #1a1a2e; }
.page-title .sub { font-size: 13px; font-weight: 400; color: #9e9eaf; margin-left: 8px; }
.stat-row { margin-bottom: 16px; }
.stat-card { cursor: pointer; display: flex; align-items: flex-start; gap: 14px; }
.stat-card:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(0,0,0,0.06); transition: all 0.2s; }
.stat-icon { width: 40px; height: 40px; border-radius: 8px; display: flex; align-items: center; justify-content: center; font-size: 20px; flex-shrink: 0; }
.stat-body { flex: 1; }
.stat-label { font-size: 12px; color: #9e9eaf; margin-bottom: 4px; }
.stat-value { font-size: 24px; font-weight: 700; color: #1a1a2e; }
.stat-trend { font-size: 11px; margin-top: 4px; }
.stat-trend.up { color: #22C55E; }
.stat-trend.down { color: #EF4444; }
.chart-card { margin-bottom: 16px; }
.order-card { margin-top: 16px; }
.order-card .sub { font-size: 12px; color: #9e9eaf; font-weight: 400; }
</style>
