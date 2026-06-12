<template>
  <div>
    <h2 class="page-title">商品管理 <span class="sub">商品列表 / 上下架 / 规格管理</span></h2>

    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="query" size="small">
        <el-form-item label="商品名称"><el-input v-model="query.keyword" placeholder="商品名称" clearable /></el-form-item>
        <el-form-item label="分类"><el-select v-model="query.categoryId" placeholder="全部分类" clearable style="width:130px"><el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" /></el-select></el-form-item>
        <el-form-item label="状态"><el-select v-model="query.status" placeholder="全部状态" clearable style="width:110px"><el-option label="在售" :value="1" /><el-option label="下架" :value="0" /></el-select></el-form-item>
        <el-form-item><el-button type="primary" @click="handleSearch">搜索</el-button><el-button @click="handleReset">重置</el-button></el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <div style="margin-bottom:12px">
        <el-button type="primary" plain @click="handleAdd">＋ 新增商品</el-button>
        <el-button plain @click="batchUp">批量上架</el-button>
        <el-button plain @click="batchDown">批量下架</el-button>
      </div>
      <el-table :data="tableData" stripe v-loading="loading" @selection-change="selected = $event">
        <el-table-column type="selection" width="40" />
        <el-table-column label="商品" min-width="220">
          <template #default="{ row }">
            <div style="display:flex;align-items:center;gap:10px">
              <el-image
                v-if="row.images"
                :src="getFirstImage(row.images)"
                style="width:40px;height:40px;border-radius:6px;flex-shrink:0;"
                fit="cover"
              >
                <template #error><div style="width:40px;height:40px;border-radius:6px;background:#f0f2f5;display:flex;align-items:center;justify-content:center;font-size:18px;">📦</div></template>
              </el-image>
              <div v-else style="width:40px;height:40px;border-radius:6px;background:#f0f2f5;display:flex;align-items:center;justify-content:center;font-size:18px;flex-shrink:0;">📦</div>
              <div><div style="font-weight:500;font-size:13px;">{{ row.name }}</div><div style="font-size:11px;color:#9e9eaf;">{{ row.brand || '-' }}</div></div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="categoryId" label="分类" width="100"><template #default="{ row }">{{ getCategoryName(row.categoryId) }}</template></el-table-column>
        <el-table-column label="价格" width="160">
          <template #default="{ row }">¥{{ row.price }} <span v-if="row.originalPrice" style="text-decoration:line-through;color:#9e9eaf;font-size:11px;">¥{{ row.originalPrice }}</span></template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="70" />
        <el-table-column prop="sales" label="销量" width="70" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }"><el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '在售' : '下架' }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="230">
          <template #default="{ row }">
            <el-button text type="primary" size="small" style="padding:0 5px" @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 1" text type="warning" size="small" style="padding:0 5px" @click="handleToggle(row)">下架</el-button>
            <el-button v-else text type="success" size="small" style="padding:0 5px" @click="handleToggle(row)">上架</el-button>
            <el-button text type="danger" size="small" style="padding:0 5px" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="query.page" v-model:page-size="query.size" :total="total" layout="total, sizes, prev, pager, next" background @change="fetchData" style="margin-top:16px;justify-content:flex-end" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑商品' : '新增商品'" width="750px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px" label-position="top">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="商品名称" prop="name"><el-input v-model="form.name" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="品牌"><el-input v-model="form.brand" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8"><el-form-item label="分类" prop="categoryId"><el-tree-select v-model="form.categoryId" :data="categories" :props="{label:'name',value:'id'}" placeholder="选择分类" style="width:100%" check-strictly /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="价格" prop="price"><el-input-number v-model="form.price" :min="0" :precision="2" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="原价"><el-input-number v-model="form.originalPrice" :min="0" :precision="2" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8"><el-form-item label="库存"><el-input-number v-model="form.stock" :min="0" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="关键词"><el-input v-model="form.keywords" placeholder="逗号分隔" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="状态"><el-switch v-model="form.status" :active-value="1" :inactive-value="0" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="商品描述"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="规格（JSON）"><el-input v-model="form.specs" type="textarea" :rows="2" placeholder='[{"name":"颜色","values":["黑","白"]}]' /></el-form-item>

        <!-- 商品图片上传 -->
        <el-form-item label="商品图片">
          <div class="upload-area">
            <div class="image-list">
              <div v-for="(url, idx) in imageList" :key="idx" class="image-item">
                <el-image :src="url" fit="cover" style="width:80px;height:80px;border-radius:6px;" />
                <el-button class="image-del" size="small" circle @click="removeImage(idx)">✕</el-button>
              </div>
              <el-upload
                :show-file-list="false"
                :http-request="handleUploadRequest"
                accept="image/*"
              >
                <div class="upload-trigger">
                  <span style="font-size:24px;color:#9e9eaf;">＋</span>
                  <span style="font-size:12px;color:#9e9eaf;margin-top:4px;">上传图片</span>
                </div>
              </el-upload>
            </div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { getProductList, createProduct, updateProduct, upProduct, downProduct, batchUpProduct, batchDownProduct, deleteProduct, getCategoryList, uploadFile } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'

const loading = ref(false); const submitting = ref(false)
const tableData = ref<any[]>([]); const categories = ref<any[]>([]); const total = ref(0)
const dialogVisible = ref(false); const selected = ref<any[]>([])
const formRef = ref<FormInstance>()
const query = reactive({ page: 1, size: 10, keyword: '', categoryId: null as number | null, status: null as number | null })
const form = reactive({ id: null as number | null, name: '', brand: '', categoryId: null, price: 0, originalPrice: null, stock: 0, keywords: '', description: '', specs: '', status: 1 })
const rules: FormRules = { name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }], categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }] }

