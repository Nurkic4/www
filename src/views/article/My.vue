<template>
  <div class="my-article-page">
    <div class="page-header">
      <h1 class="page-title">
        <span class="icon-glow">📚</span> 我的文章
      </h1>
      <button @click="createArticle" class="create-btn">
        <span class="create-icon">+</span>
        创建文章
      </button>
    </div>

    <!-- 状态筛选 -->
    <div class="status-filter">
      <button 
        v-for="status in statusOptions" 
        :key="status.value"
        @click="filterByStatus(status.value)"
        :class="['filter-btn', { active: currentStatus === status.value }]"
      >
        {{ status.label }}
        <span class="count-badge">{{ getStatusCount(status.value) }}</span>
      </button>
    </div>

    <!-- 文章列表 -->
    <div class="articles-list" v-if="articles.length > 0">
      <div 
        v-for="article in articles" 
        :key="article.id" 
        class="article-item"
      >
        <div class="article-cover">
          <img 
            :src="article.coverImage || defaultCover" 
            :alt="article.title"
            @error="handleImageError"
          />
          <div class="article-status" :class="article.status.toLowerCase()">
            {{ getStatusText(article.status) }}
          </div>
        </div>
        
        <div class="article-info">
          <h3 class="article-title">{{ article.title }}</h3>
          <p class="article-excerpt">{{ getExcerpt(article.content) }}</p>
          
          <div class="article-meta">
            <div class="meta-left">
              <span class="meta-item">
                <span class="meta-icon">❤️</span>
                {{ article.likeCount || 0 }}
              </span>
              <span class="meta-item">
                <span class="meta-icon">📅</span>
                {{ formatDate(article.updatedAt || article.createdAt) }}
              </span>
            </div>
            
            <div class="meta-right">
              <button @click="viewArticle(article.id)" class="action-btn view-btn">
                查看
              </button>
              <button @click="editArticle(article.id)" class="action-btn edit-btn">
                编辑
              </button>
              <button @click="deleteArticle(article.id)" class="action-btn delete-btn">
                删除
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else-if="!loading && currentStatus === 'ALL'" class="empty-state">
      <div class="empty-icon">📝</div>
      <h3>暂无文章</h3>
      <p>您还没有创建任何文章，快来写第一篇吧！</p>
      <button @click="createArticle" class="create-btn">创建文章</button>
    </div>
    <!-- 其它标签下无文章时不显示空状态和按钮 -->

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <p>正在加载文章...</p>
    </div>

    <!-- 分页 -->
    <div v-if="total > 0" class="pagination">
      <button 
        @click="changePage(currentPage - 1)" 
        :disabled="currentPage <= 1"
        class="page-btn"
      >
        上一页
      </button>
      <span class="page-info">{{ currentPage }} / {{ totalPages }}</span>
      <button 
        @click="changePage(currentPage + 1)" 
        :disabled="currentPage >= totalPages"
        class="page-btn"
      >
        下一页
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMyArticles, deleteArticle as deleteArticleApi, updateArticle } from '@/api/article.ts'
import defaultCover from '@/views/css/png/登录注册图标 (1).png'
// vee-validate 集成
import { useForm, useField } from 'vee-validate'
import * as yup from 'yup'

const router = useRouter()

// 响应式数据
const articles = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const currentStatus = ref('ALL')

// 编辑表单校验规则
const editSchema = yup.object({
  title: yup.string().required('请输入文章标题').max(200, '标题不能超过200字'),
  content: yup.string().required('请输入文章内容').max(10000, '内容不能超过10000字'),
  coverImage: yup.string().nullable(),
  images: yup.array().default([])
})
const { handleSubmit, errors, values, setValues, resetForm } = useForm({
  validationSchema: editSchema,
  initialValues: { title: '', content: '', coverImage: '', images: [] }
})
const titleField = useField('title')
const contentField = useField('content')
const coverImageField = useField('coverImage')
const imagesField = useField('images')

