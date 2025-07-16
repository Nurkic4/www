<template>
  <div class="user-dropdown" @mouseover="show = true" @mouseleave="show = false">
      <img class="avatar" :class="{ 'avatar-active': show }" :src="user.avatar || defaultAvatar" alt="头像" />
    <transition name="dropdown-fade">
      <div v-if="show" class="dropdown-menu">
        <div class="dropdown-title">个人中心</div>
        <div class="dropdown-divider"></div>
        <div class="user-info">
          <div class="user-row"><span>用户名：</span>{{ user.username }}</div>
          <div class="user-row"><span>手机号：</span>{{ user.phone }}</div>
          <div class="user-row"><span>邮箱：</span>{{ user.email }}</div>
          <div class="user-row"><span>类型：</span>{{ user.userType }}</div>
        </div>
        <!-- 管理员审核入口 -->
        <button v-if="user.userType === 'ADMIN'" class="admin-review-btn" @click="goReview">文章审核</button>
        <button class="logout-btn" @click="logout">退出登录</button>
        <div class="edit-info">
          <button class="edit-btn" @click="openEditDialog">编辑个人信息</button>
        </div>
      </div>
    </transition>
    <!-- 编辑个人信息弹窗 -->
    <teleport to="body">
      <div v-if="editDialogVisible" class="edit-dialog-mask">
        <div class="edit-dialog">
          <div class="edit-dialog-title">编辑个人信息</div>
          <div class="edit-dialog-body">
            <div class="edit-avatar">
              <img :src="editValues.avatar || defaultAvatar" class="edit-avatar-img" alt="头像" />
              <input type="file" accept="image/*" @change="onAvatarChange" />
            </div>
            <div class="edit-row"><span>用户名：</span><input v-model="editValues.username" /></div>
            <div class="edit-row"><span>手机号：</span><input v-model="editValues.phone" /></div>
            <div class="edit-row"><span>邮 箱：</span><input v-model="editValues.email" /></div>
            <div class="edit-row">
              <span>密 码：</span>
              <input type="password" value="********" disabled style="width:120px;opacity:0.7;" />
              <button class="pwd-btn" @click="openPwdDialog">校验密码</button>
            </div>
          </div>
          <div class="edit-dialog-footer">
            <button class="edit-cancel" @click="editDialogVisible = false">取消</button>
            <button class="edit-save" @click="saveEdit">保存</button>
          </div>
        </div>
      </div>
    </teleport>
    <!-- 修改密码弹窗 -->
    <teleport to="body">
      <div v-if="pwdDialogVisible" class="pwd-dialog-mask">
        <div class="pwd-dialog">
          <div class="pwd-dialog-title">修改密码</div>
          <div class="pwd-dialog-body">
            <div class="pwd-row"><span>原密码：</span><input type="password" v-model="pwdForm.oldPwd" /></div>
            <div class="pwd-row"><span>新密码：</span><input type="password" v-model="pwdForm.newPwd" /></div>
            <div v-if="pwdError" class="pwd-error">{{ pwdError }}</div>
          </div>
          <div class="pwd-dialog-footer">
            <button class="pwd-cancel" @click="closePwdDialog">取消</button>
            <button class="pwd-save" @click="savePwd">保存</button>
          </div>
        </div>
      </div>
    </teleport>
  </div>
</template>

