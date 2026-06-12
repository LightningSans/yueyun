<template>
  <div>
    <h2 class="page-title">配送员管理 <span class="sub">管理配送员账号</span></h2>

    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="query" size="small">
        <el-form-item label="关键词"><el-input v-model="query.keyword" placeholder="用户名 / 昵称" clearable /></el-form-item>
        <el-form-item label="状态"><el-select v-model="query.status" placeholder="全部状态" clearable style="width:110px"><el-option label="在岗" :value="1" /><el-option label="禁用" :value="0" /></el-select></el-form-item>
        <el-form-item><el-button type="primary" @click="handleSearch">搜索</el-button><el-button @click="handleReset">重置</el-button><el-button type="primary" plain @click="handleAdd">＋ 新增配送员</el-button></el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="nickName" label="昵称" width="120" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }"><el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '在岗' : '禁用' }}</el-tag></template>
        </el-table-column>
        <el-table-column label="创建人" width="120"><template #default="{ row }">系统管理员</template></el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="260">
          <template #default="{ row }">
            <el-button text type="primary" size="small" style="padding:0 5px" @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 1" text type="danger" size="small" style="padding:0 5px" @click="handleToggle(row)">禁用</el-button>
            <el-button v-else text type="success" size="small" style="padding:0 5px" @click="handleToggle(row)">启用</el-button>
            <el-button text type="warning" size="small" style="padding:0 5px" @click="handleResetPwd(row)">重置密码</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑配送员' : '新增配送员'" width="450px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="username"><el-input v-model="form.username" :disabled="!!form.id" /></el-form-item>
        <el-form-item label="密码" :prop="form.id ? '' : 'password'"><el-input v-model="form.password" type="password" :placeholder="form.id ? '不填则不修改' : '请输入密码'" show-password /></el-form-item>
        <el-form-item label="昵称"><el-input v-model="form.nickName" /></el-form-item>
        <el-form-item label="手机号" prop="phone"><el-input v-model="form.phone" /></el-form-item>
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
import { getCourierList, createCourier, updateCourier, disableCourier, enableCourier, resetCourierPwd } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'

const loading = ref(false); const submitting = ref(false)
const tableData = ref<any[]>([]); const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const query = reactive({ page: 1, size: 50, keyword: '', status: null as number | null })
const form = reactive({ id: null as number | null, username: '', password: '', nickName: '', phone: '' })
const rules: FormRules = { username: [{ required: true, message: '请输入用户名', trigger: 'blur' }], phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }] }

onMounted(() => fetchData())
async function fetchData() { loading.value = true; try { const res = await getCourierList(query); tableData.value = res.data.records } finally { loading.value = false } }
function handleSearch() { fetchData() }
function handleReset() { query.keyword = ''; query.status = null; fetchData() }
function handleAdd() { form.id = null; form.username = ''; form.password = ''; form.nickName = ''; form.phone = ''; dialogVisible.value = true }
function handleEdit(row: any) { form.id = row.id; form.username = row.username; form.password = ''; form.nickName = row.nickName; form.phone = row.phone; dialogVisible.value = true }

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return; submitting.value = true
  try {
    if (form.id) { await updateCourier(form); ElMessage.success('修改成功') }
    else { await createCourier(form); ElMessage.success('新增成功') }
    dialogVisible.value = false; fetchData()
  } finally { submitting.value = false }
}

async function handleToggle(row: any) {
  const action = row.status === 1 ? '禁用' : '启用'
  try { await ElMessageBox.confirm(`确定${action}配送员【${row.nickName}】？`, '提示'); row.status === 1 ? await disableCourier(row.id) : await enableCourier(row.id); ElMessage.success(`${action}成功`); fetchData() } catch { /* cancelled */ }
}
async function handleResetPwd(row: any) {
  try { await ElMessageBox.confirm(`确定重置【${row.nickName}】的密码为 123456？`, '提示'); await resetCourierPwd(row.id); ElMessage.success('密码已重置为 123456') } catch { /* cancelled */ }
}
</script>
<style scoped>
.page-title { font-size: 20px; font-weight: 700; margin-bottom: 20px; color: #1a1a2e; }
.page-title .sub { font-size: 13px; font-weight: 400; color: #9e9eaf; }
.search-card { margin-bottom: 16px; }
</style>
