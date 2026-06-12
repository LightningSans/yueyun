<template>
  <div class="profile-layout">
    <div class="profile-sidebar">
      <div class="profile-card">
        <div class="avatar-lg">{{ avatarText }}</div>
        <div class="name">{{ authStore.userInfo?.nickname || authStore.userInfo?.username || '用户' }}</div>
        <div class="phone">{{ authStore.userInfo?.phone || '' }}</div>
        <span class="edit-btn" @click="showEdit = true">编辑资料</span>
      </div>
      <div class="profile-menu">
        <div class="menu-item" @click="$router.push('/orders')"><span class="menu-icon">📋</span><span class="menu-label">我的订单</span><span class="menu-arrow">›</span></div>
        <div class="menu-item" @click="showPassword = true"><span class="menu-icon">🔒</span><span class="menu-label">修改密码</span><span class="menu-arrow">›</span></div>
        <div class="menu-item" @click="handleLogout"><span class="menu-icon">🚪</span><span class="menu-label">退出登录</span><span class="menu-arrow">›</span></div>
      </div>
    </div>
    <div class="profile-main">
      <div class="profile-stats">
        <div class="stat-item" @click="$router.push('/orders')"><div class="stat-num">我的订单</div><div class="stat-label">查看全部</div></div>
        <div class="stat-item" @click="$router.push('/orders')"><div class="stat-num">待付款</div><div class="stat-label">去付款</div></div>
        <div class="stat-item" @click="$router.push('/orders')"><div class="stat-num">待发货</div><div class="stat-label">等发货</div></div>
        <div class="stat-item" @click="$router.push('/orders')"><div class="stat-num">已完成</div><div class="stat-label">去评价</div></div>
      </div>
    </div>

    <!-- 编辑资料弹窗 -->
    <div v-if="showEdit" class="modal-overlay" @click.self="showEdit = false">
      <div class="modal-box"><div class="modal-header"><h3>编辑资料</h3><button class="modal-close" @click="showEdit = false">✕</button></div>
        <div class="modal-body">
          <div class="field"><label>昵称</label><input v-model="editForm.nickname" /></div>
          <div class="field"><label>手机号</label><input v-model="editForm.phone" /></div>
          <div class="field"><label>邮箱</label><input v-model="editForm.email" /></div>
          <div class="field"><label>性别</label>
            <select v-model="editForm.gender"><option :value="0">未知</option><option :value="1">男</option><option :value="2">女</option></select>
          </div>
        </div>
        <div class="modal-footer"><button class="btn btn-cancel" @click="showEdit = false">取消</button><button class="btn btn-primary" :disabled="saving" @click="saveProfile">{{ saving ? '保存中...' : '保存修改' }}</button></div>
      </div>
    </div>

    <!-- 修改密码弹窗 -->
    <div v-if="showPassword" class="modal-overlay" @click.self="showPassword = false">
      <div class="modal-box"><div class="modal-header"><h3>修改密码</h3><button class="modal-close" @click="showPassword = false">✕</button></div>
        <div class="modal-body">
          <div class="field"><label>原密码</label><input v-model="pwdForm.oldPassword" type="password" /></div>
          <div class="field"><label>新密码</label><input v-model="pwdForm.newPassword" type="password" /></div>
        </div>
        <div class="modal-footer"><button class="btn btn-cancel" @click="showPassword = false">取消</button><button class="btn btn-primary" :disabled="savingPwd" @click="savePassword">{{ savingPwd ? '修改中...' : '确认修改' }}</button></div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { updateUserInfo, changePassword } from '@/api'
import { ElMessage } from '@/utils/toast'

const router = useRouter()
const authStore = useAuthStore()
const showEdit = ref(false)
const showPassword = ref(false)
const saving = ref(false)
const savingPwd = ref(false)

const editForm = ref({ nickname: '', phone: '', email: '', gender: 0 })
const pwdForm = ref({ oldPassword: '', newPassword: '' })

const avatarText = computed(() => {
  const name = authStore.userInfo?.nickname || authStore.userInfo?.username || 'U'
  return name.charAt(0)
})

onMounted(async () => {
  if (authStore.isLoggedIn && !authStore.userInfo) await authStore.fetchUserInfo()
  editForm.value = {
    nickname: authStore.userInfo?.nickname || '',
    phone: authStore.userInfo?.phone || '',
    email: authStore.userInfo?.email || '',
    gender: authStore.userInfo?.gender ?? 0
  }
})

