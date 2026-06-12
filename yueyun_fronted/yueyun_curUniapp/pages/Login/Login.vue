<template>
  <view class="login-page">
    <!-- Logo & Brand -->
    <view class="login-brand">
      <view class="logo-icon">🚚</view>
      <text class="brand-title">悦选配送</text>
      <text class="brand-sub">配送员工作台 · 高效交付每一单</text>
    </view>

    <!-- 登录表单 -->
    <view class="login-form">
      <view class="field">
        <text class="field-icon">👤</text>
        <input class="field-input" v-model="username" placeholder="配送员账号" />
      </view>
      <view class="field">
        <text class="field-icon">🔒</text>
        <input class="field-input" v-model="password" placeholder="登录密码" type="password" />
      </view>

      <!-- 用户名密码登录 -->
      <view class="btn-login" @tap="handleLogin" v-if="!wxLoading">
        <text>登 录</text>
      </view>

      <!-- 微信小程序授权登录（仅微信环境可用） -->
      <view class="btn-wx-login" @tap="handleWxLogin" v-if="canWxLogin && !wxLoading">
        <text class="wx-icon">💬</text>
        <text>微信一键登录</text>
      </view>

      <!-- 加载中 -->
      <view class="btn-login" v-if="wxLoading" style="opacity:0.6">
        <text>登录中...</text>
      </view>

      <!-- 错误提示 -->
      <text class="error-text" v-if="errorMsg">{{ errorMsg }}</text>

      <text class="version-text">版本 v1.0.0 · 悦选配送员端</text>
      <text class="version-text" style="color:#475569;font-size:11px;margin-top:2px;">模拟支付模式 · 无需真实 API Key</text>
    </view>
  </view>
</template>

<script>
import { courierLogin, courierWxLogin } from '../../utils/api'
import CONFIG from '../../utils/config'

export default {
  data() {
    return {
      username: 'courier01',
      password: '123456',
      errorMsg: '',
      wxLoading: false,
      canWxLogin: false
    }
  },
  onLoad() {
    // 检测是否在微信小程序环境
    // #ifdef MP-WEIXIN
    this.canWxLogin = true
    // #endif

    // 如果已有 Token，直接进入主页
    const token = uni.getStorageSync('token')
    if (token) {
      this.goHome()
    }
  },
  methods: {
    /** 用户名 + 密码登录 */
    async handleLogin() {
      if (!this.username.trim() || !this.password.trim()) {
        this.errorMsg = '请输入账号和密码'
        return
      }
      this.errorMsg = ''
      this.wxLoading = true

      try {
        const res = await courierLogin({
          username: this.username.trim(),
          password: this.password.trim()
        })

        this.handleLoginSuccess(res.data)
      } catch (e) {
        this.errorMsg = e.message || '登录失败，请检查账号密码'
      } finally {
        this.wxLoading = false
      }
    },

    /** 微信小程序一键登录 */
    async handleWxLogin() {
      this.errorMsg = ''
      this.wxLoading = true

      try {
        // 调用微信登录获取临时 code
        const wxRes = await this.getWxCode()
        if (!wxRes.code) {
          throw new Error('获取微信登录凭证失败')
        }

        // 发送 code 到后端换取 JWT
        const res = await courierWxLogin({ code: wxRes.code })
        this.handleLoginSuccess(res.data)
      } catch (e) {
        this.errorMsg = e.message || '微信登录失败，请重试'
      } finally {
        this.wxLoading = false
      }
    },

    /** 微信小程序：获取 wx.login() code */
    getWxCode() {
      return new Promise((resolve, reject) => {
        // #ifdef MP-WEIXIN
        uni.login({
          provider: 'weixin',
          success: (res) => resolve(res),
          fail: (err) => reject(err)
        })
        // #endif
        // #ifndef MP-WEIXIN
        reject(new Error('非微信环境'))
        // #endif
      })
    },

    /** 登录成功后的统一处理 */
    handleLoginSuccess(data) {
      if (!data || !data.token) {
        this.errorMsg = '登录返回数据异常'
        return
      }

      // 保存 Token 和用户信息
      uni.setStorageSync('token', data.token)
      uni.setStorageSync('courierInfo', JSON.stringify(data.courierInfo || {}))

      uni.showToast({ title: '登录成功', icon: 'success' })

      // 跳转到主页
      setTimeout(() => this.goHome(), 500)
    },

    /** 跳转到主页 */
    goHome() {
      uni.reLaunch({ url: '/pages/Dashboard/Dashboard' })
    }
  }
}
</script>

<style scoped>
.login-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: #0E1217;
  padding: 40px 32px;
  box-sizing: border-box;
}

/* ─── Brand ─── */
.login-brand {
  text-align: center;
  margin-bottom: 48px;
}
.logo-icon {
  width: 72px;
  height: 72px;
  margin: 0 auto 16px;
  background: linear-gradient(135deg, #3B82F6, #8B5CF6);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  box-shadow: 0 8px 30px rgba(59, 130, 246, 0.25);
}
.brand-title {
  display: block;
  font-size: 28px;
  font-weight: 700;
  background: linear-gradient(135deg, #fff, #94A3B8);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}
.brand-sub {
  display: block;
  font-size: 13px;
  color: #64748B;
  margin-top: 6px;
}

/* ─── Form ─── */
.login-form {
  width: 100%;
  max-width: 320px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.field {
  position: relative;
}
.field-icon {
  position: absolute;
  left: 14px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 16px;
  z-index: 1;
}
.field-input {
  width: 100%;
  height: 48px;
  padding: 0 14px 0 42px;
  border: 1.5px solid #2D3548;
  border-radius: 12px;
  font-size: 14px;
  outline: none;
  background: #1C2333;
  color: #F1F5F9;
  box-sizing: border-box;
}
.field-input:focus {
  border-color: #3B82F6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.15);
}
.field-input::placeholder {
  color: #475569;
}

/* ─── Buttons ─── */
.btn-login {
  height: 48px;
  line-height: 48px;
  text-align: center;
  border-radius: 12px;
  background: linear-gradient(135deg, #3B82F6, #2563EB);
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  box-shadow: 0 4px 15px rgba(59, 130, 246, 0.15);
}
.btn-login:active {
  opacity: 0.85;
}

.btn-wx-login {
  height: 48px;
  line-height: 48px;
  text-align: center;
  border-radius: 12px;
  background: #1C2333;
  border: 1.5px solid #2D3548;
  color: #94A3B8;
  font-size: 14px;
  font-weight: 500;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}
.btn-wx-login:active {
  background: #252D3F;
}
.wx-icon {
  font-size: 18px;
}

/* ─── Status ─── */
.error-text {
  color: #EF4444;
  font-size: 13px;
  text-align: center;
}
.version-text {
  text-align: center;
  font-size: 12px;
  color: #64748B;
  margin-top: 8px;
}
</style>
