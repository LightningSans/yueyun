var api = require('../../utils/api')
Page({
  data: {
    courierName: '', courierPhone: '', isActive: true,
    stats: {}, praiseRateText: '0%', ratingText: '--',
    todayIncomeText: '0.00', monthIncomeText: '0.00', balanceText: '0.00'
  },
  onShow: function() {
    var token = wx.getStorageSync('token')
    if (!token) { wx.reLaunch({ url: '/pages/Login/Login' }); return }
    var info = JSON.parse(wx.getStorageSync('courierInfo') || '{}')
    this.setData({
      courierName: info.nickName || info.username || '配送员',
      courierPhone: info.phone || '暂无手机号',
      isActive: info.status === 1
    })
    this.loadStats()
  },
  loadStats: function() {
    var _this = this
    api.getCourierStats()
      .then(function(res) {
        var s = res.data || {}
        _this.setData({
          stats: s,
          praiseRateText: (s.praiseRate || 0) + '%',
          ratingText: s.rating || '--',
          todayIncomeText: _this.fmt(s.todayIncome),
          monthIncomeText: _this.fmt(s.monthIncome),
          balanceText: _this.fmt(s.availableBalance)
        })
      })
      .catch(function(e) { console.log('stats error:', e) })
  },
  fmt: function(v) { return (Number(v) || 0).toFixed(2) },
  goHist: function() { wx.switchTab({ url: '/pages/History/History' }) },
  goIncome: function() { wx.showToast({ title: '收入明细开发中', icon: 'none' }) },
  goAbout: function() { wx.showToast({ title: '悦选配送 v1.0.0 · 模拟支付', icon: 'none' }) },
  handleLogout: function() {
    var _this = this
    wx.showModal({
      title: '退出登录', content: '确定退出当前账号吗？',
      success: function(r) {
        if (r.confirm) {
          wx.removeStorageSync('token')
          wx.removeStorageSync('courierInfo')
          wx.reLaunch({ url: '/pages/Login/Login' })
        }
      }
    })
  }
})
