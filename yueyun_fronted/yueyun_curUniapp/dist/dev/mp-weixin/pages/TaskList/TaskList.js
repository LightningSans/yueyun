var api = require('../../utils/api')
Page({
  data: { tasks: [], loading: false, refreshing: false, activeTab: '', curTabLabel: '', tabs: [
    { key: '', label: '全部' }, { key: 'ASSIGNED', label: '待取货' },
    { key: 'IN_TRANSIT', label: '配送中' }, { key: 'DELIVERED', label: '已送达' }
  ]},
  onShow: function() {
    var token = wx.getStorageSync('token')
    if (!token) { wx.reLaunch({ url: '/pages/Login/Login' }); return }
    this.loadData()
  },
  loadData: function() {
    var _this = this
    _this.setData({ loading: true })
    api.getTaskList(_this.data.activeTab || undefined)
      .then(function(res) {
        var tasks = (res.data || []).map(function(t) {
          var statusMap = { ASSIGNED: 'pickup', IN_TRANSIT: 'transit', DELIVERED: 'done', COMPLETED: 'done' }
          return {
            id: t.id, orderNo: t.orderNo, status: t.status, statusText: t.statusText,
            statusTag: statusMap[t.status] || 'pickup',
            deliveryAddress: t.deliveryAddress, deliveryName: t.deliveryName,
            deliveryPhone: t.deliveryPhone,
            payAmountText: _this.fmt(t.payAmount)
          }
        })
        _this.setData({ tasks: tasks })
      })
      .catch(function(e) { console.log('taskList error:', e) })
      .finally(function() { _this.setData({ loading: false }) })
  },
  fmt: function(v) { return (Number(v) || 0).toFixed(2) },
  switchTab: function(e) {
    var key = e.currentTarget.dataset.key
    var label = ''
    var tabs = this.data.tabs
    for (var i = 0; i < tabs.length; i++) { if (tabs[i].key === key) { label = tabs[i].label; break } }
    this.setData({ activeTab: key, curTabLabel: label }, function() { this.loadData() })
  },
  onRefresh: function() {
    var _this = this
    _this.setData({ refreshing: true })
    _this.loadData()
    _this.setData({ refreshing: false })
  },
  goDetail: function(e) { wx.navigateTo({ url: '/pages/TaskDetail/TaskDetail?id=' + e.currentTarget.dataset.id }) }
})
