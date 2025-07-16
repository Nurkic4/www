<template>
  <div class="article-list-container">
    <!-- 我的文章跳转按钮 -->
    <button class="my-articles-btn" @click="goMyArticles">我的文章</button>
    
    <!-- 搜索栏 -->
    <div class="search-section">
      <div class="search-bar">
        <input
          v-model="searchKeyword"
          @input="handleSearch"
          placeholder="搜索文章标题或内容..."
          class="search-input"
        />
        <button @click="handleSearch" class="search-btn">🔍</button>
      </div>
    </div>

    <!-- 文章网格 -->
    <div class="articles-grid" v-if="articles && articles.length > 0">
      <div 
        v-for="article in articles" 
        :key="article.id" 
        class="article-card"
        @click="viewArticle(article.id)"
      >
        <div class="article-cover">
          <img 
            :src="article.coverImage || defaultCover" 
            :alt="article.title"
            @error="handleImageError"
          />
          <!-- 移除“已发布”状态标签 -->
          <!-- <div class="article-status" :class="article.status.toLowerCase()">
            {{ getStatusText(article.status) }}
          </div> -->
        </div>
        <div class="article-content">
          <h3 class="article-title">{{ article.title }}</h3>
          <p class="article-excerpt">{{ getExcerpt(article.content) }}</p>
          <div class="article-meta">
            <div class="author-info">
              <img :src="article.authorAvatar || defaultAvatar" :alt="article.authorName" class="author-avatar" />
              <span class="author-name">{{ article.authorName }}</span>
            </div>
            <div class="article-stats">
              <!-- 删除浏览量 -->
              <!-- <span class="stat-item">
                <span class="stat-icon">👁️</span>
                {{ article.viewCount }}
              </span> -->
              <span class="stat-item">
                <span class="stat-icon">❤️</span>
                {{ article.likeCount }}
              </span>
              <span class="stat-item">
                <span class="stat-icon">📅</span>
                {{ formatDate(article.publishedAt || article.createdAt) }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else-if="!loading" class="empty-state">
      <div class="empty-icon">📝</div>
      <h3>这里什么都没有</h3>
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

    <!-- 创建文章按钮 -->
    <div class="create-article-btn" @click="createArticle">
      <span class="plus-icon">+</span>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { getArticleList } from '@/api/article.ts'
import defaultCover from '@/views/css/png/登录注册图标 (1).png'
import defaultAvatar from '@/views/css/png/头像.png'

const router = useRouter()

// 响应式数据
const articles = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)
const searchKeyword = ref('')
const searchTimeout = ref(null)

// 计算属性
const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

// 获取文章列表
const fetchArticles = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      status: 'APPROVED', // 只显示已发布的文章
      keyword: searchKeyword.value
    }
    const response = await getArticleList(params)
    console.log('API响应:', response)
    if (response.data && response.data.records) {
      articles.value = response.data.records
      total.value = response.data.total || 0
    } else {
      articles.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('获取文章列表失败:', error)
    articles.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 搜索处理
const handleSearch = () => {
  if (searchTimeout.value) {
    clearTimeout(searchTimeout.value)
  }
  searchTimeout.value = setTimeout(() => {
    currentPage.value = 1
    fetchArticles()
  }, 500)
}

// 切换页面
const changePage = (page) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
    fetchArticles()
  }
}

// 查看文章详情
const viewArticle = (id) => {
  router.push(`/articles/${id}`)
}

// 创建文章
const createArticle = () => {
  router.push('/articles/create')
}

// 跳转到我的文章
const goMyArticles = () => {
  router.push('/articles/my')
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
  // 移除HTML标签
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
.article-list-container {
  min-height: calc(100vh - 80px - 48px);
  background: var(--bg-color);
  padding-top: 80px;
  padding-bottom: 48px;
  padding-left: 32px;
  padding-right: 32px;
  position: relative;
}
@media (max-width: 900px) {
  .article-list-container {
    padding-left: 12px;
    padding-right: 12px;
    padding-top: 64px;
    padding-bottom: 24px;
  }
}
.page-header, .articles-header, .articles-grid, .my-articles-btn {
  margin-top: 24px;
}
.articles-grid, .el-pagination {
  margin-bottom: 32px;
}

.article-page {
  padding-top: 64px;
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

/* 删除 .search-bar 相关样式 */

.articles-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 24px;
  margin-bottom: 32px;
}

.article-card {
  background: var(--card-bg);
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(78,107,222,0.08);
  border: 1px solid var(--border-color);
  cursor: pointer;
  transition: all 0.3s ease;
}

.article-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(78,107,222,0.15);
}

