var api = require('../../utils/api')
Page({
  data: {
    username: 'courier01',
    password: '123456',
    errorMsg: '',
    wxLoading: false,
    wxLoginEnabled: false
  },
  onLoad: function() {
    // 检测微信登录能力
    var _this = this
    wx.canIUse('login') && _this.setData({ wxLoginEnabled: true })
    // 已有 Token 则直接跳转
    var token = wx.getStorageSync('token')
    if (token) { this.goHome() }
  },
  onUsernameInput: function(e) { this.setData({ username: e.detail.value }) },
  onPasswordInput: function(e) { this.setData({ password: e.detail.value }) },
  handleLogin: function() {
    var _this = this
    if (!_this.data.username.trim() || !_this.data.password.trim()) {
      _this.setData({ errorMsg: '请输入账号和密码' }); return
    }
    _this.setData({ errorMsg: '', wxLoading: true })
    api.courierLogin({ username: _this.data.username.trim(), password: _this.data.password.trim() })
      .then(function(res) { _this.handleLoginSuccess(res.data) })
      .catch(function(e) { _this.setData({ errorMsg: e.message || '登录失败' }) })
      .finally(function() { _this.setData({ wxLoading: false }) })
  },
  handleWxLogin: function() {
    var _this = this
    _this.setData({ errorMsg: '', wxLoading: true })
    wx.login({
      success: function(wxRes) {
        if (wxRes.code) {
          api.courierWxLogin(wxRes.code)
            .then(function(res) { _this.handleLoginSuccess(res.data) })
            .catch(function(e) { _this.setData({ errorMsg: e.message || '微信登录失败' }) })
            .finally(function() { _this.setData({ wxLoading: false }) })
        } else {
          _this.setData({ errorMsg: '获取微信凭证失败', wxLoading: false })
        }
      },
      fail: function() {
        _this.setData({ errorMsg: '微信登录失败', wxLoading: false })
      }
    })
  },
  handleLoginSuccess: function(data) {
    if (!data || !data.token) {
      this.setData({ errorMsg: '登录返回数据异常' }); return
    }
    wx.setStorageSync('token', data.token)
    wx.setStorageSync('courierInfo', JSON.stringify(data.courierInfo || {}))
    wx.showToast({ title: '登录成功', icon: 'success' })
    setTimeout(function() { this.goHome() }.bind(this), 500)
  },
  goHome: function() {
    wx.reLaunch({ url: '/pages/Dashboard/Dashboard' })
  }
})