async function saveProfile() {
  saving.value = true
  try {
    await updateUserInfo(editForm.value)
    if (authStore.userInfo) {
      authStore.userInfo.nickname = editForm.value.nickname
      authStore.userInfo.phone = editForm.value.phone
      authStore.userInfo.email = editForm.value.email
      authStore.userInfo.gender = editForm.value.gender
    }
    ElMessage('修改成功')
    showEdit.value = false
  } catch (e: any) { ElMessage(e.message || '修改失败') }
  finally { saving.value = false }
}

async function savePassword() {
  if (!pwdForm.value.oldPassword || !pwdForm.value.newPassword) { ElMessage('请填写完整'); return }
  savingPwd.value = true
  try { await changePassword(pwdForm.value); ElMessage('密码修改成功'); showPassword.value = false; pwdForm.value = { oldPassword: '', newPassword: '' } }
  catch (e: any) { ElMessage(e.message || '修改失败') }
  finally { savingPwd.value = false }
}

function handleLogout() {
  authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.profile-layout { display: grid; grid-template-columns: 280px 1fr; gap: 24px; }
.profile-card { background: var(--bg-white); border-radius: var(--radius); border: 1px solid var(--border-light); padding: 24px; text-align: center; margin-bottom: 12px; }
.avatar-lg { width: 72px; height: 72px; border-radius: 50%; background: var(--primary-gradient); display: flex; align-items: center; justify-content: center; font-size: 32px; margin: 0 auto 12px; color: #fff; }
.profile-card .name { font-size: 18px; font-weight: 700; }
.profile-card .phone { font-size: 13px; color: var(--text-light); margin-top: 4px; }
.edit-btn { font-size: 13px; color: var(--primary); cursor: pointer; margin-top: 8px; display: inline-block; }
.profile-menu { display: flex; flex-direction: column; gap: 6px; }
.menu-item { display: flex; align-items: center; gap: 12px; padding: 14px 16px; background: var(--bg-white); border-radius: var(--radius-sm); border: 1px solid var(--border-light); cursor: pointer; transition: all .15s; }
.menu-item:hover { border-color: var(--primary); }
.menu-icon { font-size: 18px; width: 24px; text-align: center; }
.menu-label { flex: 1; font-size: 14px; }
.menu-arrow { color: var(--text-light); font-size: 12px; }
.profile-stats { display: grid; grid-template-columns: repeat(4, 1fr); background: var(--bg-white); border-radius: var(--radius); border: 1px solid var(--border-light); overflow: hidden; }
.stat-item { text-align: center; padding: 24px 8px; border-right: 1px solid var(--border-light); cursor: pointer; transition: all .15s; }
.stat-item:last-child { border-right: none; }
.stat-item:hover { background: var(--bg); }
.stat-num { font-size: 16px; font-weight: 700; color: var(--primary); }
.stat-label { font-size: 12px; color: var(--text-light); margin-top: 4px; }
.field { margin-bottom: 14px; }
.field label { display: block; font-size: 13px; font-weight: 500; margin-bottom: 4px; color: var(--text-secondary); }
.field input, .field select { width: 100%; height: 42px; padding: 0 14px; border: 1.5px solid var(--border); border-radius: var(--radius-xs); font-size: 14px; outline: none; font-family: inherit; box-sizing:border-box; }
.field input:focus, .field select:focus { border-color: var(--primary); box-shadow: 0 0 0 3px rgba(232,93,58,.1); }
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,.4); z-index: 200; display: flex; align-items: center; justify-content: center; backdrop-filter: blur(4px); }
.modal-box { background: var(--bg-white); border-radius: var(--radius); width: 420px; max-height: 80vh; overflow-y: auto; box-shadow: 0 20px 60px rgba(0,0,0,.15); }
.modal-header { padding: 18px 20px; border-bottom: 1px solid var(--border); display: flex; align-items: center; justify-content: space-between; }
.modal-header h3 { font-size: 16px; font-weight: 600; }
.modal-close { width: 30px; height: 30px; border: none; background: none; font-size: 18px; cursor: pointer; color: var(--text-light); border-radius: 4px; }
.modal-close:hover { background: var(--bg); }
.modal-body { padding: 20px; }
.modal-footer { padding: 12px 20px; border-top: 1px solid var(--border); display: flex; justify-content: flex-end; gap: 8px; }
.btn { padding: 8px 20px; border-radius: 6px; font-size: 13px; font-weight: 500; cursor: pointer; font-family: inherit; }
.btn-cancel { border: 1px solid var(--border); background: var(--bg-white); }
.btn-primary { background: var(--primary); color: #fff; border: none; }
.btn-primary:hover { background: var(--primary-dark); }
.btn-primary:disabled { opacity: .5; cursor: not-allowed; }
@media (max-width: 960px) { .profile-layout { grid-template-columns: 1fr; } }
</style>