function openEditDialog(article) {
  currentArticleId.value = article.id
  setValues({
    title: article.title || '',
    content: article.content || '',
    coverImage: article.coverImage || '',
    images: article.images || []
  })
  editDialogVisible.value = true
}

const saveEdit = handleSubmit(async (formValues) => {
  loading.value = true
  try {
    await updateArticle(currentArticleId.value, formValues)
    alert('保存成功！')
    editDialogVisible.value = false
    // 刷新列表等后续操作
  } catch (error) {
    alert('保存失败：' + (error.response?.data || '未知错误'))
  } finally {
    loading.value = false
  }
})

// 状态选项
const statusOptions = [
  { value: 'ALL', label: '全部' },
  { value: 'DRAFT', label: '草稿' },
  { value: 'PENDING', label: '待审核' },
  { value: 'APPROVED', label: '已发布' },
  { value: 'REJECTED', label: '已拒绝' }
]

// 计算属性
const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

// 获取我的文章列表
const fetchMyArticles = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      status: currentStatus.value === 'ALL' ? undefined : currentStatus.value
    }
    const response = await getMyArticles(params)
    articles.value = response.data.records
    total.value = response.data.total
  } catch (error) {
    console.error('获取我的文章失败:', error)
  } finally {
    loading.value = false
  }
}

// 按状态筛选
const filterByStatus = (status) => {
  currentStatus.value = status
  currentPage.value = 1
  fetchMyArticles()
}

// 切换页面
const changePage = (page) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
    fetchMyArticles()
  }
}

// 创建文章
const createArticle = () => {
  router.push('/articles/create')
}

// 查看文章
const viewArticle = (id) => {
  router.push(`/articles/${id}`)
}

// 编辑文章
const editArticle = (id) => {
  router.push(`/articles/edit/${id}`)
}

// 删除文章
const deleteArticle = async (id) => {
  if (!confirm('确定要删除这篇文章吗？删除后无法恢复。')) {
    return
  }
  
  try {
    await deleteArticleApi(id)
    alert('文章删除成功！')
    fetchMyArticles()
  } catch (error) {
    console.error('删除文章失败:', error)
    alert('删除失败：' + (error.response?.data || '未知错误'))
  }
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    'DRAFT': '草稿',
    'PENDING': '待审核',
    'APPROVED': '已发布',
    'REJECTED': '已拒绝'
  }
  return statusMap[status] || status
}

// 获取文章摘要
const getExcerpt = (content) => {
  if (!content) return '暂无内容'
  const text = content.replace(/<[^>]*>/g, '')
  return text.length > 100 ? text.substring(0, 100) + '...' : text
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

// 获取状态数量
const getStatusCount = (status) => {
  // 这里可以根据实际需求实现状态统计
  return articles.value.filter(article => 
    status === 'ALL' || article.status === status
  ).length
}

// 图片加载错误处理
const handleImageError = (event) => {
  event.target.src = defaultCover
}

// 页面加载时获取文章列表
onMounted(() => {
  fetchMyArticles()
})
</script>

<style scoped>
.my-article-page {
  min-height: calc(100vh - 80px - 48px);
  background: var(--bg-color);
  padding-top: 80px;
  padding-bottom: 48px;
  padding-left: 32px;
  padding-right: 32px;
}
@media (max-width: 900px) {
  .my-article-page {
    padding-left: 12px;
    padding-right: 12px;
    padding-top: 64px;
    padding-bottom: 24px;
  }
}
.my-header, .my-title, .my-actions {
  margin-top: 24px;
}
.my-content, .el-pagination {
  margin-bottom: 32px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
  flex-wrap: wrap;
  gap: 16px;
}

.page-title {
  font-size: 2.4rem;
  font-weight: 900;
  color: var(--tool-title-color);
  margin: 0;
  display: flex;
  align-items: center;
  gap: 12px;
}

.create-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  background: linear-gradient(90deg, var(--tool-title-color) 0%, #06b6d4 100%);
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.create-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(78,107,222,0.2);
}

.create-icon {
  font-size: 1.2rem;
  font-weight: bold;
}

.status-filter {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.filter-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  color: var(--text-color);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 0.9rem;
}

.filter-btn:hover {
  background: var(--border-color);
}

.filter-btn.active {
  background: var(--tool-title-color);
  color: white;
  border-color: var(--tool-title-color);
}

.count-badge {
  background: rgba(255,255,255,0.2);
  color: inherit;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 0.8rem;
  font-weight: 600;
}

.articles-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-bottom: 32px;
}

