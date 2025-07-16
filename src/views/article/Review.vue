<template>
  <div class="article-review-container">
    <div class="page-header">
      <h1 class="page-title">
        <span class="icon-glow">🔍</span> 文章审核
      </h1>
      <div class="review-stats">
        <div class="stat-item">
          <span class="stat-value">{{ pendingCount }}</span>
          <span class="stat-label">待审核</span>
        </div>
      </div>
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
              <div class="author-info">
                <img :src="article.authorAvatar || defaultAvatar" :alt="article.authorName" class="author-avatar" />
                <span class="author-name">{{ article.authorName }}</span>
              </div>
              <span class="meta-item">
                <span class="meta-icon">📅</span>
                {{ formatDate(article.createdAt) }}
              </span>
            </div>
            
            <div class="meta-right">
              <button @click="viewArticle(article.id)" class="action-btn view-btn">
                查看
              </button>
              <button v-if="article.status === 'PENDING'" @click="approveArticle(article.id)" class="action-btn approve-btn">
                通过
              </button>
              <button v-if="article.status === 'PENDING'" @click="rejectArticle(article.id)" class="action-btn reject-btn">
                拒绝
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else-if="!loading" class="empty-state">
      <div class="empty-icon">📝</div>
      <h3>暂无待审核文章</h3>
      <p>当前没有需要审核的文章。</p>
    </div>

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

    <!-- 审核弹窗 -->
    <div v-if="reviewDialogVisible" class="review-dialog-overlay" @click="closeReviewDialog">
      <div class="review-dialog" @click.stop>
        <div class="dialog-header">
          <h3>审核文章</h3>
          <button @click="closeReviewDialog" class="close-btn">×</button>
        </div>
        
        <div class="dialog-content">
          <div class="article-preview">
            <h4>{{ selectedArticle?.title }}</h4>
            <p>{{ getExcerpt(selectedArticle?.content) }}</p>
          </div>
          
          <div class="review-form">
            <label class="form-label">审核意见：</label>
            <textarea 
              v-model="commentField.value" 
              placeholder="请输入审核意见（可选）..."
              class="review-textarea"
              rows="4"
            ></textarea>
            <div v-if="errors.comment" class="error-message">{{ errors.comment }}</div>
          </div>
        </div>
        
        <div class="dialog-actions">
          <button @click="closeReviewDialog" class="cancel-btn">取消</button>
          <button @click="submitReview" class="approve-btn">通过</button>
          <button @click="submitReview" class="reject-btn">拒绝</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getPendingArticles, reviewArticle } from '@/api/article.ts'
import defaultCover from '@/views/css/png/登录注册图标 (1).png'
import defaultAvatar from '@/views/css/png/头像.png'
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
const currentStatus = ref('PENDING')
const reviewDialogVisible = ref(false)
const selectedArticle = ref(null)
const reviewComment = ref('')
const reviewAction = ref('')

// 审核表单校验规则
const reviewSchema = yup.object({
  comment: yup.string().required('请输入审核意见').max(200, '审核意见不能超过200字'),
  action: yup.string().required('请选择审核操作')
})
const { handleSubmit, errors, values, setValues, resetForm } = useForm({
  validationSchema: reviewSchema,
  initialValues: { comment: '', action: '' }
})
const commentField = useField('comment')
const actionField = useField('action')

function openReviewDialog(articleId) {
  selectedArticle.value = articles.value.find(a => a.id === articleId)
  setValues({ comment: '', action: '' })
  reviewDialogVisible.value = true
}

const submitReview = handleSubmit(async (formValues) => {
  loading.value = true
  try {
    await reviewArticle(selectedArticle.value.id, formValues)
    alert('审核成功！')
    reviewDialogVisible.value = false
    // 刷新列表等后续操作
    fetchArticles()
  } catch (error) {
    alert('审核失败：' + (error.response?.data || '未知错误'))
  } finally {
    loading.value = false
  }
})

// 状态选项
const statusOptions = [
  { value: 'PENDING', label: '待审核' }
]

// 计算属性
const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

// 统计数据
const pendingCount = ref(0)
const approvedCount = ref(0)
const rejectedCount = ref(0)

