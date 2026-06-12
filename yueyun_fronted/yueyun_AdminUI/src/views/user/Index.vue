<template>
  <div>
    <h2 class="page-title">用户管理 <span class="sub">管理前台注册用户</span></h2>

    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="query" size="small">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="用户名 / 手机号" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部状态" clearable style="width:120px">
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column type="selection" width="40" />
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="nickname" label="昵称" width="120" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="170" />
        <el-table-column prop="lastLoginTime" label="最后登录" width="170" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button text type="primary" size="small" style="padding:0 6px" @click.stop="handleDetail(row)">详情</el-button>
            <el-button
              text :type="row.status === 1 ? 'danger' : 'success'" size="small" style="padding:0 6px"
              :loading="freezingId === row.id"
              @click.stop="handleToggleStatus(row)">
              {{ row.status === 1 ? '冻结' : '解冻' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        background
        @change="fetchData"
        style="margin-top:16px;justify-content:flex-end" />
    </el-card>

    <!-- 详情/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="'用户详情 - ' + editForm.username" width="520px" @close="handleDialogClose">
      <el-form ref="formRef" :model="editForm" :rules="rules" label-width="80px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用户名">
              <el-input v-model="editForm.username" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="昵称" prop="nickname">
              <el-input v-model="editForm.nickname" placeholder="请输入昵称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="editForm.phone" placeholder="请输入手机号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="editForm.email" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="性别">
              <el-select v-model="editForm.gender" style="width:100%">
                <el-option :label="'未知'" :value="0" />
                <el-option :label="'男'" :value="1" />
                <el-option :label="'女'" :value="2" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-switch v-model="editForm.status" :active-value="1" :inactive-value="0" active-text="正常" inactive-text="禁用" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="注册时间">
          <el-input v-model="editForm.createTime" disabled />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">保存修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { getUserList, getUserDetail, updateUser, freezeUser, unfreezeUser } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)

// 冻结按钮独立 loading（记录正在操作的行 ID）
const freezingId = ref<number | null>(null)

// 编辑对话框
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const editForm = reactive({
  id: null as number | null,
  username: '',
  nickname: '',
  phone: '',
  email: '',
  gender: 0,
  status: 1,
  createTime: ''
})

const rules: FormRules = {
  nickname: [{ max: 50, message: '昵称不超过50个字符', trigger: 'blur' }],
  phone: [{ pattern: /^1\d{10}$/, message: '请输入正确的手机号', trigger: 'blur' }],
  email: [{ type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }]
}

const query = reactive({ page: 1, size: 10, keyword: '', status: null as number | null })

onMounted(() => fetchData())

async function fetchData() {
  loading.value = true
  try {
    const res = await getUserList(query)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally { loading.value = false }
}

function handleSearch() { query.page = 1; fetchData() }
function handleReset() { query.keyword = ''; query.status = null; query.page = 1; fetchData() }

/** 冻结/解冻 — 原地更新行状态，不触发全表 reload */
async function handleToggleStatus(row: any) {
  const action = row.status === 1 ? '冻结' : '解冻'
  try {
    await ElMessageBox.confirm(`确定要${action}用户【${row.nickname || row.username}】吗？`, '提示')
  } catch {
    return
  }

  freezingId.value = row.id
  try {
    row.status === 1 ? await freezeUser(row.id) : await unfreezeUser(row.id)
    // 直接更新行的状态和显示
    row.status = row.status === 1 ? 0 : 1
    ElMessage.success(`${action}成功`)
  } catch {
    // 失败时不改状态
  } finally {
    freezingId.value = null
  }
}

/** 打开详情编辑对话框 */
async function handleDetail(row: any) {
  const res = await getUserDetail(row.id)
  const data = res.data
  editForm.id = data.id
  editForm.username = data.username
  editForm.nickname = data.nickname || ''
  editForm.phone = data.phone || ''
  editForm.email = data.email || ''
  editForm.gender = data.gender ?? 0
  editForm.status = data.status ?? 1
  editForm.createTime = data.createTime || ''
  dialogVisible.value = true
}

function handleDialogClose() {
  formRef.value?.resetFields()
}

/** 保存修改 */
async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await updateUser({
      id: editForm.id,
      nickname: editForm.nickname,
      phone: editForm.phone,
      email: editForm.email,
      gender: editForm.gender,
      status: editForm.status
    })
    ElMessage.success('修改成功')
    dialogVisible.value = false
    // 刷新列表以反映最新数据
    fetchData()
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.page-title { font-size: 20px; font-weight: 700; margin-bottom: 20px; color: #1a1a2e; }
.page-title .sub { font-size: 13px; font-weight: 400; color: #9e9eaf; }
.search-card { margin-bottom: 16px; }
</style>
