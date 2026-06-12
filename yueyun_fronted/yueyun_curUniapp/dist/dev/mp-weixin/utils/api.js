var CONFIG = require('./config')
var BASE_URL = CONFIG.BASE_URL

function request(url, options) {
  options = options || {}
  var token = wx.getStorageSync('token')
  return new Promise(function(resolve, reject) {
    wx.request({
      url: BASE_URL + url,
      method: options.method || 'GET',
      data: options.data || {},
      header: {
        'Content-Type': 'application/json',
        ...(token ? { 'Authorization': 'Bearer ' + token } : {}),
        ...(options.header || {})
      },
      timeout: options.timeout || 15000,
      success: function(res) {
        var data = res.data
        if (data.code === 200) {
          resolve(data)
        } else if (data.code === 401) {
          wx.removeStorageSync('token')
          wx.removeStorageSync('courierInfo')
          wx.reLaunch({ url: '/pages/Login/Login' })
          reject(new Error(data.msg || '登录已过期'))
        } else {
          reject(new Error(data.msg || '请求失败'))
        }
      },
      fail: function() {
        wx.showToast({ title: '网络异常，请检查连接', icon: 'none' })
        reject(new Error('网络错误'))
      }
    })
  })
}

function courierLogin(data) {
  return request('/courier/login', { method: 'POST', data: data })
}
function courierWxLogin(code) {
  return request('/courier/wx-login', { method: 'POST', data: { code: code } })
}
function getTaskList(status) {
  return request('/courier/task/list', { data: status ? { status: status } : {} })
}
function getTaskDetail(id) {
  return request('/courier/task/detail/' + id)
}
function pickupTask(id) {
  return request('/courier/task/pickup/' + id, { method: 'POST' })
}
function deliverTask(id) {
  return request('/courier/task/deliver/' + id, { method: 'POST' })
}
function getHistoryList(page, size) {
  page = page || 1; size = size || CONFIG.PAGE_SIZE
  return request('/courier/history/list', { data: { page: page, size: size } })
}
function getCourierStats() {
  return request('/courier/stats')
}

module.exports = {
  courierLogin: courierLogin,
  courierWxLogin: courierWxLogin,
  getTaskList: getTaskList,
  getTaskDetail: getTaskDetail,
  pickupTask: pickupTask,
  deliverTask: deliverTask,
  getHistoryList: getHistoryList,
  getCourierStats: getCourierStats
}
