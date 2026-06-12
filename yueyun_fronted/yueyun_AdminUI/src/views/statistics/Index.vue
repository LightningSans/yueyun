<template>
  <div>
    <h2 class="page-title">数据统计 <span class="sub">销售数据图表</span></h2>

    <el-row :gutter="16" class="stat-row">
      <el-col :span="6" v-for="item in overview" :key="item.label">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">{{ item.label }}</div>
          <div class="stat-value">{{ item.value }}</div>
          <div class="stat-trend" :class="item.trend > 0 ? 'up' : 'down'">{{ item.trend > 0 ? '↑' : '↓' }} {{ Math.abs(item.trend) }}%</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :span="14">
        <el-card shadow="never" class="chart-card">
          <template #header>每月订单趋势</template>
          <div ref="trendRef" style="height:280px"></div>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="never" class="chart-card">
          <template #header>热销商品 Top 10</template>
          <div v-loading="topLoading">
            <div v-for="(item, i) in topProducts" :key="item.id" class="top-item">
              <span class="top-rank" :class="{ gold: i === 0, silver: i === 1, bronze: i === 2 }">{{ i + 1 }}</span>
              <span class="top-name">{{ item.name }}</span>
              <div class="top-bar-wrap">
                <div class="top-bar" :style="{ width: (item.sales / maxSales * 100) + '%' }"></div>
              </div>
              <span class="top-sales">{{ item.sales }}</span>
            </div>
            <el-empty v-if="!topProducts.length" description="暂无数据" />
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { getStatisticsOverview, getOrderTrendStat, getTopProducts } from '@/api/admin'
import * as echarts from 'echarts'

const overview = ref([
  { label: '总订单数', value: '—', trend: 0 },
  { label: '总成交额', value: '—', trend: 0 },
  { label: '总用户数', value: '—', trend: 0 },
  { label: '总商品数', value: '—', trend: 0 }
])
const topProducts = ref<any[]>([])
const topLoading = ref(false)
const trendRef = ref<HTMLElement>()
let chart: echarts.ECharts | null = null
const maxSales = computed(() => Math.max(...topProducts.value.map((p: any) => p.sales || 0), 1))

onMounted(async () => {
  try {
    const res = await getStatisticsOverview()
    if (res.data) {
      overview.value[0].value = res.data.totalOrders || 0
      overview.value[1].value = '¥' + ((res.data.totalRevenue || 0) / 10000).toFixed(0) + '万'
      overview.value[2].value = res.data.totalUsers || 0
      overview.value[3].value = res.data.totalProducts || 0
    }
  } catch { /* ignore */ }

  try {
    const trendRes = await getOrderTrendStat()
    chart = echarts.init(trendRef.value!)
    chart.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { type: 'category', data: trendRes.data.map((d: any) => d.month), axisLabel: { fontSize: 11 } },
      yAxis: { type: 'value', minInterval: 1 },
      series: [{
        data: trendRes.data.map((d: any) => d.count),
        type: 'bar',
        barWidth: '40%',
        itemStyle: { color: '#1677FF', borderRadius: [4, 4, 0, 0] }
      }]
    })
  } catch { /* ignore */ }

  topLoading.value = true
  try { const res = await getTopProducts(); topProducts.value = res.data || [] } catch { /* ignore */ }
  topLoading.value = false
})

const onResize = () => chart?.resize()
window.addEventListener('resize', onResize)
onUnmounted(() => { chart?.dispose(); window.removeEventListener('resize', onResize) })
</script>

<style scoped>
.page-title { font-size: 20px; font-weight: 700; margin-bottom: 20px; color: #1a1a2e; }
.page-title .sub { font-size: 13px; font-weight: 400; color: #9e9eaf; }
.stat-row { margin-bottom: 16px; }
.stat-card { margin-bottom: 16px; }
.stat-label { font-size: 12px; color: #9e9eaf; margin-bottom: 4px; }
.stat-value { font-size: 24px; font-weight: 700; color: #1a1a2e; }
.stat-trend { font-size: 11px; margin-top: 4px; }
.stat-trend.up { color: #22C55E; }
.stat-trend.down { color: #EF4444; }
.chart-card { margin-bottom: 16px; }
.top-item { display: flex; align-items: center; gap: 8px; padding: 6px 0; font-size: 12px; }
.top-rank { width: 18px; height: 18px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 10px; font-weight: 700; color: #fff; background: #d9d9d9; flex-shrink: 0; }
.top-rank.gold { background: #F59E0B; }
.top-rank.silver { background: #94A3B8; }
.top-rank.bronze { background: #D97706; }
.top-name { flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.top-bar-wrap { width: 80px; height: 14px; background: #f0f2f5; border-radius: 4px; overflow: hidden; flex-shrink: 0; }
.top-bar { height: 100%; background: linear-gradient(90deg, #1677FF, #6950F0); border-radius: 4px; transition: width 0.3s; }
.top-sales { width: 40px; text-align: right; color: #9e9eaf; font-size: 11px; }
</style>
