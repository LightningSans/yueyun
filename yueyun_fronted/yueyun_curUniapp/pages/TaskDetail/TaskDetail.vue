<template>
  <view class="page">
    <view class="header">
      <view class="back" @tap="goBack">‹</view>
      <text class="h-title">任务详情</text>
      <view style="width:36px"></view>
    </view>
    <scroll-view scroll-y class="content">
      <view v-if="task" class="detail-content">
        <!-- 状态信息 -->
        <view class="info-card card">
          <view class="row"><text class="row-label">订单号</text><text class="row-value mono">{{ task.orderNo }}</text></view>
          <view class="row"><text class="row-label">状态</text><text class="tag-status" :class="'tag-' + statusTag(task.status)">{{ task.statusText }}</text></view>
          <view class="row"><text class="row-label">金额</text><text class="row-value highlight">¥{{ (task.payAmount || 0).toFixed(2) }}</text></view>
          <view class="row"><text class="row-label">时间</text><text class="row-value">{{ task.createTime }}</text></view>
        </view>

        <!-- 取货/收货信息 -->
        <view class="info-card card">
          <view class="row"><text class="row-label">取货地址</text><text class="row-value">{{ task.pickupAddress }}</text></view>
          <view class="row">
            <text class="row-label">取货联系</text>
            <text class="row-value highlight" @tap="callPhone(task.pickupPhone)">{{ task.pickupContact }} {{ task.pickupPhone }}</text>
          </view>
          <view class="row"><text class="row-label">收货地址</text><text class="row-value">{{ task.deliveryAddress }}</text></view>
          <view class="row">
            <text class="row-label">收货人</text>
            <text class="row-value highlight" @tap="callPhone(task.deliveryPhone)">{{ task.deliveryName }} {{ task.deliveryPhone }}</text>
          </view>
        </view>

        <!-- 商品明细 -->
        <view class="info-card card" v-if="task.items && task.items.length">
          <view class="section-label">商品明细</view>
          <view class="row" v-for="(item, i) in task.items" :key="i">
            <text class="row-value" style="flex:1;">{{ item.productName }}</text>
            <text class="row-value" style="text-align:right;">x{{ item.quantity }} ¥{{ (item.price || 0).toFixed(2) }}</text>
          </view>
        </view>

        <!-- 配送状态时间线 -->
        <view class="info-card card">
          <view class="section-label">配送进度</view>
          <view class="status-timeline">
            <view class="tl-step" :class="{ active: step >= 0 }">
              <view class="tl-dot"></view><view class="tl-line"></view>
              <view class="tl-info"><text class="tl-title">已指派</text><text class="tl-desc">等待配送员取货</text></view>
            </view>
            <view class="tl-step" :class="{ active: step >= 1, current: task.status === 'IN_TRANSIT' }">
              <view class="tl-dot"></view><view class="tl-line"></view>
              <view class="tl-info"><text class="tl-title">配送中</text><text class="tl-desc">正在派送至目的地</text></view>
            </view>
            <view class="tl-step" :class="{ active: step >= 2 }">
              <view class="tl-dot"></view>
              <view class="tl-info"><text class="tl-title">已送达</text><text class="tl-desc">订单完成</text></view>
            </view>
          </view>
        </view>

        <!-- 操作按钮 -->
        <view class="action-bar">
          <view v-if="task.status === 'ASSIGNED'" class="btn-action primary" @tap="doPickup">
            📦 确认取货
          </view>
          <view v-if="task.status === 'IN_TRANSIT'" class="btn-action success" @tap="doDeliver">
            ✅ 确认送达
          </view>
          <view v-if="task.status === 'ASSIGNED' || task.status === 'IN_TRANSIT'" class="btn-action outline" @tap="callPhone(task.deliveryPhone)">
            📞 联系收货人
          </view>
        </view>
      </view>

      <!-- 加载状态 -->
      <view v-if="!task && !loadError" class="loading-state">
        <view class="loading-spinner"></view>
        <text style="color:#64748B;font-size:13px;margin-top:8px;">加载中...</text>
      </view>

      <!-- 错误状态 -->
      <view v-if="loadError" class="loading-state">
        <text style="font-size:36px;opacity:.3;">😵</text>
        <text style="color:#64748B;font-size:13px;margin-top:8px;">{{ loadError }}</text>
        <view class="btn-action outline" style="margin-top:16px;width:200px;" @tap="load(taskId)">重新加载</view>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import { getTaskDetail, pickupTask, deliverTask } from '../../utils/api'
