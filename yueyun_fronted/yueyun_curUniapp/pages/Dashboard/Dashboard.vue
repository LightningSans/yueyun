<template>
  <view class="page">
    <view class="header"><text class="h-title">工作台</text></view>
    <scroll-view scroll-y class="content">
      <view class="dash-top">
        <!-- 问候卡片 -->
        <view class="dash-greeting card">
          <view class="avatar">🚚</view>
          <view class="info">
            <text class="name">{{ courierInfo.nickName || courierInfo.username || '配送员' }}</text>
            <text class="status"><text class="pulse"></text> 今日已接单 · 在线</text>
          </view>
        </view>

        <!-- 数据概览 -->
        <view class="dash-stats">
          <view class="dash-stat card" @tap="goTab">
            <text class="num accent-blue">{{ stats.todayOrders || 0 }}</text>
            <text class="label">今日单量</text>
          </view>
          <view class="dash-stat card">
            <text class="num accent-green">{{ stats.completedOrders || 0 }}</text>
            <text class="label">已完成</text>
          </view>
          <view class="dash-stat card" @tap="goTab">
            <text class="num accent-yellow">{{ stats.inProgressOrders || 0 }}</text>
            <text class="label">进行中</text>
          </view>
        </view>

        <!-- 快捷入口 -->
        <view class="quick-actions">
          <view class="quick-action" @tap="goTab">
            <view class="qa-icon" style="background:rgba(59,130,246,.15);"><text style="color:#3B82F6;">📦</text></view>
            <view class="qa-info">
              <text class="qa-title">待取货</text>
              <text class="qa-sub">{{ pendingCount }} 单待处理</text>
            </view>
            <text class="qa-arrow">›</text>
          </view>
          <view class="quick-action" @tap="goTab">
            <view class="qa-icon" style="background:rgba(34,197,94,.15);"><text style="color:#22C55E;">🚚</text></view>
            <view class="qa-info">
              <text class="qa-title">配送中</text>
              <text class="qa-sub">{{ transitCount }} 单进行中</text>
            </view>
            <text class="qa-arrow">›</text>
          </view>
          <view class="quick-action" @tap="goHist">
            <view class="qa-icon" style="background:rgba(139,92,246,.15);"><text style="color:#8B5CF6;">📋</text></view>
            <view class="qa-info">
              <text class="qa-title">历史记录</text>
              <text class="qa-sub">查看已完成配送</text>
            </view>
            <text class="qa-arrow">›</text>
          </view>
          <view class="quick-action" @tap="goProfile">
            <view class="qa-icon" style="background:rgba(6,182,212,.15);"><text style="color:#06B6D4;">👤</text></view>
            <view class="qa-info">
              <text class="qa-title">我的</text>
              <text class="qa-sub">今日收入 ¥{{ (stats.todayIncome || 0).toFixed(2) }}</text>
            </view>
            <text class="qa-arrow">›</text>
          </view>
        </view>
      </view>

      <!-- 最近任务 -->
      <view class="dash-section-header">
        <text class="dash-sec-title">最近配送任务</text>
        <text class="dash-sec-more" @tap="goTab">查看全部</text>
      </view>
      <view class="delivery-list">
        <view v-for="t in recentTasks" :key="t.id" class="delivery-card card" @tap="goDetail(t)">
          <view class="dc-top">
            <text class="dc-order">#{{ t.orderNo }}</text>
            <text class="tag-status" :class="'tag-' + statusTag(t.status)">{{ t.statusText }}</text>
          </view>
          <view class="address-row">
            <text>📍</text>
            <text class="addr-text">{{ t.deliveryAddress }}</text>
          </view>
          <view class="meta">
            <text>{{ t.deliveryName }} {{ t.deliveryPhone }}</text>
            <text>¥{{ (t.payAmount || 0).toFixed(2) }}</text>
          </view>
        </view>
        <view v-if="!recentTasks.length" class="empty-state">
          <text class="empty-icon">📋</text>
          <text>暂无配送任务</text>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import { getTaskList, getCourierStats } from '../../utils/api'
