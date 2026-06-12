<template>
  <div>
    <h2 class="page-title">分类管理 <span class="sub">商品分类多级维护</span></h2>

    <el-card shadow="never" class="search-card">
      <el-button type="primary" @click="handleAddRoot">＋ 新增顶级分类</el-button>
    </el-card>

    <el-card shadow="never">
      <el-table :data="tableData" row-key="id" stripe default-expand-all :tree-props="{ children: 'children' }" v-loading="loading">
        <el-table-column prop="name" label="分类名称" min-width="200" />
        <el-table-column prop="parentId" label="父级分类" width="150">
          <template #default="{ row }">{{ row.parentId === 0 ? '- 顶级 -' : getParentName(row.parentId) }}</template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="70" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button text type="primary" size="small" @click="handleAddChild(row)">+ 新增子级</el-button>
            <el-button text type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑分类' : '新增分类'" width="450px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="父级分类" v-if="form.id || form.parentId !== null">
          <el-tree-select v-model="form.parentId" :data="treeData" :props="{ label: 'name', value: 'id' }" placeholder="顶级分类" clearable check-strictly style="width:100%" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
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
import { ref, reactive, onMounted } from 'vue'
import { getCategoryList, createCategory, updateCategory, deleteCategory } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'

const loading = ref(false); const submitting = ref(false)
const tableData = ref<any[]>([])
const treeData = ref<any[]>([])
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()

const form = reactive({ id: null as number | null, name: '', parentId: null as number | null, sortOrder: 30, status: 1 })
const rules: FormRules = { name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }] }

onMounted(() => fetchData())

async function fetchData() {
  loading.value = true
  try {
    console.log('[分类管理] 开始请求数据...')
    const res = await getCategoryList()
    console.log('[分类管理] 请求成功，原始响应:', res)
    const list = res.data || []
    console.log('[分类管理] 提取的列表数据:', list)
    if (!list.length) {
      console.warn('[分类管理] 返回的数据为空数组')
    }
    tableData.value = list
    treeData.value = flattenTree(list)
    console.log('[分类管理] 填充完成，tableData:', tableData.value)
  } catch (e: any) {
    console.error('[分类管理] 请求失败:', e)
    console.error('[分类管理] 错误详情:', e.message, e.response?.status, e.response?.data)
    ElMessage.error('获取分类数据失败: ' + (e.message || '未知错误'))
  } finally { loading.value = false }
}

/** 将树形数据拍平为 el-tree-select 可用格式 */
function flattenTree(list: any[]): any[] {
  const result: any[] = []
  function walk(items: any[]) {
    for (const item of items) {
      result.push({ id: item.id, name: item.name, parentId: item.parentId })
      if (item.children?.length) walk(item.children)
    }
  }
  walk(list)
  return result
}

function getParentName(id: number) {
  for (const item of treeData.value || tableData.value) {
    if (item.id === id) return item.name
  }
  return '-'
}

function handleAddRoot() {
  form.id = null; form.name = ''; form.parentId = null; form.sortOrder = 30; form.status = 1
  dialogVisible.value = true
}
function handleAddChild(row: any) {
  form.id = null; form.name = ''; form.parentId = row.id; form.sortOrder = 30; form.status = 1
  dialogVisible.value = true
}
function handleEdit(row: any) {
  Object.assign(form, row)
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return; submitting.value = true
  try {
    if (form.id) {
      await updateCategory({ id: form.id, name: form.name, sortOrder: form.sortOrder, status: form.status, parentId: form.parentId || 0 })
      ElMessage.success('修改成功')
    } else {
      await createCategory({ name: form.name, parentId: form.parentId || 0, sortOrder: form.sortOrder })
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false; fetchData()
  } finally { submitting.value = false }
}

async function handleDelete(row: any) {
  try {
    await ElMessageBox.confirm(`确定删除分类【${row.name}】？`, '提示')
    await deleteCategory(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch { /* cancelled */ }
}
</script>
<style scoped>
.page-title { font-size: 20px; font-weight: 700; margin-bottom: 20px; color: #1a1a2e; }
.page-title .sub { font-size: 13px; font-weight: 400; color: #9e9eaf; }
.search-card { margin-bottom: 16px; }
</style>