export default {
  data() {
    return {
      task: null,
      taskId: null,
      loadError: ''
    }
  },
  onLoad(options) {
    this.taskId = Number(options.id)
    this.load(this.taskId)
  },
  computed: {
    step() {
      const m = { ASSIGNED: 0, IN_TRANSIT: 1, DELIVERED: 2, COMPLETED: 2 }
      return m[this.task ? this.task.status : ''] ?? -1
    }
  },
  methods: {
    async load(id) {
      this.loadError = ''
      try {
        const res = await getTaskDetail(id)
        this.task = res.data
      } catch (e) {
        this.loadError = e.message || '任务不存在'
        uni.showToast({ title: '加载失败', icon: 'none' })
      }
    },
    async doPickup() {
      try {
        await pickupTask(this.task.id)
        uni.showToast({ title: '已确认取货 ✓', icon: 'success' })
        this.load(this.task.id)
      } catch (e) {
        uni.showToast({ title: e.message, icon: 'none' })
      }
    },
    async doDeliver() {
      try {
        await deliverTask(this.task.id)
        uni.showToast({ title: '已确认送达 ✓', icon: 'success' })
        this.load(this.task.id)
      } catch (e) {
        uni.showToast({ title: e.message, icon: 'none' })
      }
    },
    /** 拨打电话（微信小程序使用 uni.makePhoneCall） */
    callPhone(phone) {
      if (!phone) return
      // #ifdef MP-WEIXIN
      uni.makePhoneCall({ phoneNumber: phone })
      // #endif
      // #ifndef MP-WEIXIN
      uni.showToast({ title: '拨打 ' + phone, icon: 'none' })
      // #endif
    },
    goBack() { uni.navigateBack() },
    statusTag(s) {
      return { ASSIGNED: 'pickup', IN_TRANSIT: 'transit', DELIVERED: 'done', COMPLETED: 'done' }[s] || 'pickup'
    }
  }
}
</script>

<style scoped>
.page { display: flex; flex-direction: column; height: 100vh; background: #0E1217; }
.header { height: 52px; display: flex; align-items: center; padding: 0 16px; background: #0E1217; }
.back { width: 36px; height: 36px; background: #1C2333; border-radius: 10px; display: flex; align-items: center; justify-content: center; font-size: 22px; color: #F1F5F9; }
.back:active { background: #252D3F; }
.h-title { flex: 1; text-align: center; font-size: 16px; font-weight: 600; color: #F1F5F9; }
.content { flex: 1; }
.detail-content { padding: 0 16px 20px; }
.info-card { margin-bottom: 10px; }
.row { display: flex; align-items: center; padding: 10px 14px; border-bottom: 1px solid #2D3548; }
.row:last-child { border-bottom: none; }
.row-label { width: 70px; font-size: 12px; color: #64748B; flex-shrink: 0; }
.row-value { flex: 1; font-size: 13px; color: #F1F5F9; }
.row-value.highlight { color: #3B82F6; }
.row-value.mono { font-family: monospace; font-size: 12px; }
.section-label { font-size: 11px; font-weight: 600; text-transform: uppercase; letter-spacing: .8px; color: #475569; padding: 12px 14px 4px; }

/* ─── Timeline ─── */
.status-timeline { padding: 16px; }
.tl-step { display: flex; gap: 12px; position: relative; padding-bottom: 18px; }
.tl-step:last-child { padding-bottom: 0; }
.tl-dot { width: 10px; height: 10px; min-width: 10px; border-radius: 50%; background: #2D3548; margin-top: 3px; z-index: 1; }
.tl-step.active .tl-dot { background: #22C55E; box-shadow: 0 0 0 3px rgba(34,197,94,.2); }
.tl-step.current .tl-dot { background: #3B82F6; box-shadow: 0 0 0 3px rgba(59,130,246,.2); }
.tl-line { position: absolute; left: 4.5px; top: 18px; bottom: 0; width: 2px; background: #2D3548; }
.tl-step:last-child .tl-line { display: none; }
.tl-info { flex: 1; }
.tl-title { font-size: 13px; font-weight: 500; display: block; color: #F1F5F9; }
.tl-desc { font-size: 11px; color: #64748B; display: block; }
.tl-step.current .tl-title { color: #3B82F6; font-weight: 600; }

/* ─── Actions ─── */
.action-bar { padding: 12px 16px; display: flex; flex-direction: column; gap: 8px; }
.btn-action { height: 48px; line-height: 48px; text-align: center; border-radius: 12px; font-size: 15px; font-weight: 600; }
.btn-action:active { opacity: .85; }
.btn-action.primary { background: linear-gradient(135deg,#3B82F6,#2563EB); color: #fff; }
.btn-action.success { background: #22C55E; color: #fff; }
.btn-action.outline { background: transparent; border: 1.5px solid #2D3548; color: #94A3B8; }

/* ─── States ─── */
.loading-state { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 80px 20px; }
.loading-spinner { width: 32px; height: 32px; border: 3px solid #2D3548; border-top-color: #3B82F6; border-radius: 50%; animation: spin .8s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

.card { background: #1C2333; border: 1px solid #2D3548; border-radius: 12px; }
</style>
