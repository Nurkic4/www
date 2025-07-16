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

// 文章管理接口

/**
 * 创建文章
 * @param {Object} data - 文章数据
 * @param {string} data.title - 文章标题
 * @param {string} data.content - 文章内容
 * @param {string} data.coverImage - 封面图片URL
 * @param {Array} data.images - 文章插图数组
 * @param {string} data.status - 状态，默认为DRAFT
 */
export function createArticle(data: {
  title: string;
  content: string;
  coverImage?: string;
  images?: string[];
  status?: string;
}) {
  return axios.post(`${BASE_URL}/api/article/create`, data)
}

/**
 * 获取文章列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码，默认1
 * @param {number} params.size - 每页数量，默认10
 * @param {string} params.status - 状态筛选
 * @param {number} params.authorId - 作者ID筛选
 * @param {string} params.keyword - 关键词搜索
 */
export function getArticleList(params: {
  page?: number;
  size?: number;
  status?: string;
  authorId?: number;
  keyword?: string;
}) {
  return axios.get(`${BASE_URL}/api/article/list`, { params })
}

/**
 * 获取文章详情
 * @param {number} id - 文章ID
 */
export function getArticleDetail(id: number) {
  return axios.get(`${BASE_URL}/api/article/${id}`)
}

/**
 * 更新文章
 * @param {number} id - 文章ID
 * @param {Object} data - 文章数据
 */
export function updateArticle(id: number, data: {
  title?: string;
  content?: string;
  coverImage?: string;
  images?: string[];
  status?: string;
}) {
  return axios.put(`${BASE_URL}/api/article/${id}`, data)
}

/**
 * 删除文章
 * @param {number} id - 文章ID
 */
export function deleteArticle(id: number) {
  return axios.delete(`${BASE_URL}/api/article/${id}`)
}

/**
 * 提交审核
 * @param {number} id - 文章ID
 */
export function submitArticle(id: number) {
  return axios.post(`${BASE_URL}/api/article/${id}/submit`)
}

// 审核管理接口（管理员专用）

/**
 * 获取待审核文章列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码，默认1
 * @param {number} params.size - 每页数量，默认10
 */
export function getPendingArticles(params: {
  page?: number;
  size?: number;
}) {
  return axios.get(`${BASE_URL}/api/article/pending`, { params })
}

/**
 * 审核文章
 * @param {number} id - 文章ID
 * @param {Object} data - 审核数据
 * @param {string} data.action - 审核动作：APPROVE-通过，REJECT-拒绝
 * @param {string} data.comment - 审核意见
 */
export function reviewArticle(id: number, data: {
  action: 'APPROVE' | 'REJECT';
  comment: string;
}) {
  return axios.post(`${BASE_URL}/api/article/${id}/review`, data)
}

// 用户交互接口

/**
 * 点赞文章
 * @param {number} id - 文章ID
 */
export function likeArticle(id: number) {
  return axios.post(`${BASE_URL}/api/article/${id}/like`)
}

/**
 * 取消点赞
 * @param {number} id - 文章ID
 */
export function unlikeArticle(id: number) {
  return axios.delete(`${BASE_URL}/api/article/${id}/like`)
}

/**
 * 增加浏览次数
 * @param {number} id - 文章ID
 */
export function viewArticle(id: number) {
  return axios.post(`${BASE_URL}/api/article/${id}/view`)
}

// 文件上传接口

/**
 * 上传图片
 * @param {File} file - 图片文件
 * @param {string} type - 图片类型：cover-封面，content-内容插图
 */
export function uploadImage(file: File, type: 'cover' | 'content' = 'content') {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('type', type)
  
  return axios.post(`${BASE_URL}/api/upload/image`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 用户文章管理接口

/**
 * 获取我的文章列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码，默认1
 * @param {number} params.size - 每页数量，默认10
 * @param {string} params.status - 状态筛选
 */
export function getMyArticles(params: {
  page?: number;
  size?: number;
  status?: string;
}) {
  return axios.get(`${BASE_URL}/api/article/my`, { params })
}

// 导出所有接口
export default {
  // 文章管理
  createArticle,
  getArticleList,
  getArticleDetail,
  updateArticle,
  deleteArticle,
  submitArticle,
  
  // 审核管理
  getPendingArticles,
  reviewArticle,
  
  // 用户交互
  likeArticle,
  unlikeArticle,
  viewArticle,
  
  // 文件上传
  uploadImage,
  
  // 用户文章管理
  getMyArticles
} 