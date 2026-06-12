var api = require('../../utils/api')
Page({
  data: { tasks: [], page: 1, loading: false, refreshing: false, noMore: false },
  onShow: function() {
    var token = wx.getStorageSync('token')
    if (!token) { wx.reLaunch({ url: '/pages/Login/Login' }); return }
    this.resetAndLoad()
  },
  resetAndLoad: function() {
    this.setData({ page: 1, tasks: [], noMore: false })
    this.loadData()
  },
  loadData: function() {
    if (this.data.loading) return
    var _this = this
    _this.setData({ loading: true })
    api.getHistoryList(_this.data.page, 20)
      .then(function(res) {
        var records = (res.data && res.data.records) || []
        var newTasks = _this.data.tasks.concat(records.map(function(t) {
          var sc = 'tag-done'
          if (t.status === 'CANCELLED') sc = 'tag-cancel'
          return {
            id: t.id, orderNo: t.orderNo, status: t.status, statusText: t.statusText,
            statusTagClass: sc,
            deliveryAddress: t.deliveryAddress, deliveryName: t.deliveryName,
            payAmountText: _this.fmt(t.payAmount)
          }
        }))
        var noMore = records.length < 20
        _this.setData({ tasks: newTasks, noMore: noMore })
      })
      .catch(function(e) { console.log('history error:', e) })
      .finally(function() { _this.setData({ loading: false }) })
  },
  fmt: function(v) { return (Number(v) || 0).toFixed(2) },
  loadMore: function() {
    if (!this.data.noMore && !this.data.loading) {
      this.setData({ page: this.data.page + 1 }, function() { this.loadData() })
    }
  },
  onRefresh: function() {
    var _this = this
    _this.setData({ refreshing: true })
    _this.resetAndLoad()
    _this.setData({ refreshing: false })
  },
  goDetail: function(e) {
    wx.navigateTo({ url: '/pages/TaskDetail/TaskDetail?id=' + e.currentTarget.dataset.id })
  }
})
