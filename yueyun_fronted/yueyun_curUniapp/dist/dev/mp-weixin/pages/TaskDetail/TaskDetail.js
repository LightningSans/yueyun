var api = require('../../utils/api')
Page({
  data: {
    task: null, taskId: null, loadError: '',
    showPickupBtn: false, showDeliverBtn: false, showCallBtn: false,
    stepCls0: '', stepCls1: '', stepCls2: ''
  },
  onLoad: function(options) {
    var id = Number(options.id)
    this.setData({ taskId: id })
    this.load(id)
  },
  load: function(id) {
    var _this = this
    _this.setData({ loadError: '' })
    api.getTaskDetail(id)
      .then(function(res) {
        var t = res.data
        if (!t) { _this.setData({ loadError: '任务不存在' }); return }
        var statusMap = { ASSIGNED: 'pickup', IN_TRANSIT: 'transit', DELIVERED: 'done', COMPLETED: 'done' }
        var step = { ASSIGNED: 0, IN_TRANSIT: 1, DELIVERED: 2, COMPLETED: 2 }
        var s = step[t.status] !== undefined ? step[t.status] : -1
        // 处理商品明细
        if (t.items) {
          t.items = t.items.map(function(item) {
            item.priceText = _this.fmt(item.price)
            return item
          })
        }
        t.statusTag = statusMap[t.status] || 'pickup'
        t.payAmountText = _this.fmt(t.payAmount)
        _this.setData({
          task: t,
          showPickupBtn: t.status === 'ASSIGNED',
          showDeliverBtn: t.status === 'IN_TRANSIT',
          showCallBtn: t.status === 'ASSIGNED' || t.status === 'IN_TRANSIT',
          stepCls0: s >= 0 ? 'active' : '',
          stepCls1: (s >= 1 ? 'active' : '') + (t.status === 'IN_TRANSIT' ? ' current' : ''),
          stepCls2: s >= 2 ? 'active' : ''
        })
      })
      .catch(function(e) {
        _this.setData({ loadError: e.message || '任务不存在' })
        wx.showToast({ title: '加载失败', icon: 'none' })
      })
  },
  fmt: function(v) { return (Number(v) || 0).toFixed(2) },
  reload: function() { this.load(this.data.taskId) },
  doPickup: function() {
    var _this = this
    wx.showModal({
      title: '确认取货', content: '确认已从商家取到商品？',
      success: function(r) {
        if (r.confirm) {
          api.pickupTask(_this.data.task.id)
            .then(function() {
              wx.showToast({ title: '已确认取货 ✓', icon: 'success' })
              _this.load(_this.data.task.id)
            })
            .catch(function(e) { wx.showToast({ title: e.message, icon: 'none' }) })
        }
      }
    })
  },
  doDeliver: function() {
    var _this = this
    wx.showModal({
      title: '确认送达', content: '确认已送达至收货人？',
      success: function(r) {
        if (r.confirm) {
          api.deliverTask(_this.data.task.id)
            .then(function() {
              wx.showToast({ title: '已确认送达 ✓', icon: 'success' })
              _this.load(_this.data.task.id)
            })
            .catch(function(e) { wx.showToast({ title: e.message, icon: 'none' }) })
        }
      }
    })
  },
  callPhone: function(e) {
    var phone = e.currentTarget.dataset.phone
    if (phone) { wx.makePhoneCall({ phoneNumber: phone }) }
  },
  goBack: function() { wx.navigateBack() }
})