<script setup>
import { onMounted, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { request } from '@/utils/request'
import defaultAvatarImg from '../views/css/png/头像.png'
import { useUserStore } from '@/store/user'
// vee-validate 集成
import { useForm, useField } from 'vee-validate'
import * as yup from 'yup'
const defaultAvatar = defaultAvatarImg
const userStore = useUserStore()
const user = computed(() => userStore.userInfo || {})
const show = ref(false)
const router = useRouter()
const editDialogVisible = ref(false)
const pwdDialogVisible = ref(false)
const pwdForm = ref({ oldPwd: '', newPwd: '' })
const pwdError = ref('')

// 个人信息编辑表单校验
const editSchema = yup.object({
  username: yup.string().required('用户名必填'),
  phone: yup.string().required('手机号必填').matches(/^1[3-9]\d{9}$/, '手机号格式不正确'),
  email: yup.string().required('邮箱必填').email('邮箱格式不正确'),
  avatar: yup.string().nullable()
})
const { handleSubmit: handleEditSubmit, errors: editErrors, values: editValues, setValues: setEditValues, resetForm: resetEditForm } = useForm({
  validationSchema: editSchema,
  initialValues: { avatar: '', username: '', phone: '', email: '' }
})
const editUsername = useField('username')
const editPhone = useField('phone')
const editEmail = useField('email')
const editAvatar = useField('avatar')

onMounted(async () => {
  const token = userStore.token
  if (token && !userStore.userInfo) {
    const { data, error } = await request({ url: '/api/user/info', method: 'GET' })
    if (!error && data) userStore.setUserInfo(data)
  }
})

function logout() {
  userStore.logout()
  router.push('/login')
}

async function onAvatarChange(e) {
  const file = e.target.files[0]
  if (!file) return
  const reader = new FileReader()
  reader.onload = async (evt) => {
    const base64 = evt.target.result
    editValues.avatar = base64
  }
  reader.readAsDataURL(file)
}

function openEditDialog() {
  setEditValues({
    avatar: user.value.avatar || '',
    username: user.value.username || '',
    phone: user.value.phone || '',
    email: user.value.email || ''
  })
  editDialogVisible.value = true
}

const saveEdit = handleEditSubmit(async (values) => {
  // 只提交有变更的字段
  const payload = {}
  if (values.username !== user.value.username) payload.username = values.username
  if (values.phone !== user.value.phone) payload.phone = values.phone
  if (values.email !== user.value.email) payload.email = values.email
  if (values.avatar && values.avatar !== user.value.avatar) payload.avatar = values.avatar
  if (Object.keys(payload).length === 0) {
    editDialogVisible.value = false
    return
  }
  try {
    const { error } = await request({ url: '/api/user/update', method: 'POST', data: payload })
    if (error) {
      alert(error.message || '保存失败')
      return
    }
    await userStore.fetchUserInfo()
    editDialogVisible.value = false
    alert('信息保存成功')
  } catch (e) {
    alert(e.message || '保存失败')
  }
})

function openPwdDialog() {
  pwdForm.value = { oldPwd: '', newPwd: '' }
  pwdError.value = ''
  pwdDialogVisible.value = true
}

function closePwdDialog() {
  pwdDialogVisible.value = false
  pwdForm.value = { oldPwd: '', newPwd: '' }
  pwdError.value = ''
}

async function savePwd() {
  try {
    const { error } = await request({ url: '/api/user/changePwd', method: 'POST', data: pwdForm.value })
    if (error) {
      pwdError.value = error.message || '密码校验不通过'
      return
    }
    pwdError.value = ''
    closePwdDialog()
    alert('密码修改成功')
    await userStore.fetchUserInfo() // 密码修改后自动刷新用户信息
  } catch (e) {
    pwdError.value = e.message || '密码校验不通过'
  }
}

function goReview() {
  router.push('/admin/articles/review')
}
</script>

<style scoped>
.user-dropdown {
  position: relative;
  display: flex;
  align-items: center;
  margin-left: 24px;
  z-index: 1000;
}
.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: 3px solid #fff;
  box-shadow: 0 2px 8px rgba(140,166,219,0.18);
  background: #fff;
  cursor: pointer;
  transition: transform 0.28s cubic-bezier(.4,1.5,.5,1), box-shadow 0.18s, width 0.28s, height 0.28s;
  position: relative;
  z-index: 11001;
}
.avatar-active {
  width: 56px;
  height: 56px;
  transform: translateY(8px) scale(1.4);
  box-shadow: 0 8px 32px rgba(140,166,219,0.25);
  border: 4px solid #fff;
}
.dropdown-menu {
  position: absolute;
  top: 66px;
  right: 50%;
  transform: translateX(50%);
  min-width: 280px;
  max-width: 350px;
  background: var(--card-bg);
  color: var(--text-color);
  box-shadow: 0 8px 32px rgba(0,0,0,0.13);
  border-radius: 20px;
  padding: 48px 32px 20px 32px; /* 顶部padding为头像留空间 */
  z-index: 199;
  display: flex;
  flex-direction: column;
  align-items: center;
  border: 1px solid var(--border-color);
}
.dropdown-title {
  font-size: 20px;
  font-family: '微软雅黑', 'Microsoft YaHei', Arial, sans-serif;
  font-weight: bold;
  text-align: center;
  margin-bottom: 16px;
  letter-spacing: 1px;
  color: var(--text-color);
}
.dropdown-divider {
  width: 100%;
  height: 1px;
  background: var(--border-color);
  margin: 0 0 14px 0;
  border: none;
  border-radius: 1px;
}
.user-info {
  width: 100%;
  margin-bottom: 16px;
}
.user-row {
  font-size: 16px;
  margin-bottom: 8px;
  color: var(--text-color);
  font-weight: 500;
  letter-spacing: 1px;
  display: flex;
  justify-content: flex-start;
}
.user-row span {
  color: #7b98d2;
  min-width: 64px;
  display: inline-block;
}
.admin-review-btn {
  width: 100%;
  padding: 12px 0;
  background: linear-gradient(90deg, #409eff 0%, #66b1ff 100%);
  color: #fff;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  font-size: 17px;
  font-weight: bold;
  box-shadow: 0 2px 8px rgba(64,158,255,0.08);
  transition: background 0.2s, box-shadow 0.2s;
  margin-bottom: 12px;
}
.admin-review-btn:hover {
  background: linear-gradient(90deg, #3a8ee6 0%, #59a1ff 100%);
  box-shadow: 0 4px 16px rgba(64,158,255,0.18);
}
.logout-btn {
  width: 100%;
  padding: 12px 0;
  background: linear-gradient(90deg, #f56c6c 0%, #fa9d9d 100%);
  color: #fff;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  font-size: 17px;
  font-weight: bold;
  box-shadow: 0 2px 8px rgba(245,108,108,0.08);
  transition: background 0.2s, box-shadow 0.2s;
  margin-bottom: 12px;
}
.logout-btn:hover {
  background: linear-gradient(90deg, #d9534f 0%, #f56c6c 100%);
  box-shadow: 0 4px 16px rgba(245,108,108,0.18);
}
.edit-info {
  width: 100%;
  text-align: center;
  margin-top: 4px;
}
.edit-btn {
  display: inline-block;
  color: #7b98d2;
  font-size: 15px;
  cursor: pointer;
  padding: 4px 12px;
  border-radius: 8px;
  background: var(--bg-color);
  transition: background 0.18s;
  border: 1px solid var(--border-color);
}
.edit-btn:hover {
  background: var(--border-color);
}
.edit-dialog-mask,
.pwd-dialog-mask {
  position: fixed;
  left: 0; top: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.18);
  z-index: 20000;
}
.edit-dialog {
  position: fixed;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  background: var(--card-bg);
  color: var(--text-color);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.13);
  padding: 28px 36px 18px 36px;
  min-width: 340px;
  max-width: 90vw;
  max-height: 90vh;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  align-items: stretch;
}
.edit-dialog-title {
  font-size: 20px;
  font-weight: bold;
  margin-bottom: 18px;
  text-align: center;
}
.edit-dialog-body {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.edit-avatar {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 8px;
}
.edit-avatar-img {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  border: 3px solid #fff;
  box-shadow: 0 2px 8px rgba(140,166,219,0.18);
  background: #fff;
}
.edit-row {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 16px;
}
.edit-row input {
  flex: 1;
  padding: 4px 8px;
  border-radius: 6px;
  border: 1px solid var(--border-color);
  background: var(--bg-color);
  color: var(--text-color);
}
.edit-row span {
  display: inline-block;
  width: 64px;
  text-align: left;
  flex-shrink: 0;
}
.pwd-btn {
  margin-left: 8px;
  font-size: 14px;
  color: #7b98d2;
  background: none;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  padding: 2px 8px;
  cursor: pointer;
}
.pwd-btn:hover {
  background: var(--border-color);
}
.edit-dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  margin-top: 18px;
}
.edit-cancel, .edit-save {
  font-size: 15px;
  padding: 6px 18px;
  border-radius: 8px;
  border: 1px solid var(--border-color);
  background: var(--bg-color);
  color: var(--text-color);
  cursor: pointer;
}
.edit-save {
  background: #7b98d2;
  color: #fff;
  border: none;
}
.edit-save:hover {
  background: #5d91d2;
}
.pwd-dialog {
  position: fixed;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  background: var(--card-bg);
  color: var(--text-color);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.13);
  padding: 28px 36px 18px 36px;
  min-width: 340px;
  max-width: 90vw;
  max-height: 90vh;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  align-items: stretch;
}
.pwd-dialog-title {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 14px;
  text-align: center;
}
.pwd-dialog-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.pwd-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
}
.pwd-row input {
  flex: 1;
  padding: 4px 8px;
  border-radius: 6px;
  border: 1px solid var(--border-color);
  background: var(--bg-color);
  color: var(--text-color);
}
.pwd-error {
  color: #f56c6c;
  font-size: 14px;
  margin-top: 4px;
}
.pwd-dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  margin-top: 14px;
}
.pwd-cancel, .pwd-save {
  font-size: 15px;
  padding: 6px 18px;
  border-radius: 8px;
  border: 1px solid var(--border-color);
  background: var(--bg-color);
  color: var(--text-color);
  cursor: pointer;
}
.pwd-save {
  background: #7b98d2;
  color: #fff;
  border: none;
}
.pwd-save:hover {
  background: #5d91d2;
}
.dropdown-fade-enter-active, .dropdown-fade-leave-active {
  transition: opacity 0.22s cubic-bezier(.4,1.5,.5,1);
}
.dropdown-fade-enter-from, .dropdown-fade-leave-to {
  opacity: 0;
}
.dropdown-fade-enter-to, .dropdown-fade-leave-from {
  opacity: 1;
}
</style>
