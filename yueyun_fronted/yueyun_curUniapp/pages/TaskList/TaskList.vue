<template>
  <view class="page">
    <view class="header">
      <text class="h-title">配送任务</text>
      <view style="width:36px"></view>
    </view>
    <scroll-view scroll-x class="task-tabs" show-scrollbar="false">
      <view v-for="tab in tabs" :key="tab.key" class="task-tab" :class="{ active: activeTab === tab.key }" @tap="switchTab(tab.key)">
        <text>{{ tab.label }}</text>
      </view>
    </scroll-view>
    <scroll-view scroll-y class="content" refresher-enabled @refresherrefresh="onRefresh" :refresher-triggered="refreshing">
      <view class="delivery-list">
        <view v-for="t in tasks" :key="t.id" class="delivery-card card" @tap="goDetail(t)">
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
        <view v-if="!tasks.length && !loading" class="empty-state">
          <text class="empty-icon">📋</text>
          <text v-if="!activeTab">暂无配送任务</text>
          <text v-else>暂无"{{ getTabLabel(activeTab) }}"任务</text>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import { getTaskList } from '../../utils/api'
export default {
  data() {
    return {
      tasks: [],
      loading: false,
      refreshing: false,
      activeTab: '',
      tabs: [
        { key: '', label: '全部' },
        { key: 'ASSIGNED', label: '待取货' },
        { key: 'IN_TRANSIT', label: '配送中' },
        { key: 'DELIVERED', label: '已送达' }
      ]
    }
  },
  onShow() { this.loadData() },
  methods: {
    async loadData() {
      this.loading = true
      try {
        const res = await getTaskList(this.activeTab || undefined)
        this.tasks = res.data || []
      } catch (e) {
        console.log('taskList error:', e)
      } finally {
        this.loading = false
      }
    },
    switchTab(key) {
      this.activeTab = key
      this.loadData()
    },
    async onRefresh() {
      this.refreshing = true
      await this.loadData()
      this.refreshing = false
    },
    getTabLabel(key) {
      const tab = this.tabs.find(t => t.key === key)
      return tab ? tab.label : ''
    },
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
.task-tabs { display: flex; gap: 6px; padding: 12px 16px 8px; white-space: nowrap; }
.task-tab { padding: 8px 18px; border-radius: 20px; font-size: 12px; font-weight: 500; background: #1C2333; color: #94A3B8; border: 1px solid #2D3548; flex-shrink: 0; }
.task-tab.active { background: #3B82F6; color: #fff; border-color: #3B82F6; }
.content { flex: 1; }
.delivery-list { padding: 0 16px 16px; display: flex; flex-direction: column; gap: 10px; }
.delivery-card { padding: 14px; }
.dc-top { display: flex; justify-content: space-between; margin-bottom: 8px; }
.dc-order { font-size: 13px; font-weight: 500; color: #F1F5F9; }
.address-row { display: flex; gap: 6px; font-size: 13px; margin-bottom: 6px; }
.addr-text { color: #94A3B8; line-height: 1.4; flex: 1; }
.meta { display: flex; gap: 16px; font-size: 11px; color: #475569; }
.card { background: #1C2333; border: 1px solid #2D3548; border-radius: 12px; }
</style>
