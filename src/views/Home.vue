<template>
  <el-row justify="center" style="margin-top: 100px;">
    <el-col :span="12">
      <el-card class="home-card">
        <div style="display:flex;justify-content:space-between;align-items:center;">
          <h2 style="color:var(--text-color);">主页</h2>
          <el-button type="danger" @click="logout">退出登录</el-button>
        </div>
        <el-divider />
        <p style="font-size:18px;text-align:center;color:var(--text-color);">欢迎来到主页！</p>
      </el-card>
    </el-col>
  </el-row>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
const router = useRouter()
const userStore = useUserStore()
const logout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.home-card {
  background: var(--card-bg) !important;
  color: var(--text-color) !important;
  border: 1px solid var(--border-color) !important;
}
</style> 