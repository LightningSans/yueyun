<template>
  <div>
    <h2 class="page-title">管理员管理 <span class="sub">超级管理员专属，管理后台账号</span></h2>

    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="query" size="small">
        <el-form-item label="关键词"><el-input v-model="query.keyword" placeholder="用户名 / 真实姓名" clearable /></el-form-item>
        <el-form-item label="角色">
          <el-select v-model="query.role" placeholder="全部角色" clearable style="width:130px">
            <el-option label="超级管理员" value="SUPER_ADMIN" />
            <el-option label="普通管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部状态" clearable style="width:110px">
            <el-option label="在职" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="primary" plain @click="handleAdd">＋ 新增管理员</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="真实姓名" width="120" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="role" label="角色" width="130">
          <template #default="{ row }">
            <el-tag :type="row.role === 'SUPER_ADMIN' ? 'danger' : 'default'" size="small">
              {{ row.role === 'SUPER_ADMIN' ? '超级管理员' : '普通管理员' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '在职' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="最后更新" width="170" />
        <el-table-column label="操作" width="140">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button
              v-if="row.role !== 'SUPER_ADMIN'"
              text :type="row.status === 1 ? 'danger' : 'success'" size="small"
              @click="handleToggle(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑管理员' : '新增管理员'" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="密码" :prop="isEdit ? '' : 'password'">
          <el-input v-model="form.password" type="password" :placeholder="isEdit ? '不填则不修改' : '请输入密码'" show-password />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" style="width:100%">
            <el-option label="普通管理员" value="ADMIN" />
            <el-option label="超级管理员" value="SUPER_ADMIN" />
          </el-select>
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
import { getAdminList, createAdmin, updateAdmin, toggleAdminStatus } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'

const loading = ref(false); const submitting = ref(false)
const tableData = ref<any[]>([]); const dialogVisible = ref(false); const isEdit = ref(false)
const formRef = ref<FormInstance>()
const query = reactive({ page: 1, size: 50, keyword: '', role: null as string | null, status: null as number | null })
const form = reactive({ id: null as number | null, username: '', password: '', realName: '', phone: '', email: '', role: 'ADMIN' })
const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

onMounted(() => fetchData())
async function fetchData() { loading.value = true; try { const res = await getAdminList(query); tableData.value = res.data.records } finally { loading.value = false } }
function handleSearch() { fetchData() }
function handleReset() { query.keyword = ''; query.role = null; query.status = null; fetchData() }
function handleAdd() { isEdit.value = false; form.id = null; form.username = ''; form.password = ''; form.realName = ''; form.phone = ''; form.email = ''; form.role = 'ADMIN'; dialogVisible.value = true }
function handleEdit(row: any) { isEdit.value = true; Object.assign(form, row); form.password = ''; dialogVisible.value = true }

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return; submitting.value = true
  try {
    if (isEdit.value) { await updateAdmin(form); ElMessage.success('修改成功') }
    else { await createAdmin(form); ElMessage.success('新增成功') }
    dialogVisible.value = false; fetchData()
  } finally { submitting.value = false }
}

async function handleToggle(row: any) {
  const action = row.status === 1 ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确定${action}管理员【${row.realName}】？`, '提示')
    await toggleAdminStatus(row.id)
    ElMessage.success(`${action}成功`)
    fetchData()
  } catch { /* cancelled */ }
}
</script>
<style scoped>
.page-title { font-size: 20px; font-weight: 700; margin-bottom: 20px; color: #1a1a2e; }
.page-title .sub { font-size: 13px; font-weight: 400; color: #9e9eaf; }
.search-card { margin-bottom: 16px; }
</style>
