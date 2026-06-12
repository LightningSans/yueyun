import request from '@/utils/request'

// ─── 用户认证 ───
export function login(data: { username: string; password: string }) {
  return request.post('/user/login', data)
}
export function register(data: any) {
  return request.post('/user/register', data)
}
export function getUserInfo() {
  return request.get('/user/info')
}
export function updateUserInfo(data: any) {
  return request.put('/user/update', data)
}
export function changePassword(data: { oldPassword: string; newPassword: string }) {
  return request.put('/user/password', data)
}

// ─── 商品 ───
export function getProductList(params: any) {
  return request.get('/product/list', { params })
}
export function getProductDetail(id: number) {
  return request.get(`/product/detail/${id}`)
}
export function getCategoryList() {
  return request.get('/product/category/list')
}

// ─── 购物车 ───
export function getCartList() {
  return request.get('/cart/list')
}
export function addCart(data: { productId: number; quantity: number }) {
  return request.post('/cart/add', data)
}
export function updateCart(data: { id: number; quantity?: number; selected?: boolean }) {
  return request.put('/cart/update', data)
}
export function deleteCart(id: number) {
  return request.delete(`/cart/delete/${id}`)
}
export function clearCart() {
  return request.delete('/cart/clear')
}

// ─── 地址 ───
export function getAddressList() {
  return request.get('/address/list')
}
export function saveAddress(data: any) {
  return request.post('/address/save', data)
}
export function deleteAddress(id: number) {
  return request.delete(`/address/delete/${id}`)
}
export function setDefaultAddress(id: number) {
  return request.put(`/address/default/${id}`)
}

// ─── 订单 ───
export function createOrder(data: { addressId: number; cartItemIds: number[] }) {
  return request.post('/order/create', data)
}
export function getOrderList(params: any) {
  return request.get('/order/list', { params })
}
export function getOrderDetail(id: number) {
  return request.get(`/order/detail/${id}`)
}
export function cancelOrder(id: number, reason?: string) {
  return request.post(`/order/cancel/${id}`, { cancelReason: reason })
}
export function confirmOrder(id: number) {
  return request.post(`/order/confirm/${id}`)
}
export function deleteOrder(id: number) {
  return request.post(`/order/delete/${id}`)
}

// ─── 支付 ───
export function payOrder(data: { orderId: number; paymentMethod?: string }) {
  return request.post('/payment/pay', data)
}
export function getPaymentRecord(orderId: number) {
  return request.get(`/payment/record/${orderId}`)
}

// ─── 评价 ───
export function createReview(data: any) {
  return request.post('/review/create', data)
}
export function getReviewList(productId: number, params: any) {
  return request.get(`/review/list/${productId}`, { params })
}

// ════════════════════════════════════════════
//  AI 智能客服（新接口）
// ════════════════════════════════════════════

/** 热点问题 */
export function getHotQuestions(limit = 3) {
  return request.get('/ai/hot-questions', { params: { limit } })
}

/** 停止生成 */
export function stopGeneration(data: { sessionId: string }) {
  return request.post('/ai/stop', data)
}

/**
 * 流式对话（SSE）
 *
 * 后端新地址：POST /api/ai/chat
 * 事件格式：
 *   event: delta   → {"eventType":1001,"eventData":"文本块"}
 *   event: done    → {"eventType":1002}
 *   event: error   → {"eventType":1003,"eventData":"错误信息或商品卡片"}
 *   1003 双重用途：
 *     - 若 eventData 为字符串 → 错误信息
 *     - 若 eventData 为对象 {"requestId":"xxx","products":[...]} → 商品卡片
 */
export function aiChatStream(
  data: { message: string; sessionId?: string },
  onDelta: (text: string) => void,
  onDone: (title?: string) => void,
  onError: (err: string) => void,
  onProducts?: (products: any[]) => void
): AbortController {
  const controller = new AbortController()

  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
  }
  const token = localStorage.getItem('token')
  if (token) {
    headers['Authorization'] = 'Bearer ' + token
  }

  fetch('/api/ai/chat', {
    method: 'POST',
    headers,
    body: JSON.stringify(data),
    signal: controller.signal
  }).then(async (response) => {
    if (!response.ok) {
      onError('网络异常: ' + response.status)
      return
    }

    const reader = response.body?.getReader()
    if (!reader) {
      onError('浏览器不支持流式读取')
      return
    }

    const decoder = new TextDecoder()
    let buffer = ''
    let doneReceived = false

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      buffer += decoder.decode(value, { stream: true })

      // 解析 SSE 格式
      const lines = buffer.split('\n\n')
      buffer = lines.pop() || ''

      for (const block of lines) {
        const lines2 = block.split('\n')
        let eventType = 'message'
        let dataStr = ''

        for (const line of lines2) {
          // ★ 兼容 Spring SseEmitter 的格式（event:xxx 无空格）和标准格式（event: xxx 有空格）
          if (line.startsWith('event:') || line.startsWith('event: ')) {
            eventType = line.includes(':') ? line.substring(line.indexOf(':') + 1).trim() : 'message'
          } else if (line.startsWith('data:') || line.startsWith('data: ')) {
            dataStr = line.includes(':') ? line.substring(line.indexOf(':') + 1).trim() : ''
          }
        }

        if (!dataStr) continue

        try {
          const parsed = JSON.parse(dataStr)
          const evtType = parsed.eventType

          switch (evtType) {
            case 1001: // delta
              onDelta(parsed.eventData || '')
              break
            case 1002: // done
              doneReceived = true
              onDone(parsed.title || undefined)
              break
            case 1003: // 错误 或 商品卡片
              const ed = parsed.eventData
              if (typeof ed === 'string') {
                onError(ed || '未知错误')
              } else if (ed && ed.products && Array.isArray(ed.products) && onProducts) {
                onProducts(ed.products)
              } else {
                onError('未知错误')
              }
              break
          }
        } catch {
          // 忽略解析错误
        }
      }
    }

    if (!doneReceived) {
      onDone()
    }
  }).catch((err) => {
    if (err.name !== 'AbortError') {
      onError('网络开小差了，请稍后再试')
    }
  })

  return controller
}

// ════════════════════════════════════════════
//  会话管理（第6轮）
// ════════════════════════════════════════════

/** 获取会话列表（按时间分组） */
export function getSessions() {
  return request.get('/ai/sessions')
}

/** 修改会话标题 */
export function updateSessionTitle(sessionId: string, title: string) {
  return request.put(`/ai/session/${sessionId}/title`, { title })
}
