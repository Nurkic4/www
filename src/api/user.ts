import axios from 'axios'
import { request } from '@/utils/request'

const BASE_URL = '' // 使用相对路径，让Vite代理处理

// 全局请求拦截器，自动加 token
axios.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = 'Bearer ' + token
  }
  // 添加CORS相关请求头
  config.headers['Content-Type'] = 'application/json'
  return config
}, error => Promise.reject(error))

export function register(data: { username: string; password: string; email: string }) {
  return axios.post(`${BASE_URL}/api/user/register`, data)
}

export function login(data: { username: string; password: string }) {
  return axios.post(`${BASE_URL}/api/user/login`, data)
}

export function getUserInfo() {
  return axios.get(`${BASE_URL}/api/user/info`)
}

export function updateAvatar(data: { avatar: string }) {
  return axios.post(`${BASE_URL}/api/user/avatar`, data)
}

export function updateUserInfo(data: { username: string; email: string }) {
  return axios.post(`${BASE_URL}/api/user/update`, data)
}

export function changePwd(data: { oldPassword: string; newPassword: string }) {
  return axios.post(`${BASE_URL}/api/user/changePwd`, data)
} 