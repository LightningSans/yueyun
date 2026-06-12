/**
 * 悦选配送 — 全局配置
 *
 * WeChat Mini Program + UniApp 适配
 * 模拟支付模式（无第三方 API Key）
 */
const CONFIG = {
  // API 基础路径（开发环境用 localhost，生产环境替换为实际域名）
  BASE_URL: 'http://localhost:8080/api',

  // 应用信息
  APP_NAME: '悦选配送',
  VERSION: '1.0.0',

  // 微信小程序配置（无真实 appId 时使用模拟模式）
  // 后端已配置：未设置 WECHAT_APP_ID 时自动启用模拟登录
  WECHAT: {
    // 留空 = 使用后端 mock 模式
    APP_ID: ''
  },

  // 模拟支付方式
  PAYMENT_METHOD: 'MOCK_PAY',

  // 分页默认值
  PAGE_SIZE: 20
}

export default CONFIG
