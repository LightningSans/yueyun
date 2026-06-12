<template>
  <div>
    <h2 class="page-title">评价管理 <span class="sub">审核 / 管理用户评价</span></h2>

    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="query" size="small">
        <el-form-item label="评分"><el-select v-model="query.rating" placeholder="全部评分" clearable style="width:100px"><el-option v-for="i in 5" :key="i" :label="i + '星'" :value="i" /></el-select></el-form-item>
        <el-form-item label="状态"><el-select v-model="query.status" placeholder="全部状态" clearable style="width:110px"><el-option label="显示" :value="1" /><el-option label="隐藏" :value="0" /></el-select></el-form-item>
        <el-form-item><el-button type="primary" @click="handleSearch">搜索</el-button><el-button @click="handleReset">重置</el-button></el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <div style="margin-bottom:12px">
        <el-button type="warning" plain @click="batchHide">🙈 批量隐藏</el-button>
        <el-button type="success" plain @click="batchShow">👁 批量显示</el-button>
      </div>
      <el-table :data="tableData" stripe v-loading="loading" @selection-change="selected = $event">
        <el-table-column type="selection" width="40" />
        <el-table-column label="商品" min-width="160">
          <template #default="{ row }">
            <div style="display:flex;align-items:center;gap:8px">
              <el-image
                v-if="row.productImage"
                :src="row.productImage" fit="cover"
                style="width:30px;height:30px;border-radius:4px;flex-shrink:0;"
              >
                <template #error><div style="width:30px;height:30px;border-radius:4px;background:#f0f2f5;flex-shrink:0;"></div></template>
              </el-image>
              <div v-else style="width:30px;height:30px;border-radius:4px;background:#f0f2f5;flex-shrink:0;"></div>
              <el-text line-clamp="1" style="font-size:13px;">{{ row.productName || '商品 #' + row.productId }}</el-text>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="用户" width="80"><template #default="{ row }">{{ row.userNickname || '用户 #' + row.userId }}</template></el-table-column>
        <el-table-column label="评分" width="90">
          <template #default="{ row }"><span v-for="i in 5" :key="i" :style="{ color: i <= (row.rating || 0) ? '#F59E0B' : '#e8e8f0' }">★</span></template>
        </el-table-column>
        <el-table-column label="评价内容" min-width="200">
          <template #default="{ row }">
            <el-text line-clamp="1">{{ row.content || '-' }}</el-text>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="70">
          <template #default="{ row }"><el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '显示' : '隐藏' }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="createTime" label="时间" min-width="140" />
        <el-table-column label="操作" width="110">
          <template #default="{ row }">
            <el-button v-if="row.status === 1" text type="warning" size="small" @click="handleHide(row)">隐藏</el-button>
            <el-button v-else text type="success" size="small" @click="handleShow(row)">显示</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="query.page" v-model:page-size="query.size" :total="total" layout="total, sizes, prev, pager, next" background @change="fetchData" style="margin-top:16px;justify-content:flex-end" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { getReviewList, hideReview, showReview, batchHideReview, batchShowReview } from '@/api/admin'
import { ElMessage } from 'element-plus'

const loading = ref(false); const tableData = ref<any[]>([]); const total = ref(0); const selected = ref<any[]>([])
const query = reactive({ page: 1, size: 10, rating: null as number | null, status: null as number | null })

onMounted(() => fetchData())
async function fetchData() { loading.value = true; try { const res = await getReviewList(query); tableData.value = res.data.records; total.value = res.data.total } finally { loading.value = false } }
function handleSearch() { query.page = 1; fetchData() }
function handleReset() { query.rating = null; query.status = null; query.page = 1; fetchData() }
async function handleHide(row: any) { await hideReview(row.id); ElMessage.success('评价已隐藏'); fetchData() }
async function handleShow(row: any) { await showReview(row.id); ElMessage.success('评价已显示'); fetchData() }
async function batchHide() { const ids = selected.value.map((s: any) => s.id); if (!ids.length) return ElMessage.warning('请选择评价'); await batchHideReview(ids); ElMessage.success('批量隐藏成功'); fetchData() }
async function batchShow() { const ids = selected.value.map((s: any) => s.id); if (!ids.length) return ElMessage.warning('请选择评价'); await batchShowReview(ids); ElMessage.success('批量显示成功'); fetchData() }
</script>
<style scoped>
.page-title { font-size: 20px; font-weight: 700; margin-bottom: 20px; color: #1a1a2e; }
.page-title .sub { font-size: 13px; font-weight: 400; color: #9e9eaf; }
.search-card { margin-bottom: 16px; }
</style>
