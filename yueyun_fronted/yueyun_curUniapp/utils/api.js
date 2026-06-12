/**
 * 悦选配送 — API 请求封装
 *
 * 适配：
 *   - UniApp H5（开发调试）
 *   - 微信小程序（生产运行）
 *   - 模拟支付模式（无第三方 API Key）
 */
import CONFIG from './config'

const BASE_URL = CONFIG.BASE_URL

/**
 * 通用请求方法
 * 使用 uni.request 跨 H5 / 微信小程序
 */
function request(url, options = {}) {
  const token = uni.getStorageSync('token')
  return new Promise((resolve, reject) => {
    uni.request({
      url: BASE_URL + url,
      method: options.method || 'GET',
      data: options.data || {},
      header: {
        'Content-Type': 'application/json',
        ...(token ? { 'Authorization': 'Bearer ' + token } : {}),
        ...options.header
      },
      timeout: options.timeout || 15000,
      success: (res) => {
        const data = res.data
        if (data.code === 200) {
          resolve(data)
        } else if (data.code === 401) {
          // Token 过期 → 清除登录态 → 跳转登录页
          uni.removeStorageSync('token')
          uni.removeStorageSync('courierInfo')
          uni.reLaunch({ url: '/pages/Login/Login' })
          reject(new Error(data.msg || '登录已过期'))
        } else {
          reject(new Error(data.msg || '请求失败'))
        }
      },
      fail: (err) => {
        console.error('请求失败:', err)
        // 微信小程序网络错误提示
        uni.showToast({ title: '网络异常，请检查连接', icon: 'none' })
        reject(new Error('网络错误'))
      }
    })
  })
}

// ═══════════════════════════════════════════════
//  认证模块（Courier Auth）
// ═══════════════════════════════════════════════

/** 用户名 + 密码登录（开发测试用） */
export function courierLogin(data) {
  return request('/courier/login', { method: 'POST', data })
}

/**
 * 微信小程序登录
 * 流程：wx.login() → code → POST /api/courier/wx-login
 * 后端无真实 appId 时自动启用模拟模式
 */
export function courierWxLogin(code) {
  return request('/courier/wx-login', {
    method: 'POST',
    data: { code }
  })
}

/** 获取配送员信息 */
export function getCourierInfo() {
  return request('/courier/info')
}

// ═══════════════════════════════════════════════
//  任务模块（Task）
// ═══════════════════════════════════════════════

/** 任务列表（可按状态筛选） */
export function getTaskList(status) {
  return request('/courier/task/list', {
    data: status ? { status } : {}
  })
}

/** 任务详情 */
export function getTaskDetail(id) {
  return request('/courier/task/detail/' + id)
}

/** 确认取货 */
export function pickupTask(id) {
  return request('/courier/task/pickup/' + id, { method: 'POST' })
}

/** 确认送达 */
export function deliverTask(id) {
  return request('/courier/task/deliver/' + id, { method: 'POST' })
}

// ═══════════════════════════════════════════════
//  历史模块（History）
// ═══════════════════════════════════════════════

/** 历史配送记录（分页） */
export function getHistoryList(page = 1, size = CONFIG.PAGE_SIZE) {
  return request('/courier/history/list', {
    data: { page, size }
  })
}

// ═══════════════════════════════════════════════
//  统计模块（Stats）
// ═══════════════════════════════════════════════

/** 配送员数据统计（包含收入） */
export function getCourierStats() {
  return request('/courier/stats')
}
