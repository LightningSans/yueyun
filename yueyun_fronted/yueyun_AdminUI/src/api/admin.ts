import request from '@/utils/request'

// ─── 认证 ───
export function login(data: { username: string; password: string }) {
  return request.post('/admin/login', data)
}

export function getAdminInfo() {
  return request.get('/admin/info')
}

export function logout() {
  return request.post('/admin/logout')
}

// ─── 工作台 ───
export function getDashboardStat() {
  return request.get('/admin/dashboard/stat')
}

export function getOrderTrend() {
  return request.get('/admin/dashboard/orderTrend')
}

export function getOrderDist() {
  return request.get('/admin/dashboard/orderDist')
}

// ─── 用户管理 ───
export function getUserList(params: any) {
  return request.get('/admin/user/list', { params })
}

export function getUserDetail(id: number) {
  return request.get(`/admin/user/detail/${id}`)
}

export function updateUser(data: any) {
  return request.put('/admin/user/update', data)
}

export function freezeUser(id: number) {
  return request.post(`/admin/user/freeze/${id}`)
}

export function unfreezeUser(id: number) {
  return request.post(`/admin/user/unfreeze/${id}`)
}

// ─── 管理员管理 ───
export function getAdminList(params: any) {
  return request.get('/admin/admin/list', { params })
}

export function createAdmin(data: any) {
  return request.post('/admin/admin/create', data)
}

export function updateAdmin(data: any) {
  return request.put('/admin/admin/update', data)
}

export function toggleAdminStatus(id: number) {
  return request.post(`/admin/admin/toggleStatus/${id}`)
}

// ─── 分类管理 ───
export function getCategoryList() {
  return request.get('/admin/category/list')
}

export function createCategory(data: any) {
  return request.post('/admin/category/create', data)
}

export function updateCategory(data: any) {
  return request.put('/admin/category/update', data)
}

export function deleteCategory(id: number) {
  return request.delete(`/admin/category/delete/${id}`)
}

// ─── 商品管理 ───
export function getProductList(params: any) {
  return request.get('/admin/product/list', { params })
}

export function getProductDetail(id: number) {
  return request.get(`/admin/product/detail/${id}`)
}

export function createProduct(data: any) {
  return request.post('/admin/product/create', data)
}

export function updateProduct(data: any) {
  return request.put('/admin/product/update', data)
}

export function upProduct(id: number) {
  return request.post(`/admin/product/up/${id}`)
}

export function downProduct(id: number) {
  return request.post(`/admin/product/down/${id}`)
}

export function batchUpProduct(ids: number[]) {
  return request.post('/admin/product/batchUp', ids)
}

export function batchDownProduct(ids: number[]) {
  return request.post('/admin/product/batchDown', ids)
}

export function deleteProduct(id: number) {
  return request.delete(`/admin/product/delete/${id}`)
}

// ─── 订单管理 ───
export function getOrderList(params: any) {
  return request.get('/admin/order/list', { params })
}

export function getOrderDetail(id: number) {
  return request.get(`/admin/order/detail/${id}`)
}

export function assignCourier(orderId: number, courierId: number) {
  return request.post(`/admin/order/assign/${orderId}`, { courierId })
}

// ─── 配送员管理 ───
export function getCourierList(params: any) {
  return request.get('/admin/courier/list', { params })
}

export function createCourier(data: any) {
  return request.post('/admin/courier/create', data)
}

export function updateCourier(data: any) {
  return request.put('/admin/courier/update', data)
}

export function disableCourier(id: number) {
  return request.post(`/admin/courier/disable/${id}`)
}

export function enableCourier(id: number) {
  return request.post(`/admin/courier/enable/${id}`)
}

export function resetCourierPwd(id: number) {
  return request.post(`/admin/courier/resetPwd/${id}`)
}

// ─── 支付记录 ───
export function getPaymentList(params: any) {
  return request.get('/admin/payment/list', { params })
}

// ─── 评价管理 ───
export function getReviewList(params: any) {
  return request.get('/admin/review/list', { params })
}

export function hideReview(id: number) {
  return request.post(`/admin/review/hide/${id}`)
}

export function showReview(id: number) {
  return request.post(`/admin/review/show/${id}`)
}

export function batchHideReview(ids: number[]) {
  return request.post('/admin/review/batchHide', ids)
}

export function batchShowReview(ids: number[]) {
  return request.post('/admin/review/batchShow', ids)
}

// ─── 数据统计 ───
export function getStatisticsOverview() {
  return request.get('/admin/statistics/overview')
}

export function getOrderTrendStat() {
  return request.get('/admin/statistics/orderTrend')
}

export function getTopProducts() {
  return request.get('/admin/statistics/topProducts')
}

// ─── 文件上传 ───
export function uploadFile(file: File, type: string = 'common') {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('type', type)
  return request.post('/file/upload', formData)
}
