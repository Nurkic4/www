<template>
  <div class="article-page">
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <p>正在加载文章...</p>
    </div>

    <!-- 文章内容 -->
    <div v-else-if="article" class="article-content">
      <!-- 文章头部 -->
      <div class="article-header">
        <div class="article-meta">
          <div class="article-status" :class="article.status.toLowerCase()">
            {{ getStatusText(article.status) }}
          </div>
          <div class="article-date">
            {{ formatDate(article.publishedAt || article.createdAt) }}
          </div>
        </div>
        
        <h1 class="article-title">{{ article.title }}</h1>
        
        <div class="article-author">
          <img :src="article.authorAvatar || defaultAvatar" :alt="article.authorName" class="author-avatar" />
          <div class="author-info">
            <div class="author-name">{{ article.authorName }}</div>
            <div class="author-desc">作者</div>
          </div>
        </div>
      </div>

      <!-- 封面图片 -->
      <div v-if="article.coverImage" class="article-cover">
        <img :src="article.coverImage" :alt="article.title" />
      </div>

      <!-- 文章正文 -->
      <div class="article-body">
        <MarkdownRenderer :content="article.content" :images="article.images" />
      </div>

      <!-- 文章统计 -->
      <div class="article-stats">
        <!-- 删除浏览量 -->
        <!-- <div class="stat-item">
          <span class="stat-icon">👁️</span>
          <span class="stat-value">{{ article.viewCount }}</span>
          <span class="stat-label">浏览</span>
        </div> -->
        <div class="stat-item">
          <span class="stat-icon">❤️</span>
          <span class="stat-value">{{ article.likeCount }}</span>
          <span class="stat-label">点赞</span>
        </div>
        <div class="stat-item">
          <span class="stat-icon">📝</span>
          <span class="stat-value">{{ getContentLength(article.content) }}</span>
          <span class="stat-label">字数</span>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="article-actions">
        <button 
          @click="toggleLike" 
          :class="['like-btn', { 'liked': isLiked }]"
        >
          <span class="like-icon">{{ isLiked ? '❤️' : '🤍' }}</span>
          {{ isLiked ? '已点赞' : '点赞' }}
        </button>
        
        <button @click="shareArticle" class="share-btn">
          <span class="share-icon">📤</span>
          分享
        </button>
        
        <button v-if="canEdit" @click="editArticle" class="edit-btn">
          <span class="edit-icon">✏️</span>
          编辑
        </button>
      </div>
    </div>

    <!-- 错误状态 -->
    <div v-else class="error-state">
      <div class="error-icon">❌</div>
      <h3>文章不存在</h3>
      <p>抱歉，您访问的文章不存在或已被删除。</p>
      <button @click="goBack" class="back-btn">返回列表</button>
    </div>

    <!-- 图片查看器 -->
    <div v-if="showImageViewer" class="image-viewer" @click="closeImageViewer">
      <div class="viewer-content" @click.stop>
        <img :src="selectedImage.imageUrl" :alt="selectedImage.imageName" />
        <div class="viewer-caption">{{ selectedImage.imageName }}</div>
        <button @click="closeImageViewer" class="close-viewer-btn">×</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getArticleDetail, likeArticle, unlikeArticle } from '@/api/article.ts'
import MarkdownRenderer from '@/components/MarkdownRenderer.vue'
import defaultAvatar from '@/views/css/png/头像.png'

const route = useRoute()
const router = useRouter()

// 响应式数据
const article = ref(null)
const loading = ref(true)
const isLiked = ref(false)
const showImageViewer = ref(false)
const selectedImage = ref(null)

// 计算属性
const canEdit = computed(() => {
  // 这里可以根据用户权限和文章作者来判断是否可以编辑
  return article.value && article.value.authorId === getCurrentUserId()
})

