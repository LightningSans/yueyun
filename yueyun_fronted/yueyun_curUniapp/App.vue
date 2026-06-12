<template><view></view></template>
<script>
export default {
  onLaunch() {
    console.log('=== 悦选配送 App launched ===')

    // 检查登录状态
    const token = uni.getStorageSync('token')
    if (!token) {
      // 无 token 时跳转登录页
      setTimeout(() => {
        const pages = getCurrentPages()
        // 仅在非登录页时跳转
        if (!pages.length || pages[0].route !== 'pages/Login/Login') {
          uni.reLaunch({ url: '/pages/Login/Login' })
        }
      }, 500)
    }
  },
  onError(err) {
    console.error('App global error:', err)
  }
}
</script>
<style>
/* ═══════════════════════════════════
   悦选配送 — 全局样式
   适配 H5 + 微信小程序
   ═══════════════════════════════════ */
view, scroll-view, swiper, text, image, input, textarea, button, navigator {
  box-sizing: border-box;
}
page {
  background: #0E1217;
  color: #F1F5F9;
  font-family: -apple-system, 'Noto Sans SC', sans-serif;
  font-size: 14px;
  line-height: 1.5;
}
/* 卡片基础样式 */
.card {
  background: #1C2333;
  border: 1px solid #2D3548;
  border-radius: 12px;
}
/* 主按钮 */
.btn-primary {
  height: 48px;
  border: none;
  border-radius: 12px;
  background: linear-gradient(135deg, #3B82F6, #2563EB);
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  box-shadow: 0 4px 15px rgba(59,130,246,.15);
  display: flex;
  align-items: center;
  justify-content: center;
}
.btn-primary:active { opacity: .85; }
/* 描边按钮 */
.btn-outline {
  height: 40px;
  border: 1px solid #2D3548;
  border-radius: 8px;
  background: transparent;
  color: #94A3B8;
  font-size: 13px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.btn-outline:active { background: #252D3F; }
/* 状态标签 */
.tag-status {
  padding: 3px 10px;
  border-radius: 6px;
  font-size: 11px;
  font-weight: 600;
  display: inline-block;
}
.tag-pickup { background: rgba(234,179,8,.12); color: #EAB308; }
.tag-transit { background: rgba(59,130,246,.12); color: #3B82F6; }
.tag-done { background: rgba(34,197,94,.12); color: #22C55E; }
.tag-cancel { background: rgba(239,68,68,.12); color: #EF4444; }
/* 加载动画 */
.loading-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid #2D3548;
  border-top-color: #3B82F6;
  border-radius: 50%;
  animation: spin .8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }
/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60px 20px;
  color: #475569;
  gap: 8px;
}
.empty-icon { font-size: 36px; opacity: .4; }
</style>