// 获取待审核文章列表
const fetchArticles = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      status: currentStatus.value
    }
    const response = await getPendingArticles(params)
    articles.value = response.data.records
    total.value = response.data.total
    
    // 更新统计数据
    updateStats()
  } catch (error) {
    console.error('获取文章列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 更新统计数据
const updateStats = async () => {
  try {
    // 这里可以调用API获取统计数据
    // 暂时使用模拟数据
    pendingCount.value = articles.value.filter(a => a.status === 'PENDING').length
    approvedCount.value = articles.value.filter(a => a.status === 'APPROVED').length
    rejectedCount.value = articles.value.filter(a => a.status === 'REJECTED').length
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

// 按状态筛选
const filterByStatus = (status) => {
  currentStatus.value = status
  currentPage.value = 1
  fetchArticles()
}

// 切换页面
const changePage = (page) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
    fetchArticles()
  }
}

// 查看文章
const viewArticle = (id) => {
  router.push(`/articles/${id}`)
}

// 通过文章
const approveArticle = (id) => {
  openReviewDialog(id)
  actionField.value = 'APPROVE'
}

// 拒绝文章
const rejectArticle = (id) => {
  openReviewDialog(id)
  actionField.value = 'REJECT'
}

// 关闭审核弹窗
const closeReviewDialog = () => {
  reviewDialogVisible.value = false
  selectedArticle.value = null
  resetForm()
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    'DRAFT': '草稿',
    'PENDING': '待审核',
    'APPROVED': '已通过',
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
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 获取状态数量
const getStatusCount = (status) => {
  return articles.value.filter(article => article.status === status).length
}

// 图片加载错误处理
const handleImageError = (event) => {
  event.target.src = defaultCover
}

// 页面加载时获取文章列表
onMounted(() => {
  fetchArticles()
})
</script>

<style scoped>
.article-page {
  padding-top: 64px;
}
.article-review-container {
  min-height: 100vh;
  background: var(--bg-color);
  padding-top: 64px;
  padding-left: 32px;
  padding-right: 32px;
  position: relative;
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

.review-stats {
  display: flex;
  gap: 24px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.stat-value {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--tool-title-color);
}

.stat-label {
  font-size: 0.9rem;
  color: var(--tool-desc-color);
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
  align-items: center;
  gap: 16px;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.author-avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  object-fit: cover;
}

.author-name {
  font-size: 0.9rem;
  color: var(--tool-title-color);
  font-weight: 600;
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

.approve-btn {
  background: #10b981;
  color: white;
}

.approve-btn:hover {
  background: #059669;
}

.reject-btn {
  background: #ef4444;
  color: white;
}

.reject-btn:hover {
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
  margin: 0;
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

/* 审核弹窗 */
.review-dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10000;
}

.review-dialog {
  background: var(--card-bg);
  border-radius: 16px;
  padding: 24px;
  max-width: 500px;
  width: 90%;
  max-height: 80vh;
  overflow-y: auto;
  box-shadow: 0 8px 32px rgba(0,0,0,0.3);
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.dialog-header h3 {
  font-size: 1.3rem;
  font-weight: 700;
  color: var(--text-color);
  margin: 0;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  color: var(--tool-desc-color);
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  transition: background 0.2s;
}

.close-btn:hover {
  background: var(--border-color);
}

.dialog-content {
  margin-bottom: 20px;
}

.article-preview {
  margin-bottom: 16px;
  padding: 16px;
  background: var(--bg-color);
  border-radius: 8px;
}

.article-preview h4 {
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--text-color);
  margin: 0 0 8px 0;
}

.article-preview p {
  color: var(--tool-desc-color);
  font-size: 0.9rem;
  line-height: 1.5;
  margin: 0;
}

.review-form {
  margin-bottom: 16px;
}

.form-label {
  display: block;
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-color);
  margin-bottom: 8px;
}

.review-textarea {
  width: 100%;
  box-sizing: border-box;
  padding: 12px;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  background: var(--bg-color);
  color: var(--text-color);
  font-size: 0.9rem;
  resize: vertical;
  min-height: 80px;
  max-height: 180px;
  overflow-y: auto;
}

.review-textarea:focus {
  outline: none;
  border-color: var(--tool-title-color);
}

.error-message {
  color: #ef4444;
  font-size: 0.8rem;
  margin-top: 4px;
}

.dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.cancel-btn,
.approve-btn,
.reject-btn {
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
}

.cancel-btn {
  background: var(--bg-color);
  color: var(--text-color);
  border: 1px solid var(--border-color);
}

.cancel-btn:hover {
  background: var(--border-color);
}

.approve-btn {
  background: #10b981;
  color: white;
}

.approve-btn:hover {
  background: #059669;
}

.reject-btn {
  background: #ef4444;
  color: white;
}

.reject-btn:hover {
  background: #dc2626;
}

@media (max-width: 768px) {
  .article-review-container {
    padding: 16px 12px;
  }
  
  .page-header {
    flex-direction: column;
    align-items: stretch;
  }
  
  .review-stats {
    justify-content: center;
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
  
  .review-dialog {
    width: 95%;
    padding: 16px;
  }
  
  .dialog-actions {
    flex-direction: column;
  }
}
</style> 