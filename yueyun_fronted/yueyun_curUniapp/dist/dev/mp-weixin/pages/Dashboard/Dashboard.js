var api = require('../../utils/api')
Page({
  data: { courierInfo: {}, courierName: '', stats: {}, recentTasks: [], pendingCount: 0, transitCount: 0, todayIncomeText: '0.00' },
  onShow: function() {
    // 检查登录状态
    var token = wx.getStorageSync('token')
    if (!token) { wx.reLaunch({ url: '/pages/Login/Login' }); return }
    var info = JSON.parse(wx.getStorageSync('courierInfo') || '{}')
    this.setData({ courierInfo: info, courierName: info.nickName || info.username || '配送员' })
    this.loadData()
  },
  loadData: function() {
    var _this = this
    Promise.all([api.getTaskList(), api.getCourierStats()])
      .then(function(results) {
        var tasks = results[0].data || []
        var stats = results[1].data || {}
        var pending = 0, transit = 0
        var recentTasks = tasks.slice(0, 5).map(function(t) {
          if (t.status === 'ASSIGNED') pending++
          if (t.status === 'IN_TRANSIT') transit++
          var statusMap = { ASSIGNED: 'pickup', IN_TRANSIT: 'transit', DELIVERED: 'done', COMPLETED: 'done' }
          return {
            id: t.id,
            orderNo: t.orderNo,
            status: t.status,
            statusText: t.statusText,
            statusTag: statusMap[t.status] || 'pickup',
            deliveryAddress: t.deliveryAddress,
            deliveryName: t.deliveryName,
            deliveryPhone: t.deliveryPhone,
            payAmountText: _this.fmt(t.payAmount)
          }
        })
        _this.setData({
          recentTasks: recentTasks,
          stats: stats,
          pendingCount: pending,
          transitCount: transit,
          todayIncomeText: _this.fmt(stats.todayIncome)
        })
      })
      .catch(function(e) { console.log('loadData error:', e) })
  },
  fmt: function(v) { return (Number(v) || 0).toFixed(2) },
  goTab: function() { wx.switchTab({ url: '/pages/TaskList/TaskList' }) },
  goHist: function() { wx.switchTab({ url: '/pages/History/History' }) },
  goProfile: function() { wx.switchTab({ url: '/pages/Profile/Profile' }) },
  goDetail: function(e) {
    wx.navigateTo({ url: '/pages/TaskDetail/TaskDetail?id=' + e.currentTarget.dataset.id })
  }
})