export default {
  data() {
    return {
      courierInfo: {},
      stats: {},
      recentTasks: [],
      pendingCount: 0,
      transitCount: 0
    }
  },
  onShow() {
    this.courierInfo = JSON.parse(uni.getStorageSync('courierInfo') || '{}')
    this.loadData()
  },
  methods: {
    async loadData() {
      try {
        const [taskRes, statRes] = await Promise.all([
          getTaskList(),
          getCourierStats()
        ])
        const tasks = taskRes.data || []
        this.recentTasks = tasks.slice(0, 5)
        this.pendingCount = tasks.filter(t => t.status === 'ASSIGNED').length
        this.transitCount = tasks.filter(t => t.status === 'IN_TRANSIT').length
        this.stats = statRes.data || {}
      } catch (e) {
        console.log('loadData error:', e)
      }
    },
    goTab() { uni.switchTab({ url: '/pages/TaskList/TaskList' }) },
    goHist() { uni.switchTab({ url: '/pages/History/History' }) },
    goProfile() { uni.switchTab({ url: '/pages/Profile/Profile' }) },
    goDetail(t) { uni.navigateTo({ url: '/pages/TaskDetail/TaskDetail?id=' + t.id }) },
    statusTag(s) {
      return { ASSIGNED: 'pickup', IN_TRANSIT: 'transit', DELIVERED: 'done', COMPLETED: 'done' }[s] || 'pickup'
    }
  }
}
</script>

<style scoped>
.page { display: flex; flex-direction: column; height: 100vh; background: #0E1217; }
.header { height: 52px; display: flex; align-items: center; padding: 0 16px; }
.h-title { font-size: 18px; font-weight: 700; flex: 1; text-align: center; color: #F1F5F9; }
.content { flex: 1; }

/* ─── Top ─── */
.dash-top { padding: 0 16px 16px; }
.dash-greeting { display: flex; align-items: center; gap: 12px; padding: 14px 16px; margin-bottom: 12px; }
.avatar { width: 44px; height: 44px; border-radius: 12px; background: linear-gradient(135deg,#3B82F6,#8B5CF6); display: flex; align-items: center; justify-content: center; font-size: 20px; flex-shrink: 0; }
.info { flex: 1; }
.name { font-size: 16px; font-weight: 600; display: block; color: #F1F5F9; }
.status { font-size: 12px; color: #22C55E; display: flex; align-items: center; gap: 4px; }
.pulse { width: 6px; height: 6px; border-radius: 50%; background: #22C55E; }

/* ─── Stats ─── */
.dash-stats { display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; margin-bottom: 12px; }
.dash-stat { padding: 14px 8px; text-align: center; }
.num { display: block; font-size: 24px; font-weight: 700; }
.num.accent-blue { color: #3B82F6; }
.num.accent-green { color: #22C55E; }
.num.accent-yellow { color: #EAB308; }
.label { display: block; font-size: 11px; color: #64748B; margin-top: 2px; }

/* ─── Quick Actions ─── */
.quick-actions { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; margin-bottom: 6px; }
.quick-action { display: flex; align-items: center; gap: 10px; padding: 12px; background: #1C2333; border: 1px solid #2D3548; border-radius: 8px; }
.quick-action:active { background: #252D3F; }
.qa-icon { width: 36px; height: 36px; border-radius: 8px; display: flex; align-items: center; justify-content: center; font-size: 18px; flex-shrink: 0; }
.qa-info { flex: 1; }
.qa-title { font-size: 13px; font-weight: 500; display: block; color: #F1F5F9; }
.qa-sub { font-size: 11px; color: #64748B; display: block; }
.qa-arrow { color: #475569; font-size: 12px; }

/* ─── Section ─── */
.dash-section-header { display: flex; justify-content: space-between; align-items: center; padding: 4px 16px 10px; }
.dash-sec-title { font-size: 14px; font-weight: 600; color: #94A3B8; letter-spacing: .5px; }
.dash-sec-more { font-size: 12px; color: #3B82F6; }

/* ─── Delivery List ─── */
.delivery-list { padding: 0 16px 16px; display: flex; flex-direction: column; gap: 10px; }
.delivery-card { padding: 14px; }
.dc-top { display: flex; justify-content: space-between; margin-bottom: 8px; }
.dc-order { font-size: 13px; font-weight: 500; color: #F1F5F9; }
.address-row { display: flex; gap: 6px; font-size: 13px; margin-bottom: 6px; }
.addr-text { color: #94A3B8; line-height: 1.4; flex: 1; }
.meta { display: flex; gap: 16px; font-size: 11px; color: #475569; }

.card { background: #1C2333; border: 1px solid #2D3548; border-radius: 12px; }
</style>
