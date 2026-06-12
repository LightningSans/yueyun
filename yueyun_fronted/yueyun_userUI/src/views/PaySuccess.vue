<template>
  <div class="pay-success">
    <div class="icon-circle">✅</div>
    <h2>支付成功！</h2>
    <p>订单已提交，我们将尽快为您发货</p>
    <div class="amount-box" v-if="amount">¥{{ amount }}</div>
    <div class="btn-group">
      <button class="btn outline" @click="$router.push('/orders/' + orderId)">查看订单</button>
      <button class="btn primary" @click="$router.push('/')">返回首页</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const orderId = ref(route.query.orderId || '')
const amount = ref(route.query.amount || '')
</script>

<style scoped>
.pay-success { text-align: center; padding: 60px 20px; }
.icon-circle { width: 80px; height: 80px; border-radius: 50%; background: #ECFDF5; display: flex; align-items: center; justify-content: center; margin: 0 auto 20px; font-size: 40px; }
.pay-success h2 { font-size: 24px; font-weight: 700; margin-bottom: 8px; }
.pay-success p { color: var(--text-light); margin-bottom: 16px; }
.amount-box { font-size: 32px; font-weight: 700; color: var(--danger); margin-bottom: 24px; }
.btn-group { display: flex; gap: 12px; justify-content: center; }
.btn { padding: 10px 28px; border-radius: var(--radius-xs); font-size: 14px; font-weight: 500; cursor: pointer; font-family: inherit; }
.btn.primary { background: var(--primary); color: #fff; border: none; }
.btn.outline { border: 1.5px solid var(--border); color: var(--text-secondary); background: none; }
</style>