// 渲染Markdown内容（带图片替换）
const contentWithImages = computed(() => {
  if (!article.value?.content) return '';
  let content = article.value.content;
  if (article.value.images && Array.isArray(article.value.images)) {
    content = content.replace(/!\[[^\]]*\]\(#image(\d+)\)/g, (match, idx) => {
      const img = article.value.images[Number(idx)];
      const src = img?.imageData || img?.imageUrl;
      return src ? `![图片](${src})` : match;
    });
  }
  return content;
});

// 获取文章详情
const fetchArticleDetail = async () => {
  loading.value = true
  try {
    const response = await getArticleDetail(route.params.id)
    article.value = response.data
    // 增加浏览次数
    await incrementViewCount()
  } catch (error) {
    console.error('获取文章详情失败:', error)
    article.value = null
  } finally {
    loading.value = false
  }
}

// 增加浏览次数
const incrementViewCount = async () => {
  // 这里可以调用API来增加浏览次数
  // 暂时在前端模拟
  if (article.value) {
    article.value.viewCount = (article.value.viewCount || 0) + 1
  }
}

// 切换点赞状态
const toggleLike = async () => {
  if (!article.value) return
  
  try {
    if (isLiked.value) {
      await unlikeArticle(article.value.id)
      article.value.likeCount = Math.max(0, article.value.likeCount - 1)
    } else {
      await likeArticle(article.value.id)
      article.value.likeCount = (article.value.likeCount || 0) + 1
    }
    isLiked.value = !isLiked.value
  } catch (error) {
    console.error('点赞操作失败:', error)
    alert('操作失败：' + (error.response?.data || '未知错误'))
  }
}

// 分享文章
const shareArticle = () => {
  if (navigator.share) {
    navigator.share({
      title: article.value.title,
      text: article.value.content.substring(0, 100) + '...',
      url: window.location.href
    })
  } else {
    // 复制链接到剪贴板
    navigator.clipboard.writeText(window.location.href).then(() => {
      alert('链接已复制到剪贴板')
    })
  }
}

// 编辑文章
const editArticle = () => {
  router.push(`/articles/edit/${article.value.id}`)
}

// 查看图片
const viewImage = (image) => {
  selectedImage.value = image
  showImageViewer.value = true
}