.article-item {
  background: var(--card-bg);
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(78,107,222,0.08);
  border: 1px solid var(--border-color);
  display: flex;
  gap: 20px;
  transition: all 0.3s ease;
}

.article-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(78,107,222,0.12);
}

.article-cover {
  position: relative;
  width: 120px;
  height: 80px;
  flex-shrink: 0;
  border-radius: 8px;
  overflow: hidden;
}

.article-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.article-status {
  position: absolute;
  top: 4px;
  right: 4px;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 0.7rem;
  font-weight: 600;
  color: white;
}

.article-status.draft {
  background: #6b7280;
}

.article-status.pending {
  background: #f59e0b;
}

.article-status.approved {
  background: #10b981;
}

.article-status.rejected {
  background: #ef4444;
}

.article-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.article-title {
  font-size: 1.2rem;
  font-weight: 700;
  color: var(--text-color);
  margin: 0 0 8px 0;
  line-height: 1.4;
}

.article-excerpt {
  color: var(--tool-desc-color);
  font-size: 0.9rem;
  line-height: 1.5;
  margin: 0 0 12px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.meta-left {
  display: flex;
  gap: 16px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 0.85rem;
  color: var(--tool-desc-color);
}

.meta-icon {
  font-size: 0.9rem;
}

.meta-right {
  display: flex;
  gap: 8px;
}

.action-btn {
  padding: 6px 12px;
  border-radius: 6px;
  font-size: 0.85rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
}

.view-btn {
  background: var(--bg-color);
  color: var(--tool-title-color);
  border: 1px solid var(--border-color);
}

.view-btn:hover {
  background: var(--border-color);
}

.edit-btn {
  background: var(--tool-title-color);
  color: white;
}

.edit-btn:hover {
  background: #4a5a7a;
}

.delete-btn {
  background: #ef4444;
  color: white;
}

.delete-btn:hover {
  background: #dc2626;
}

.empty-state {
  text-align: center;
  padding: 80px 20px;
  color: var(--tool-desc-color);
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 16px;
}

.empty-state h3 {
  font-size: 1.5rem;
  margin: 0 0 8px 0;
  color: var(--text-color);
}

.empty-state p {
  margin: 0 0 24px 0;
  font-size: 1rem;
}

.loading-state {
  text-align: center;
  padding: 80px 20px;
  color: var(--tool-desc-color);
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid var(--border-color);
  border-top: 4px solid var(--tool-title-color);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 16px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-top: 32px;
}

.page-btn {
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  color: var(--text-color);
  padding: 8px 16px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.page-btn:hover:not(:disabled) {
  background: var(--border-color);
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  font-size: 0.9rem;
  color: var(--tool-desc-color);
}

@media (max-width: 768px) {
  .article-my-container {
    padding: 16px 12px;
  }
  
  .page-header {
    flex-direction: column;
    align-items: stretch;
  }
  
  .status-filter {
    gap: 8px;
  }
  
  .filter-btn {
    padding: 6px 12px;
    font-size: 0.8rem;
  }
  
  .article-item {
    flex-direction: column;
    gap: 12px;
  }
  
  .article-cover {
    width: 100%;
    height: 120px;
  }
  
  .article-meta {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .meta-left {
    gap: 12px;
  }
  
  .meta-right {
    width: 100%;
    justify-content: flex-end;
  }
}
</style> 