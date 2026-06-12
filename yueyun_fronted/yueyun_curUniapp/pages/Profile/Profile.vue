<template>
  <view class="page">
    <view class="header">
      <text class="h-title">我的</text>
      <view style="width:36px"></view>
    </view>
    <scroll-view scroll-y class="content">
      <!-- 个人信息卡片 -->
      <view class="profile-card card">
        <view class="avatar-lg">🚚</view>
        <view class="p-info">
          <text class="p-name">{{ courierInfo.nickName || courierInfo.username || '配送员' }}</text>
          <text class="p-phone">{{ courierInfo.phone || '暂无手机号' }}</text>
          <text class="p-status" v-if="courierInfo.status === 1">● 在岗</text>
          <text class="p-status" style="color:#EF4444;" v-else>○ 已禁用</text>
        </view>
      </view>

      <!-- 数据统计 -->
      <view class="profile-stats">
        <view class="p-stat"><text class="p-num green">{{ stats.totalOrders || 0 }}</text><text class="p-label">总配送</text></view>
        <view class="p-stat"><text class="p-num blue">{{ stats.todayOrders || 0 }}</text><text class="p-label">今日单</text></view>
        <view class="p-stat"><text class="p-num yellow">{{ stats.praiseRate || 0 }}%</text><text class="p-label">好评率</text></view>
        <view class="p-stat"><text class="p-num purple">{{ stats.rating || '--' }}</text><text class="p-label">评分</text></view>
      </view>

      <!-- 收入模块 -->
      <view class="income-card card">
        <view class="income-item">
          <text class="income-label">今日收入</text>
          <text class="income-value green">¥{{ (stats.todayIncome || 0).toFixed(2) }}</text>
        </view>
        <view class="income-divider"></view>
        <view class="income-item">
          <text class="income-label">本月收入</text>
          <text class="income-value">¥{{ (stats.monthIncome || 0).toFixed(2) }}</text>
        </view>
        <view class="income-divider"></view>
        <view class="income-item">
          <text class="income-label">可提现</text>
          <text class="income-value highlight">¥{{ (stats.availableBalance || 0).toFixed(2) }}</text>
        </view>
      </view>

      <!-- 菜单列表 -->
      <view class="profile-menu">
        <view class="menu-item" @tap="goHist">
          <text class="menu-icon">📋</text>
          <text class="menu-label">历史配送</text>
          <text class="menu-arrow">›</text>
        </view>
        <view class="menu-item" @tap="goIncome">
          <text class="menu-icon">💰</text>
          <text class="menu-label">收入明细</text>
          <text class="menu-right">¥{{ (stats.monthIncome || 0).toFixed(2) }}</text>
          <text class="menu-arrow">›</text>
        </view>
        <view class="menu-item" @tap="goAbout">
          <text class="menu-icon">ℹ️</text>
          <text class="menu-label">关于</text>
          <text class="menu-right" style="font-size:11px;">v1.0.0</text>
          <text class="menu-arrow">›</text>
        </view>
        <view class="menu-item" style="margin-top:4px;border-color:rgba(239,68,68,.15);" @tap="handleLogout">
          <text class="menu-icon" style="color:#EF4444;">🚪</text>
          <text class="menu-label" style="color:#EF4444;">退出登录</text>
          <text class="menu-arrow" style="color:#EF4444;">›</text>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import { getCourierStats } from '../../utils/api'
export default {
  data() {
    return {
      courierInfo: {},
      stats: {}
    }
  },
  onShow() {
    this.courierInfo = JSON.parse(uni.getStorageSync('courierInfo') || '{}')
    this.loadStats()
  },
  methods: {
    async loadStats() {
      try {
        const res = await getCourierStats()
        this.stats = res.data || {}
      } catch (e) {
        console.log('stats load error:', e)
      }
    },
    goHist() { uni.switchTab({ url: '/pages/History/History' }) },
    goIncome() { uni.showToast({ title: '收入明细开发中', icon: 'none' }) },
    goAbout() { uni.showToast({ title: '悦选配送 v1.0.0 · 模拟支付', icon: 'none' }) },
    handleLogout() {
      uni.showModal({
        title: '退出登录',
        content: '确定退出当前账号吗？',
        success: (r) => {
          if (r.confirm) {
            uni.removeStorageSync('token')
            uni.removeStorageSync('courierInfo')
            uni.reLaunch({ url: '/pages/Login/Login' })
          }
        }
      })
    }
  }
}
</script>

<style scoped>
.page { display: flex; flex-direction: column; height: 100vh; background: #0E1217; }
.header { height: 52px; display: flex; align-items: center; padding: 0 16px; background: #0E1217; }
.h-title { font-size: 18px; font-weight: 700; flex: 1; text-align: center; color: #F1F5F9; }
.content { flex: 1; padding: 0 16px; }

/* ─── Profile Card ─── */
.profile-card { display: flex; align-items: center; gap: 14px; padding: 20px; margin-bottom: 12px; }
.avatar-lg { width: 56px; height: 56px; border-radius: 14px; background: linear-gradient(135deg,#3B82F6,#8B5CF6); display: flex; align-items: center; justify-content: center; font-size: 26px; flex-shrink: 0; }
.p-info { flex: 1; }
.p-name { display: block; font-size: 18px; font-weight: 700; color: #F1F5F9; }
.p-phone { display: block; font-size: 13px; color: #64748B; margin-top: 2px; }
.p-status { display: inline-block; font-size: 11px; color: #22C55E; margin-top: 4px; }

/* ─── Stats ─── */
.profile-stats { display: grid; grid-template-columns: repeat(4, 1fr); margin-bottom: 12px; background: #1C2333; border: 1px solid #2D3548; border-radius: 12px; overflow: hidden; }
.p-stat { text-align: center; padding: 14px 4px; border-right: 1px solid #2D3548; }
.p-stat:last-child { border-right: none; }
.p-num { display: block; font-size: 18px; font-weight: 700; }
.p-num.green { color: #22C55E; }
.p-num.blue { color: #3B82F6; }
.p-num.yellow { color: #EAB308; }
.p-num.purple { color: #8B5CF6; }
.p-label { display: block; font-size: 10px; color: #64748B; margin-top: 2px; }

/* ─── Income Card ─── */
.income-card { display: flex; padding: 16px; margin-bottom: 12px; }
.income-item { flex: 1; text-align: center; }
.income-label { display: block; font-size: 11px; color: #64748B; margin-bottom: 4px; }
.income-value { display: block; font-size: 16px; font-weight: 700; color: #F1F5F9; }
.income-value.green { color: #22C55E; }
.income-value.highlight { color: #3B82F6; }
.income-divider { width: 1px; background: #2D3548; margin: 0 12px; }

/* ─── Menu ─── */
.profile-menu { display: flex; flex-direction: column; gap: 6px; margin-bottom: 24px; }
.menu-item { display: flex; align-items: center; gap: 10px; padding: 14px 16px; background: #1C2333; border: 1px solid #2D3548; border-radius: 8px; }
.menu-item:active { background: #252D3F; }
.menu-icon { width: 20px; text-align: center; font-size: 16px; }
.menu-label { flex: 1; font-size: 14px; color: #F1F5F9; }
.menu-right { font-size: 13px; color: #64748B; }
.menu-arrow { color: #475569; font-size: 12px; }

.card { background: #1C2333; border: 1px solid #2D3548; border-radius: 12px; }
</style>
