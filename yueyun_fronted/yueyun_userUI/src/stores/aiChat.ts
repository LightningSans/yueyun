import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { aiChatStream, getHotQuestions, stopGeneration, getSessions, updateSessionTitle } from '@/api'

export interface ChatMessage {
  role: 'user' | 'assistant'
  content: string
  time: string
  products?: any[]
}

export interface ChatSession {
  id: string
  title: string
  messages: ChatMessage[]
  createdAt: string
  /** 后端 sessionId */
  sessionId?: string
}

export interface HotQuestion {
  question: string
  answer: string
}

/** 后端返回的分组会话项 */
export interface SessionItem {
  id: number
  sessionId: string
  title: string
  createTime: string
  updateTime: string
}

export const useAiChatStore = defineStore('aiChat', () => {
  const visible = ref(false)
  const sessions = ref<ChatSession[]>(loadSessions())
  const activeSessionId = ref<string>('')
  const loading = ref(false)
  const historyCollapsed = ref(true)
  const streamingContent = ref('')
  const hotQuestions = ref<HotQuestion[]>([])
  const hotQuestionsLoading = ref(false)
  const currentController = ref<AbortController | null>(null)

  // 服务端分组会话
  const serverSessions = ref<{ today: SessionItem[]; week: SessionItem[]; month: SessionItem[] }>({
    today: [],
    week: [],
    month: []
  })

  // 标题编辑状态
  const editingTitle = ref<string | null>(null) // sessionId
  const editingValue = ref('')

  const activeSession = computed(() => {
    return sessions.value.find(s => s.id === activeSessionId.value)
  })

  const messages = computed(() => {
    return activeSession.value?.messages || []
  })

  async function loadHotQuestions() {
    hotQuestionsLoading.value = true
    try {
      const res = await getHotQuestions(3)
      hotQuestions.value = res.data || []
    } catch {} finally { hotQuestionsLoading.value = false }
  }

  /** 从服务端加载会话列表（按时间分组） */
  async function loadServerSessions(): Promise<void> {
    try {
      const res = await getSessions()
      if (res.data) {
        serverSessions.value = res.data as any
      }
    } catch { /* 静默 */ }
  }

  /** 将服务端 AI 生成的标题同步到本地 session */
  function syncServerTitle(localSession: ChatSession | undefined) {
    if (!localSession?.sessionId) return
    const allServer = [
      ...serverSessions.value.today,
      ...serverSessions.value.week,
      ...serverSessions.value.month
    ]
    const match = allServer.find(s => s.sessionId === localSession.sessionId)
    if (match && match.title) {
      localSession.title = match.title
      saveSessions()
    }
  }

  function toggle() {
    visible.value = !visible.value
    if (visible.value) {
      loadHotQuestions()
      loadServerSessions()
    }
  }
  function open() { visible.value = true; loadHotQuestions(); loadServerSessions() }
  function close() { visible.value = false }

  function newSession() {
    const id = 'chat_' + Date.now()
    const now = new Date().toLocaleString('zh-CN')
    sessions.value.unshift({
      id,
      title: '新对话',
      messages: [
        { role: 'assistant', content: '你好！我是悦选商城的 AI 助手，可以帮你解答购物中的任何问题。', time: now }
      ],
      createdAt: now
    })
    activeSessionId.value = id
    saveSessions()
  }

  /** 切换到服务端会话 */
  function switchToServerSession(session: SessionItem) {
    const existingId = session.sessionId
    // 检查是否已在本地
    let local = sessions.value.find(s => s.id === existingId || s.sessionId === existingId)
    if (!local) {
      const now = new Date().toLocaleString('zh-CN')
      // 从标题中提取纯文本（去掉 _时间戳）
      const title = session.title ? session.title.replace(/_\d{12}$/, '') : '历史对话'
      local = {
        id: existingId,
        title,
        sessionId: existingId,
        messages: [
          { role: 'assistant', content: '📝 已切换到历史会话，继续上次的对话吧～', time: now }
        ],
        createdAt: session.createTime || now
      }
      sessions.value.unshift(local)
      saveSessions()
    }
    activeSessionId.value = local.id
    streamingContent.value = ''
    loadServerSessions()
  }

  function switchSession(id: string) {
    activeSessionId.value = id
    streamingContent.value = ''
  }

  function deleteSession(id: string) {
    sessions.value = sessions.value.filter(s => s.id !== id)
    if (activeSessionId.value === id) {
      activeSessionId.value = sessions.value.length > 0 ? sessions.value[0].id : (newSession(), activeSessionId.value)
    }
    saveSessions()
  }

  /** 开始编辑标题 */
  function startEditTitle(sessionId: string, currentTitle: string) {
    editingTitle.value = sessionId
    // 去掉 _时间戳 后缀
    editingValue.value = currentTitle.replace(/_\d{12}$/, '')
  }

  /** 保存标题编辑 */
  async function saveEditTitle(sessionId: string) {
    const newTitle = editingValue.value.trim()
    if (!newTitle) return
    try {
      await updateSessionTitle(sessionId, newTitle)
      // 更新本地
      const session = sessions.value.find(s => s.id === sessionId || s.sessionId === sessionId)
      if (session) {
        const timestamp = new Date().toISOString().replace(/[^0-9]/g, '').slice(0, 12)
        session.title = newTitle + '_' + timestamp
        saveSessions()
      }
      await loadServerSessions()
    } catch {}
    editingTitle.value = null
    editingValue.value = ''
  }

  /** 取消编辑标题 */
  function cancelEditTitle() {
    editingTitle.value = null
    editingValue.value = ''
  }

  async function stop() {
    if (!currentController.value) return
    currentController.value.abort()
    currentController.value = null
    loading.value = false
    streamingContent.value = ''
    if (activeSessionId.value) {
      try { await stopGeneration({ sessionId: activeSessionId.value }) } catch {}
    }
  }

  async function sendMessage(text: string) {
    if (!text.trim() || loading.value) return
    if (!activeSession.value) newSession()

    const session = sessions.value.find(s => s.id === activeSessionId.value)
    if (!session) return

    const now = new Date().toLocaleString('zh-CN')

    session.messages.push({ role: 'user', content: text, time: now })
    // 仅在第一条消息时设置临时标题（后续由后端 AI 生成并同步）
    if (session.messages.filter(m => m.role === 'user').length === 1) {
      session.title = text.length > 20 ? text.slice(0, 20) + '...' : text
    }

    const msg: ChatMessage = { role: 'assistant', content: '', time: new Date().toLocaleString('zh-CN') }
    session.messages.push(msg)
    loading.value = true
    streamingContent.value = ''
    saveSessions()

    let hasStreamed = false

    const controller = aiChatStream(
      { message: text, sessionId: activeSessionId.value },
      (chunk) => {
        hasStreamed = true
        streamingContent.value += chunk
        msg.content += chunk
      },
      (titleFromServer) => {
        loading.value = false
        if (!msg.content.trim()) msg.content = '😅 回复为空，请稍后再试。'
        streamingContent.value = ''
        currentController.value = null
        // 如果 done 事件带来了标题，直接更新本地 session
        if (titleFromServer && session) {
          session.title = titleFromServer
        }
        saveSessions()
        // 刷新服务端会话列表并同步标题（作为兜底）
        loadServerSessions().then(() => syncServerTitle(session))
      },
      async (err) => {
        if (!hasStreamed) {
          msg.content = err || '😅 网络开小差了，请稍后再试。'
        } else {
          msg.content = streamingContent.value + '\n\n😅 ' + (err || '回复被中断')
        }
        loading.value = false
        streamingContent.value = ''
        currentController.value = null
        saveSessions()
      },
      (products) => {
        const activeMsg = session.messages[session.messages.length - 1]
        if (activeMsg) activeMsg.products = products
      }
    )

    currentController.value = controller
  }

  function saveSessions() {
    localStorage.setItem('ai_chat_sessions', JSON.stringify(sessions.value))
    localStorage.setItem('ai_chat_active', activeSessionId.value)
  }

  function loadSessions(): ChatSession[] {
    try {
      const data = localStorage.getItem('ai_chat_sessions')
      return data ? JSON.parse(data) : []
    } catch { return [] }
  }

  const savedId = localStorage.getItem('ai_chat_active') || ''
  const loaded = loadSessions()
  sessions.value = loaded
  if (savedId && loaded.some(s => s.id === savedId)) {
    activeSessionId.value = savedId
  }

  return {
    visible, sessions, activeSessionId, activeSession, messages,
    loading, historyCollapsed, streamingContent,
    hotQuestions, hotQuestionsLoading,
    serverSessions, editingTitle, editingValue,
    toggle, open, close,
    newSession, switchToServerSession,
    switchSession, deleteSession, sendMessage, stop,
    loadHotQuestions, loadServerSessions,
    startEditTitle, saveEditTitle, cancelEditTitle
  }
})
