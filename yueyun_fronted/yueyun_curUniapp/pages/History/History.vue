<template>
  <view class="page">
    <view class="header">
      <text class="h-title">历史配送</text>
      <view style="width:36px"></view>
    </view>
    <scroll-view scroll-y class="content" @scrolltolower="loadMore" refresher-enabled @refresherrefresh="onRefresh" :refresher-triggered="refreshing">
      <view class="history-list">
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
            <text>{{ t.deliveryName }}</text>
            <text>¥{{ (t.payAmount || 0).toFixed(2) }}</text>
          </view>
        </view>

        <!-- 加载更多状态 -->
        <view v-if="loading" class="load-more">
          <view class="loading-spinner"></view>
          <text>加载中...</text>
        </view>
        <view v-if="noMore && tasks.length" class="load-more">
          <text style="color:#475569;">— 没有更多了 —</text>
        </view>
        <view v-if="!tasks.length && !loading" class="empty-state">
          <text class="empty-icon">📋</text>
          <text>暂无配送记录</text>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import { getHistoryList } from '../../utils/api'
export default {
  data() {
    return {
      tasks: [],
      page: 1,
      loading: false,
      refreshing: false,
      noMore: false
    }
  },
  onShow() {
    this.resetAndLoad()
  },
  methods: {
    resetAndLoad() {
      this.page = 1
      this.tasks = []
      this.noMore = false
      this.loadData()
    },
    async loadData() {
      if (this.loading) return
      this.loading = true
      try {
        const res = await getHistoryList(this.page, 20)
        const records = res.data?.records || []
        this.tasks.push(...records)
        if (records.length < 20) this.noMore = true
      } catch (e) {
        console.log('history error:', e)
      } finally {
        this.loading = false
      }
    },
    loadMore() {
      if (!this.noMore && !this.loading) {
        this.page++
        this.loadData()
      }
    },
    async onRefresh() {
      this.refreshing = true
      this.resetAndLoad()
      this.refreshing = false
    },
    goDetail(t) { uni.navigateTo({ url: '/pages/TaskDetail/TaskDetail?id=' + t.id }) },
    statusTag(s) {
      return { DELIVERED: 'done', COMPLETED: 'done', CANCELLED: 'cancel' }[s] || 'done'
    }
  }
}
</script>

<style scoped>
.page { display: flex; flex-direction: column; height: 100vh; background: #0E1217; }
.header { height: 52px; display: flex; align-items: center; padding: 0 16px; }
.h-title { font-size: 18px; font-weight: 700; flex: 1; text-align: center; color: #F1F5F9; }
.content { flex: 1; }
.history-list { padding: 0 16px 16px; display: flex; flex-direction: column; gap: 10px; }
.delivery-card { padding: 14px; }
.dc-top { display: flex; justify-content: space-between; margin-bottom: 8px; }
.dc-order { font-size: 13px; font-weight: 500; color: #F1F5F9; }
.address-row { display: flex; gap: 6px; font-size: 13px; margin-bottom: 6px; }
.addr-text { color: #94A3B8; line-height: 1.4; flex: 1; }
.meta { display: flex; gap: 12px; font-size: 11px; color: #475569; }

.load-more { display: flex; align-items: center; justify-content: center; gap: 8px; padding: 20px 0; color: #64748B; font-size: 12px; }
.loading-spinner { width: 20px; height: 20px; border: 2px solid #2D3548; border-top-color: #3B82F6; border-radius: 50%; animation: spin .8s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

.card { background: #1C2333; border: 1px solid #2D3548; border-radius: 12px; }
</style>