// 关闭图片查看器
const closeImageViewer = () => {
  showImageViewer.value = false
  selectedImage.value = null
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

// 获取内容长度
const getContentLength = (content) => {
  if (!content) return 0
  return content.replace(/<[^>]*>/g, '').length
}

// 获取当前用户ID
const getCurrentUserId = () => {
  // 这里应该从用户信息中获取
  return localStorage.getItem('userId')
}

// 返回列表
const goBack = () => {
  router.push('/articles')
}

// 页面加载时获取文章详情
onMounted(() => {
  fetchArticleDetail()
})

watch(() => article.value?.images, (val) => {
  console.log('Detail.vue images:', val)
}, { deep: true })
</script>

<style scoped>
.article-page {
  min-height: calc(100vh - 80px - 48px);
  background: var(--bg-color);
  padding-top: 80px;
  padding-bottom: 48px;
  padding-left: 32px;
  padding-right: 32px;
}
@media (max-width: 900px) {
  .article-page {
    padding-left: 12px;
    padding-right: 12px;
    padding-top: 64px;
    padding-bottom: 24px;
  }
}
.article-header, .article-title, .article-actions {
  margin-top: 24px;
}
.article-content, .el-pagination {
  margin-bottom: 32px;
}
.article-detail-container {
  min-height: 100vh;
  background: var(--bg-color);
  padding-top: 64px;
  position: relative;
}

.loading-state,
.error-state {
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

.error-icon {
  font-size: 4rem;
  margin-bottom: 16px;
}

.error-state h3 {
  font-size: 1.5rem;
  margin: 0 0 8px 0;
  color: var(--text-color);
}

.error-state p {
  margin: 0 0 24px 0;
  font-size: 1rem;
}

.back-btn {
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

.back-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(78,107,222,0.2);
}

.article-content {
  background: var(--card-bg);
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 4px 16px rgba(78,107,222,0.08);
  border: 1px solid var(--border-color);
}

.article-header {
  margin-bottom: 32px;
}

.article-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.article-status {
  padding: 4px 12px;
  border-radius: 6px;
  font-size: 0.9rem;
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

.article-date {
  font-size: 0.9rem;
  color: var(--tool-desc-color);
}

.article-title {
  font-size: 2.4rem;
  font-weight: 900;
  color: var(--text-color);
  margin: 0 0 24px 0;
  line-height: 1.3;
}

.article-author {
  display: flex;
  align-items: center;
  gap: 12px;
}

.author-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
}

.author-info {
  display: flex;
  flex-direction: column;
}

.author-name {
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--text-color);
}

.author-desc {
  font-size: 0.9rem;
  color: var(--tool-desc-color);
}

.article-cover {
  margin-bottom: 32px;
  border-radius: 12px;
  overflow: hidden;
}

.article-cover img {
  width: 100%;
  height: auto;
  max-height: 400px;
  object-fit: cover;
}

.article-body {
  margin-bottom: 32px;
}

.article-text {
  color: var(--text-color);
  line-height: 1.8;
  font-size: 1.1rem;
}

.article-text h1,
.article-text h2,
.article-text h3 {
  color: var(--tool-title-color);
  margin: 32px 0 16px 0;
}

.article-text p {
  margin: 16px 0;
}

.article-text img {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
  margin: 16px 0;
}

.article-text pre {
  background: var(--bg-color);
  padding: 16px;
  border-radius: 8px;
  overflow-x: auto;
  margin: 16px 0;
}

.article-text code {
  background: var(--bg-color);
  padding: 2px 6px;
  border-radius: 4px;
  font-family: monospace;
}

.article-stats {
  display: flex;
  justify-content: center;
  gap: 32px;
  margin-bottom: 32px;
  padding: 20px;
  background: var(--bg-color);
  border-radius: 12px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.stat-icon {
  font-size: 1.5rem;
}

.stat-value {
  font-size: 1.3rem;
  font-weight: 700;
  color: var(--tool-title-color);
}

.stat-label {
  font-size: 0.9rem;
  color: var(--tool-desc-color);
}

.article-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  flex-wrap: wrap;
}

.like-btn,
.share-btn,
.edit-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  border: none;
}

.like-btn {
  background: var(--card-bg);
  color: var(--text-color);
  border: 1px solid var(--border-color);
}

.like-btn.liked {
  background: #ef4444;
  color: white;
  border-color: #ef4444;
}

.like-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(239,68,68,0.2);
}

.share-btn {
  background: var(--card-bg);
  color: var(--text-color);
  border: 1px solid var(--border-color);
}

.share-btn:hover {
  background: var(--border-color);
}

.edit-btn {
  background: linear-gradient(90deg, var(--tool-title-color) 0%, #06b6d4 100%);
  color: white;
}

.edit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(78,107,222,0.2);
}

.image-viewer {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10000;
}

.viewer-content {
  position: relative;
  max-width: 90vw;
  max-height: 90vh;
}

.viewer-content img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

.viewer-caption {
  color: white;
  text-align: center;
  margin-top: 16px;
  font-size: 1rem;
}

.close-viewer-btn {
  position: absolute;
  top: -40px;
  right: 0;
  width: 32px;
  height: 32px;
  background: rgba(255,255,255,0.2);
  color: white;
  border: none;
  border-radius: 50%;
  cursor: pointer;
  font-size: 1.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-viewer-btn:hover {
  background: rgba(255,255,255,0.3);
}

@media (max-width: 768px) {
  .article-detail-container {
    padding: 16px 12px;
  }
  
  .article-content {
    padding: 20px;
  }
  
  .article-title {
    font-size: 1.8rem;
  }
  
  .article-stats {
    gap: 16px;
  }
  
  .article-actions {
    gap: 8px;
  }
  
  .like-btn,
  .share-btn,
  .edit-btn {
    padding: 8px 16px;
    font-size: 0.9rem;
  }
}
</style> 