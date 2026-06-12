<template>
  <!-- 悬浮球 -->
  <div class="ai-float-ball" @click="store.toggle()" :class="{ active: store.visible }">
    <span class="ball-icon">🤖</span>
  </div>

  <!-- 对话框遮罩 -->
  <div v-if="store.visible" class="ai-overlay" @click.self="store.close()">
    <div class="ai-dialog">
      <!-- 顶部 -->
      <div class="ai-header">
        <div class="ai-header-left">
          <span class="ai-header-icon">🤖</span>
          <span class="ai-header-title">AI 客服助手</span>
        </div>
        <div class="ai-header-actions">
          <button class="ai-btn-icon" @click="store.newSession()" title="新建对话">✏️</button>
          <button class="ai-btn-icon" @click="store.toggle()" title="关闭">✕</button>
        </div>
      </div>

      <!-- 历史折叠区（时间分组） -->
      <div class="ai-history" v-if="hasHistorySessions">
        <div class="ai-history-toggle" @click="store.historyCollapsed = !store.historyCollapsed">
          <span>历史对话</span>
          <span class="toggle-arrow" :class="{ collapsed: store.historyCollapsed }">▼</span>
        </div>
        <div class="ai-history-list" v-show="!store.historyCollapsed">
          <!-- 本地活跃会话 -->
          <div v-for="s in store.sessions" :key="s.id" class="ai-history-item"
            :class="{ active: s.id === store.activeSessionId }"
            @click="store.switchSession(s.id)">
            <span class="history-title">{{ s.title }}</span>
            <button class="history-del" @click.stop="store.deleteSession(s.id)" title="删除">×</button>
          </div>

          <!-- 服务端分组 -->
          <div v-if="store.serverSessions.today.length > 0" class="history-group">
            <div class="history-group-label">今天</div>
            <div v-for="s in store.serverSessions.today" :key="s.sessionId"
              class="ai-history-item server"
              :class="{ active: s.sessionId === store.activeSessionId || s.sessionId === store.activeSession?.sessionId }"
              @click="store.switchToServerSession(s)">
              <span class="history-title">{{ formatTitle(s.title) }}</span>
            </div>
          </div>
          <div v-if="store.serverSessions.week.length > 0" class="history-group">
            <div class="history-group-label">一周内</div>
            <div v-for="s in store.serverSessions.week" :key="s.sessionId"
              class="ai-history-item server"
              :class="{ active: s.sessionId === store.activeSessionId || s.sessionId === store.activeSession?.sessionId }"
              @click="store.switchToServerSession(s)">
              <span class="history-title">{{ formatTitle(s.title) }}</span>
            </div>
          </div>
          <div v-if="store.serverSessions.month.length > 0" class="history-group">
            <div class="history-group-label">三个月内</div>
            <div v-for="s in store.serverSessions.month" :key="s.sessionId"
              class="ai-history-item server"
              :class="{ active: s.sessionId === store.activeSessionId || s.sessionId === store.activeSession?.sessionId }"
              @click="store.switchToServerSession(s)">
              <span class="history-title">{{ formatTitle(s.title) }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 消息列表 -->
      <div class="ai-messages" ref="msgRef">

        <!-- 欢迎页 -->
        <div v-if="!store.activeSession" class="ai-welcome">
          <div class="ai-welcome-icon">🤖</div>
          <p>你好！我是悦选商城的 AI 客服助手</p>
          <p class="ai-welcome-sub">可以帮你解答购物中的任何问题</p>
          <div v-if="store.hotQuestions.length > 0" class="hot-section">
            <div class="hot-title">💡 大家都在问</div>
            <div class="hot-list">
              <div v-for="(item, idx) in store.hotQuestions" :key="idx" class="hot-item"
                @click="quickAsk(item.question)">
                <span class="hot-num">{{ idx + 1 }}</span>
                <span class="hot-text">{{ item.question }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 消息气泡 -->
        <div v-for="(msg, idx) in store.messages" :key="idx" class="ai-msg" :class="msg.role"
             v-show="msg.content || msg.products || !(store.loading && idx === store.messages.length - 1 && msg.role === 'assistant')">
          <div class="msg-avatar">{{ msg.role === 'assistant' ? '🤖' : '👤' }}</div>
          <div class="msg-content">
            <div v-if="msg.content" class="msg-bubble">
              {{ msg.content }}
              <span v-if="store.loading && idx === store.messages.length - 1 && msg.role === 'assistant' && store.streamingContent" class="stream-cursor">▍</span>
            </div>
            <div v-if="msg.products && msg.products.length > 0" class="product-cards">
              <div v-for="(prod, pIdx) in msg.products" :key="pIdx" class="product-card"
                   @click="goToDetail(prod.id)">
                  <img v-if="showProductImage(prod)" :src="prod.mainImage" class="card-img-real" @error="onImgError(prod)" />
                  <span v-else class="card-img-placeholder">{{ getCategoryEmoji(prod.name, prod.brand) }}</span>
                <div class="card-info">
                  <div class="card-name">{{ prod.name }}</div>
                  <div v-if="prod.brand" class="card-brand">{{ prod.brand }}</div>
                  <div class="card-price-row">
                    <span class="card-price">¥{{ formatPrice(prod.price) }}</span>
                    <span v-if="prod.originalPrice && prod.originalPrice > prod.price" class="card-old-price">¥{{ formatPrice(prod.originalPrice) }}</span>
                  </div>
                  <div class="card-meta">
                    <span v-if="prod.sales != null">已售 {{ prod.sales }}</span>
                    <span v-if="prod.stock != null">库存 {{ prod.stock }}</span>
                  </div>
                </div>
              </div>
            </div>
            <div class="msg-time">{{ msg.time }}</div>
          </div>
        </div>

        <!-- 加载中 -->
        <div v-if="store.loading && !store.streamingContent && !(store.messages.length > 0 && store.messages[store.messages.length - 1].products)" class="ai-msg assistant">
          <div class="msg-avatar">🤖</div>
          <div class="msg-content">
            <div class="msg-bubble thinking">
              <span class="dot-pulse"></span>
            </div>
          </div>
        </div>
      </div>

      <!-- 输入区 -->
      <div class="ai-input-area">
        <button v-if="store.loading" class="ai-stop-btn" @click="store.stop()" title="停止生成">⏹ 停止</button>
        <input v-else v-model="inputText" class="ai-input" placeholder="输入你想了解的问题..."
          @keydown.enter="send" :disabled="store.loading" />
        <button class="ai-send-btn" @click="send" :disabled="store.loading || !inputText.trim()">
          {{ store.loading ? '...' : '发送' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAiChatStore } from '@/stores/aiChat'

const store = useAiChatStore()
const router = useRouter()
const inputText = ref('')
const msgRef = ref<HTMLElement | null>(null)
const failedImages = ref<Set<number>>(new Set())

function onImgError(prod: any) {
  failedImages.value.add(prod.id)
  // 触发响应式更新
  failedImages.value = new Set(failedImages.value)
}

function showProductImage(prod: any): boolean {
  return prod.mainImage && !failedImages.value.has(prod.id)
}

const hasHistorySessions = computed(() =>
  store.sessions.length > 0 ||
  store.serverSessions.today.length > 0 ||
  store.serverSessions.week.length > 0 ||
  store.serverSessions.month.length > 0
)

function formatTitle(title: string): string {
  if (!title) return '新对话'
  return title.replace(/_\d{12}$/, '')
}

async function send() {
  const text = inputText.value.trim()
  if (!text || store.loading) return
  inputText.value = ''
  await store.sendMessage(text)
  scrollToBottom()
}

function quickAsk(text: string) {
  inputText.value = text
  send()
}

function goToDetail(id: number | string) {
  store.close()
  router.push('/detail/' + id)
}

function formatPrice(price: any): string {
  if (!price) return '0'
  const num = typeof price === 'string' ? parseFloat(price) : price
  return num.toLocaleString('zh-CN', { minimumFractionDigits: 0, maximumFractionDigits: 2 })
}

function getCategoryEmoji(name: string, brand?: string): string {
  const n = (name || '').toLowerCase()
  const b = (brand || '').toLowerCase()
  if (n.includes('手机') || n.includes('iphone') || n.includes('华为') || b.includes('apple')) return '📱'
  if (n.includes('电脑') || n.includes('macbook') || n.includes('thinkpad') || n.includes('笔记本')) return '💻'
  if (n.includes('t恤') || n.includes('连衣裙') || n.includes('服饰')) return '👕'
  if (n.includes('坚果') || n.includes('肉干') || n.includes('零食')) return '🍪'
  if (n.includes('精华') || n.includes('护肤') || n.includes('资生堂') || n.includes('红腰子')) return '🧴'
  if (n.includes('香薰') || n.includes('无印良品')) return '🏺'
  return '📦'
}

function scrollToBottom() {
  nextTick(() => {
    if (msgRef.value) msgRef.value.scrollTop = msgRef.value.scrollHeight
  })
}

watch(() => store.messages.length, scrollToBottom)
watch(() => store.streamingContent, scrollToBottom)
</script>

<style scoped>
/* ─── 悬浮球 ─── */
.ai-float-ball {
  position: fixed; right: 28px; bottom: 32px; width: 56px; height: 56px;
  border-radius: 50%; background: linear-gradient(135deg, #E85D3A, #F4A261);
  display: flex; align-items: center; justify-content: center; cursor: pointer;
  z-index: 999; box-shadow: 0 4px 20px rgba(232,93,58,.35);
  transition: all .25s cubic-bezier(.34,1.56,.64,1); user-select: none;
}
.ai-float-ball:hover { transform: scale(1.08); box-shadow: 0 6px 28px rgba(232,93,58,.45); }
.ai-float-ball:active { transform: scale(.95); }
.ai-float-ball.active { transform: scale(.9); opacity: 0; pointer-events: none; }
.ball-icon { font-size: 26px; line-height: 1; }

/* ─── 遮罩 ─── */
.ai-overlay { position: fixed; inset: 0; z-index: 9999; background: rgba(0,0,0,.25); backdrop-filter: blur(2px); display: flex; align-items: flex-end; justify-content: flex-end; padding: 20px; animation: fadeIn .2s ease; }
@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }

/* ─── 对话框 ─── */
.ai-dialog { width: 400px; height: 600px; max-height: 85vh; background: #fff; border-radius: 16px; box-shadow: 0 16px 60px rgba(0,0,0,.18); display: flex; flex-direction: column; overflow: hidden; animation: slideUp .25s cubic-bezier(.34,1.56,.64,1); }
@keyframes slideUp { from { opacity: 0; transform: translateY(20px) scale(.96); } to { opacity: 1; transform: translateY(0) scale(1); } }

/* ─── 头部 ─── */
.ai-header { display: flex; align-items: center; justify-content: space-between; padding: 14px 18px; background: linear-gradient(135deg, #E85D3A, #F4A261); color: #fff; flex-shrink: 0; }
.ai-header-left { display: flex; align-items: center; gap: 8px; }
.ai-header-icon { font-size: 20px; }
.ai-header-title { font-size: 15px; font-weight: 600; }
.ai-header-actions { display: flex; gap: 4px; }
.ai-btn-icon { width: 30px; height: 30px; border: none; background: rgba(255,255,255,.15); border-radius: 8px; font-size: 14px; cursor: pointer; display: flex; align-items: center; justify-content: center; transition: background .15s; color: #fff; }
.ai-btn-icon:hover { background: rgba(255,255,255,.3); }

/* ─── 历史折叠 ─── */
.ai-history { background: #F9FAFB; border-bottom: 1px solid #E5E7EB; flex-shrink: 0; }
.ai-history-toggle { display: flex; justify-content: space-between; align-items: center; padding: 10px 18px; font-size: 12px; color: #6B7280; cursor: pointer; user-select: none; transition: background .1s; }
.ai-history-toggle:hover { background: #F3F4F6; }
.toggle-arrow { font-size: 10px; transition: transform .2s; }
.toggle-arrow.collapsed { transform: rotate(-90deg); }
.ai-history-list { max-height: 160px; overflow-y: auto; padding: 0 12px 8px; }

/* 时间分组 */
.history-group { margin-bottom: 4px; }
.history-group-label { font-size: 10px; font-weight: 600; color: #9CA3AF; padding: 6px 10px 2px; text-transform: uppercase; letter-spacing: .5px; }

.ai-history-item { display: flex; align-items: center; gap: 6px; padding: 7px 10px; border-radius: 8px; font-size: 12px; cursor: pointer; transition: background .1s; margin-bottom: 2px; }
.ai-history-item:hover { background: #E5E7EB; }
.ai-history-item.active { background: #FEE2E2; color: #E85D3A; }
.ai-history-item.server { padding-left: 14px; }
.history-title { flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.history-del { border: none; background: none; font-size: 14px; cursor: pointer; color: #9CA3AF; padding: 0 2px; line-height: 1; opacity: 0; transition: opacity .1s; }
.ai-history-item:hover .history-del { opacity: 1; }
.history-del:hover { color: #EF4444; }

/* ─── 消息区 ─── */
.ai-messages { flex: 1; overflow-y: auto; padding: 16px 18px; background: #F9FAFB; }
.ai-messages::-webkit-scrollbar { width: 4px; }
.ai-messages::-webkit-scrollbar-thumb { background: #D1D5DB; border-radius: 4px; }

/* ─── 热点问题 ─── */
.hot-section { margin-top: 20px; text-align: left; }
.hot-title { font-size: 12px; color: #9CA3AF; margin-bottom: 8px; font-weight: 500; }
.hot-list { display: flex; flex-direction: column; gap: 4px; }
.hot-item { display: flex; align-items: center; gap: 8px; padding: 8px 12px; background: #fff; border-radius: 8px; border: 1px solid #E5E7EB; cursor: pointer; transition: all .15s; }
.hot-item:hover { border-color: #E85D3A; background: #FFF4F0; }
.hot-num { width: 18px; height: 18px; border-radius: 50%; background: linear-gradient(135deg, #E85D3A, #F4A261); color: #fff; font-size: 10px; font-weight: 700; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.hot-text { font-size: 12px; color: #374151; line-height: 1.4; }

/* ─── 欢迎 ─── */
.ai-welcome { text-align: center; padding: 24px 16px 12px; }
.ai-welcome-icon { font-size: 48px; margin-bottom: 12px; }
.ai-welcome p { font-size: 14px; color: #374151; }
.ai-welcome-sub { font-size: 12px; color: #9CA3AF; margin-top: 4px !important; }

/* ─── 消息气泡 ─── */
.ai-msg { display: flex; gap: 10px; margin-bottom: 16px; }
.msg-avatar { width: 32px; height: 32px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 16px; flex-shrink: 0; background: #F3F4F6; }
.ai-msg.assistant .msg-avatar { background: #FFF4F0; }
.ai-msg.user { flex-direction: row-reverse; }
.ai-msg.user .msg-avatar { background: #FEE2E2; }
.msg-content { max-width: 80%; }
.msg-bubble { padding: 10px 14px; border-radius: 12px; font-size: 13px; line-height: 1.6; white-space: pre-wrap; word-break: break-word; }
.ai-msg.assistant .msg-bubble { background: #fff; border: 1px solid #E5E7EB; color: #1F2937; border-top-left-radius: 4px; }
.ai-msg.user .msg-bubble { background: linear-gradient(135deg, #E85D3A, #F4A261); color: #fff; border-top-right-radius: 4px; }
.msg-time { font-size: 10px; color: #9CA3AF; margin-top: 4px; }
.ai-msg.user .msg-time { text-align: right; }

/* ─── 商品卡片 ─── */
.product-cards { display: flex; flex-direction: column; gap: 8px; margin-top: 8px; }
.product-card { display: flex; gap: 10px; padding: 10px; background: #fff; border: 1px solid #E5E7EB; border-radius: 10px; cursor: pointer; transition: all .15s; }
.product-card:hover { border-color: #E85D3A; box-shadow: 0 2px 8px rgba(232,93,58,.1); }
.card-img { width: 60px; height: 60px; border-radius: 8px; background: linear-gradient(135deg, #FFF4F0, #FFF); display: flex; align-items: center; justify-content: center; flex-shrink: 0; font-size: 28px; overflow: hidden; }
.card-img-real { width: 100%; height: 100%; object-fit: cover; }
.card-info { flex: 1; min-width: 0; }
.card-name { font-size: 12px; font-weight: 600; color: #1F2937; line-height: 1.4; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.card-brand { font-size: 10px; color: #9CA3AF; margin-top: 2px; }
.card-price-row { display: flex; align-items: baseline; gap: 4px; margin-top: 4px; }
.card-price { font-size: 15px; font-weight: 700; color: #EF4444; }
.card-old-price { font-size: 11px; color: #9CA3AF; text-decoration: line-through; }
.card-meta { display: flex; gap: 8px; font-size: 10px; color: #9CA3AF; margin-top: 2px; }

/* ─── 加载动画 ─── */
.thinking { padding: 14px 20px !important; }
.dot-pulse { display: inline-block; width: 8px; height: 8px; border-radius: 50%; background: #E85D3A; animation: pulse 1.2s ease-in-out infinite; }
.stream-cursor { display: inline-block; font-weight: 700; color: #E85D3A; animation: blink 0.8s step-end infinite; }
@keyframes blink { 0%, 100% { opacity: 1; } 50% { opacity: 0; } }
@keyframes pulse { 0%, 100% { opacity: .3; transform: scale(.8); } 50% { opacity: 1; transform: scale(1); } }

/* ─── 输入区 ─── */
.ai-input-area { display: flex; gap: 8px; padding: 12px 18px; border-top: 1px solid #E5E7EB; background: #fff; flex-shrink: 0; }
.ai-input { flex: 1; height: 40px; border: 1px solid #E5E7EB; border-radius: 10px; padding: 0 14px; font-size: 13px; outline: none; transition: border-color .15s; font-family: inherit; }
.ai-input:focus { border-color: #E85D3A; box-shadow: 0 0 0 3px rgba(232,93,58,.1); }
.ai-input:disabled { background: #F3F4F6; }
.ai-send-btn { height: 40px; padding: 0 18px; border: none; border-radius: 10px; background: linear-gradient(135deg, #E85D3A, #F4A261); color: #fff; font-size: 13px; font-weight: 500; cursor: pointer; transition: all .15s; font-family: inherit; white-space: nowrap; }
.ai-send-btn:hover { opacity: .9; }
.ai-send-btn:disabled { opacity: .5; cursor: not-allowed; }
.ai-stop-btn { flex: 1; height: 40px; border: 1.5px solid #EF4444; border-radius: 10px; background: #FEF2F2; color: #EF4444; font-size: 13px; font-weight: 500; cursor: pointer; transition: all .15s; font-family: inherit; }
.ai-stop-btn:hover { background: #FEE2E2; }

@media (max-width: 640px) {
  .ai-dialog { width: 100%; height: 100vh; max-height: 100vh; border-radius: 0; }
  .ai-overlay { padding: 0; }
}
</style>
