import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'

const instance = axios.create({
  timeout: 180000 
})

// 请求拦截：只给 /api/ 开头的请求加 token
instance.interceptors.request.use(config => {
  // 为AI相关请求设置更长的超时时间
  if (config.url && (config.url.includes('/api/sparkai') || config.url.includes('sparkai'))) {
    config.timeout = 180000 // AI请求180秒超时
  }
  
  // 只给本地后端接口加 token
  if (config.url && config.url.startsWith('/api/')) {
    // 优先从 pinia 获取 token
    let token = ''
    try {
      const userStore = useUserStore()
      token = userStore.token
    } catch (e) {
      token = localStorage.getItem('token')
    }
    if (token) {
      config.headers = config.headers || {}
      config.headers['Authorization'] = 'Bearer ' + token
    }
  }
  return config
})

// 响应拦截：统一错误处理，401 自动登出
instance.interceptors.response.use(
  response => response,
  error => {
    let msg = '请求失败，请稍后重试'
    if (error.response && error.response.data) {
      msg = error.response.data.message || error.response.data || msg
      if (error.response.status === 401) {
        try {
          const userStore = useUserStore()
          userStore.logout()
        } catch {}
        window.location.href = '/login'
      }
    } else if (error.message) {
      msg = error.message
    }
    ElMessage.error(msg)
    return Promise.reject(error)
  }
)

export async function request(config: { url: string; method?: string; data?: any; params?: any; headers?: any }) {
  try {
    const response = await instance(config)
    return { 
      data: response.data, 
      status: response.status,
      statusText: response.statusText,
      ok: response.status >= 200 && response.status < 300,
      error: null 
    }
  } catch (error) {
    return { 
      data: null, 
      status: error.response?.status || 0,
      statusText: error.response?.statusText || error.message,
      ok: false,
      error 
    }
  }
}

export default instance 