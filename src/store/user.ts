import { defineStore } from 'pinia'
import { request } from '@/utils/request.ts'

export interface UserInfo {
  id?: number;
  username?: string;
  phone?: string;
  email?: string;
  userType?: string;
  avatar?: string;
}

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || 'null') as UserInfo | null
  }),
  getters: {
    isLogin: state => !!state.token,
    isAdmin: state => state.userInfo?.userType === 'ADMIN'
  },
  actions: {
    setToken(token: string) {
      this.token = token
      localStorage.setItem('token', token)
      this.fetchUserInfo() // 登录后自动拉取用户信息
    },
    clearToken() {
      this.token = ''
      localStorage.removeItem('token')
    },
    setUserInfo(info: UserInfo) {
      this.userInfo = info
      localStorage.setItem('userInfo', JSON.stringify(info))
    },
    clearUserInfo() {
      this.userInfo = null
      localStorage.removeItem('userInfo')
    },
    async fetchUserInfo() {
      if (!this.token) {
        this.clearUserInfo()
        return
      }
      const { data, error } = await request({ url: '/api/user/info', method: 'GET' })
      if (!error && data) {
        this.setUserInfo(data)
      } else {
        this.clearUserInfo()
      }
    },
    logout() {
      this.clearToken()
      this.clearUserInfo()
    }
  }
}) 