/** 图片列表 */
const imageList = ref<string[]>([])

onMounted(async () => {
  fetchData()
  try { const res = await getCategoryList(); categories.value = flatTree(res.data) } catch { /* ignore */ }
})

function flatTree(list: any[]): any[] {
  const result: any[] = []
  function walk(items: any[]) { for (const item of items) { result.push({ id: item.id, name: item.name }); if (item.children) walk(item.children) } }
  walk(list); return result
}

/** 获取第一张图片 */
function getFirstImage(images: any): string {
  if (!images) return ''
  try {
    const arr = typeof images === 'string' ? JSON.parse(images) : images
    return Array.isArray(arr) && arr.length > 0 ? arr[0] : ''
  } catch { return '' }
}

async function fetchData() { loading.value = true; try { const res = await getProductList(query); tableData.value = res.data.records; total.value = res.data.total } finally { loading.value = false } }
function handleSearch() { query.page = 1; fetchData() }
function handleReset() { query.keyword = ''; query.categoryId = null; query.status = null; query.page = 1; fetchData() }
function handleAdd() {
  form.id = null; form.name = ''; form.brand = ''; form.categoryId = null; form.price = 0
  form.originalPrice = null; form.stock = 0; form.keywords = ''; form.description = ''; form.specs = ''; form.status = 1
  imageList.value = []
  dialogVisible.value = true
}

function handleEdit(row: any) {
  Object.assign(form, row)
  // 解析 images JSON 字符串为数组
  if (row.images) {
    try { imageList.value = typeof row.images === 'string' ? JSON.parse(row.images) : row.images }
    catch { imageList.value = [] }
  } else {
    imageList.value = []
  }
  dialogVisible.value = true
}

/** 上传图片（使用 http-request 覆盖 el-upload 默认上传） */
async function handleUploadRequest(options: any) {
  const file = options.file as File
  if (!file) return

  // 校验文件大小（最大 5MB）
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.warning('图片大小不能超过 5MB')
    options.onError?.('文件过大')
    return
  }

  try {
    const res = await uploadFile(file, 'product')
    imageList.value.push(res.data.url)
    ElMessage.success('图片上传成功')
    options.onSuccess?.(res)
  } catch (e: any) {
    ElMessage.error('图片上传失败: ' + (e.message || '未知错误'))
    options.onError?.(e)
  }
}

/** 删除图片 */
function removeImage(idx: number) {
  imageList.value.splice(idx, 1)
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  // 校验 specs
  const specsRaw = form.specs || ''
  if (specsRaw.trim()) {
    try { JSON.parse(specsRaw) } catch {
      ElMessage.error('规格（JSON）格式不正确，请输入合法的 JSON')
      return
    }
  }

  submitting.value = true
  try {
    const data = {
      ...form,
      specs: specsRaw.trim() || null,
      // 图片数组转 JSON 字符串，空数组则为 null
      images: imageList.value.length > 0 ? JSON.stringify(imageList.value) : null
    }
    if (form.id) { await updateProduct(data); ElMessage.success('修改成功') }
    else { await createProduct(data); ElMessage.success('新增成功') }
    dialogVisible.value = false; fetchData()
  } finally { submitting.value = false }
}

async function handleToggle(row: any) {
  const action = row.status === 1 ? '下架' : '上架'
  try { await ElMessageBox.confirm(`确定${action}商品【${row.name}】？`, '提示'); row.status === 1 ? await downProduct(row.id) : await upProduct(row.id); ElMessage.success(`${action}成功`); fetchData() } catch { /* cancelled */ }
}
async function handleDelete(row: any) {
  try { await ElMessageBox.confirm(`确定删除商品【${row.name}】？`, '提示'); await deleteProduct(row.id); ElMessage.success('删除成功'); fetchData() } catch { /* cancelled */ }
}
async function batchUp() {
  const ids = selected.value.map((s: any) => s.id); if (!ids.length) return ElMessage.warning('请选择商品')
  await batchUpProduct(ids); ElMessage.success('批量上架成功'); fetchData()
}
async function batchDown() {
  const ids = selected.value.map((s: any) => s.id); if (!ids.length) return ElMessage.warning('请选择商品')
  await batchDownProduct(ids); ElMessage.success('批量下架成功'); fetchData()
}
function getCategoryName(id: number) { return categories.value.find((c: any) => c.id === id)?.name || '-' }
</script>
<style scoped>
.page-title { font-size: 20px; font-weight: 700; margin-bottom: 20px; color: #1a1a2e; }
.page-title .sub { font-size: 13px; font-weight: 400; color: #9e9eaf; }
.search-card { margin-bottom: 16px; }
.upload-area { width: 100%; }
.image-list { display: flex; flex-wrap: wrap; gap: 10px; align-items: flex-start; }
.image-item { position: relative; width: 80px; height: 80px; border-radius: 6px; overflow: hidden; border: 1px solid #e8e8f0; flex-shrink: 0; }
.image-del { position: absolute; top: -6px; right: -6px; width: 20px; height: 20px; padding: 0; background: #ef4444; color: #fff; border: none; font-size: 12px; border-radius: 50%; cursor: pointer; display: flex; align-items: center; justify-content: center; }
.upload-trigger { width: 80px; height: 80px; border: 1px dashed #d9d9d9; border-radius: 6px; display: flex; flex-direction: column; align-items: center; justify-content: center; cursor: pointer; background: #fafafa; transition: border-color .2s; }
.upload-trigger:hover { border-color: #1677ff; color: #1677ff; }
</style>