.article-cover {
  position: relative;
  height: 200px;
  overflow: hidden;
}

.article-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.article-card:hover .article-cover img {
  transform: scale(1.05);
}

.article-status {
  position: absolute;
  top: 12px;
  right: 12px;
  padding: 4px 8px;
  border-radius: 6px;
  font-size: 0.8rem;
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

.article-content {
  padding: 20px;
}

.article-title {
  font-size: 1.3rem;
  font-weight: 700;
  color: var(--text-color);
  margin: 0 0 12px 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-excerpt {
  color: var(--tool-desc-color);
  font-size: 0.95rem;
  line-height: 1.6;
  margin: 0 0 16px 0;
  display: -webkit-box;
  -webkit-line-clamp: 3;
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

.article-stats {
  display: flex;
  gap: 12px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 0.85rem;
  color: var(--tool-desc-color);
}

.stat-icon {
  font-size: 0.9rem;
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

.create-btn {
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

.create-article-btn {
  position: fixed;
  bottom: 32px;
  right: 32px;
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, var(--tool-title-color) 0%, #06b6d4 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 16px rgba(78,107,222,0.3);
  transition: all 0.3s ease;
  z-index: 1000;
}

.create-article-btn:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 20px rgba(78,107,222,0.4);
}

.plus-icon {
  color: white;
  font-size: 2rem;
  font-weight: bold;
}

.my-articles-btn {
  position: absolute;
  top: 80px;
  right: 32px;
  z-index: 10;
  background: linear-gradient(90deg, var(--tool-title-color) 0%, #06b6d4 100%);
  color: white;
  border: none;
  padding: 10px 22px;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(78,107,222,0.10);
  cursor: pointer;
  transition: background 0.2s, box-shadow 0.2s;
}
.my-articles-btn:hover {
  background: linear-gradient(90deg, #06b6d4 0%, var(--tool-title-color) 100%);
  box-shadow: 0 4px 16px rgba(78,107,222,0.18);
}

/* 搜索栏样式 */
.search-section {
  margin: 24px 0 32px 0;
  display: flex;
  justify-content: center;
}

.search-bar {
  display: grid;
  grid-template-columns: 1fr auto;
  align-items: center;
  background: var(--card-bg);
  border-radius: 12px;
  padding: 4px 12px;
  border: 1px solid var(--border-color);
  box-shadow: 0 2px 8px rgba(78,107,222,0.08);
  max-width: 500px;
  width: 100%;
}

:deep(.search-input) {
  background: var(--card-bg) !important;
  border: none !important;
  box-shadow: none !important;
  color: var(--text-color) !important;
  width: 100%;
  min-width: 0;
  padding: 8px 0;
  display: block;
}

.search-input::placeholder {
  color: var(--tool-desc-color);
}

.search-btn {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 1.2rem;
  color: var(--tool-title-color);
  padding: 8px;
  border-radius: 6px;
  transition: background 0.2s;
}

.search-btn:hover {
  background: var(--border-color);
}

/* 移除多余的主题适配代码（如果有） */
[data-theme='light'] .search-input,
[data-theme='dark'] .search-input {
  background: unset;
  color: unset;
}

@media (max-width: 768px) {
  .article-list-container {
    padding: 16px 12px;
  }
  
  .search-section {
    margin: 16px 0 24px 0;
  }
  
  .search-bar {
    max-width: 100%;
  }
  
  .articles-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .create-article-btn {
    bottom: 20px;
    right: 20px;
    width: 50px;
    height: 50px;
  }
  
  .plus-icon {
    font-size: 1.5rem;
  }
}
</style> 