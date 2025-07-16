<template>
  <div class="article-create-container">
    <div class="create-header">
      <h1 class="page-title">
        <span class="icon-glow">✍️</span> 创建文章
      </h1>
      <div class="action-buttons">
        <button @click="saveDraft" :disabled="saving" class="action-btn draft-btn">
          <span v-if="saving">保存中...</span>
          <span v-else>保存草稿</span>
        </button>
        <button @click="submitArticle" :disabled="saving || !canSubmit" class="action-btn submit-btn">
          <span v-if="saving">提交中...</span>
          <span v-else>提交审核</span>
        </button>
      </div>
    </div>

    <div class="create-form">
      <!-- 文章标题 -->
      <div class="form-group">
        <label class="form-label">文章标题 *</label>
        <input 
          v-model="titleField.value" 
          type="text" 
          placeholder="请输入文章标题..."
          class="form-input title-input"
          maxlength="200"
        />
        <div class="char-count">{{ titleField.value.length }}/200</div>
        <span class="error-message">{{ errors.title }}</span>
      </div>

      <!-- 封面图片 -->
      <div class="form-group">
        <label class="form-label">封面图片</label>
        <div class="cover-upload">
          <div v-if="values.coverImage" class="cover-preview">
            <img :src="values.coverImage" alt="封面" />
            <button @click="removeCover" class="remove-btn">×</button>
          </div>
          <div v-else class="upload-area" @click="triggerCoverUpload">
            <div class="upload-icon">📷</div>
            <div class="upload-text">点击上传封面图片</div>
            <input 
              ref="coverInput" 
              type="file" 
              accept="image/*" 
              @change="handleCoverUpload" 
              style="display: none;"
            />
          </div>
        </div>
        <span class="error-message">{{ errors.coverImage }}</span>
      </div>

      <!-- 文章内容 -->
      <div class="form-group">
        <label class="form-label">文章内容 *</label>
        <MarkdownEditor 
          v-model="contentField.value"
          v-model:images="values.images"
          @image-upload="handleImageUpload"
        />
        <div class="char-count">{{ contentField.value.length }}/10000</div>
        <span class="error-message">{{ errors.content }}</span>
      </div>

      <!-- 插图列表 -->
      <div v-if="values.images.length > 0" class="form-group">
        <label class="form-label">插图列表</label>
        <div class="images-list">
          <div 
            v-for="(image, index) in values.images" 
            :key="index" 
            class="image-item"
          >
            <img :src="image.imageUrl" :alt="image.imageName" />
            <div class="image-info">
              <input 
                v-model="image.imageName" 
                placeholder="图片名称"
                class="image-name-input"
              />
              <button @click="removeImage(index)" class="remove-image-btn">删除</button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 预览区域 -->
    <div v-if="showPreview" class="preview-section">
      <h3 class="preview-title">文章预览</h3>
      <MarkdownRenderer :content="renderedContent" :images="values.images" />
    </div>

    <!-- 预览切换按钮 -->
    <div class="preview-toggle">
      <button @click="togglePreview" class="preview-btn">
        {{ showPreview ? '隐藏预览' : '显示预览' }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { createArticle } from '@/api/article.ts'
import MarkdownEditor from '@/components/MarkdownEditor.vue'
import MarkdownRenderer from '@/components/MarkdownRenderer.vue'
// vee-validate 集成
import { useForm, useField } from 'vee-validate'
import * as yup from 'yup'

const router = useRouter()

// 表单校验规则
const articleSchema = yup.object({
  title: yup.string().required('请输入文章标题').max(200, '标题不能超过200字'),
  content: yup.string().required('请输入文章内容').max(10000, '内容不能超过10000字'),
  coverImage: yup.string().nullable(),
  images: yup.array().default([])
})
const { handleSubmit, errors, values, setValues, resetForm } = useForm({
  validationSchema: articleSchema,
  initialValues: { title: '', content: '', coverImage: '', images: [] }
})
const titleField = useField('title')
const contentField = useField('content')
const coverImageField = useField('coverImage')
const imagesField = useField('images')

const saving = ref(false)
const showPreview = ref(false)
const coverInput = ref(null)
const imageInput = ref(null)

const canSubmit = computed(() => {
  return values.title.trim() && values.content.trim()
})

const renderedContent = computed(() => {
  if (!values.content) return ''
  let content = values.content
  content = content.replace(/!\[图片\]\(#image(\d+)\)/g, (m, idx) => {
    const src = values.images[Number(idx)]?.imageUrl
    return src ? `<img src='${src}' alt='图片' style='max-width:100%;height:auto;' />` : m
  })
  content = content
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.*?)\*/g, '<em>$1</em>')
    .replace(/### (.*?)$/gm, '<h3>$1</h3>')
    .replace(/## (.*?)$/gm, '<h2>$1</h2>')
    .replace(/# (.*?)$/gm, '<h1>$1</h1>')
    .replace(/\[(.*?)\]\((.*?)\)/g, '<a href="$2" target="_blank">$1</a>')
    .replace(/!\[(.*?)\]\((.*?)\)/g, '<img src="$2" alt="$1" style="max-width: 100%; height: auto;" />')
    .replace(/```([\s\S]*?)```/g, '<pre><code>$1</code></pre>')
    .replace(/- (.*?)$/gm, '<li>$1</li>')
    .replace(/\n/g, '<br>')
  return content
})

const saveDraft = handleSubmit(async (formValues) => {
  saving.value = true
  try {
    await createArticle({
      ...formValues,
      status: 'DRAFT'
    })
    alert('草稿保存成功！')
    router.push('/articles/my')
  } catch (error) {
    alert('保存失败：' + (error.response?.data || '未知错误'))
  } finally {
    saving.value = false
  }
})

const submitArticle = handleSubmit(async (formValues) => {
  saving.value = true
  try {
    await createArticle({
      ...formValues,
      status: 'PENDING'
    })
    alert('文章已提交审核！')
    router.push('/articles/my')
  } catch (error) {
    alert('提交失败：' + (error.response?.data || '未知错误'))
  } finally {
    saving.value = false
  }
})

const triggerCoverUpload = () => {
  coverInput.value?.click()
}
const handleCoverUpload = (event) => {
  const file = event.target.files[0]
  if (!file) return
  const reader = new FileReader()
  reader.onload = (e) => {
    values.coverImage = e.target.result
  }
  reader.readAsDataURL(file)
}
const removeCover = () => {
  values.coverImage = ''
}

const triggerImageUpload = () => {
  imageInput.value?.click()
}

const handleImageUpload = async (event) => {
  const file = event.target.files[0]
  if (!file) return
  const reader = new FileReader()
  reader.onload = (e) => {
    values.images.push({
      imageData: e.target.result,
      imageName: file.name,
      sortOrder: values.images.length
    })
  }
  reader.readAsDataURL(file)
}

const removeImage = (index) => {
  values.images.splice(index, 1)
}

const insertText = (text) => {
  const textarea = document.querySelector('.content-editor')
  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const content = values.content
  
  values.content = content.substring(0, start) + text + content.substring(end)
  
  // 设置光标位置
  setTimeout(() => {
    textarea.focus()
    textarea.setSelectionRange(start + text.length, start + text.length)
  }, 0)
}

const togglePreview = () => {
  showPreview.value = !showPreview.value
}

// 保证 titleField.value 始终为字符串，防止 [object Object] 问题
watch(
  () => titleField.value,
  (val) => {
    if (typeof val !== 'string') titleField.value = ''
  },
  { immediate: true }
)

// 页面加载时初始化
// onMounted(() => {
//   // 可以在这里加载草稿数据
// })

// watch(() => articleForm.value.images, (val) => {
//   console.log('Create.vue images:', val)
// }, { deep: true })
</script>

<style scoped>
.article-page {
  padding-top: 64px;
}
.article-create-container {
  min-height: 100vh;
  background: var(--bg-color);
  padding-top: 96px;
  padding-bottom: 48px;
  position: relative;
}

.create-header {
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

.action-buttons {
  display: flex;
  gap: 12px;
}

.action-btn {
  padding: 12px 24px;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  border: none;
}

.draft-btn {
  background: var(--card-bg);
  color: var(--text-color);
  border: 1px solid var(--border-color);
}

.draft-btn:hover:not(:disabled) {
  background: var(--border-color);
}

.submit-btn {
  background: linear-gradient(90deg, var(--tool-title-color) 0%, #06b6d4 100%);
  color: white;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(78,107,222,0.2);
}

.action-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.create-form {
  background: var(--card-bg);
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 4px 16px rgba(78,107,222,0.08);
  border: 1px solid var(--border-color);
}

.form-group {
  margin-bottom: 24px;
}

.form-label {
  display: block;
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--text-color);
  margin-bottom: 8px;
}

.form-input {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  background: var(--bg-color);
  color: var(--text-color);
  font-size: 1rem;
  transition: border-color 0.2s;
}

.form-input:focus {
  outline: none;
  border-color: var(--tool-title-color);
}

.title-input {
  font-size: 1.2rem;
  font-weight: 600;
}

.char-count {
  text-align: right;
  font-size: 0.9rem;
  color: var(--tool-desc-color);
  margin-top: 4px;
}

.cover-upload {
  margin-top: 8px;
}

.cover-preview {
  position: relative;
  width: 200px;
  height: 120px;
  border-radius: 8px;
  overflow: hidden;
}

.cover-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.remove-btn {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 24px;
  height: 24px;
  background: rgba(0,0,0,0.6);
  color: white;
  border: none;
  border-radius: 50%;
  cursor: pointer;
  font-size: 1rem;
  display: flex;
  align-items: center;
  justify-content: center;
}

.upload-area {
  width: 200px;
  height: 120px;
  border: 2px dashed var(--border-color);
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: border-color 0.2s;
}

.upload-area:hover {
  border-color: var(--tool-title-color);
}

.upload-icon {
  font-size: 2rem;
  margin-bottom: 8px;
}

.upload-text {
  font-size: 0.9rem;
  color: var(--tool-desc-color);
}

.editor-container {
  border: 1px solid var(--border-color);
  border-radius: 8px;
  overflow: hidden;
}

.editor-toolbar {
  display: flex;
  gap: 4px;
  padding: 8px;
  background: var(--bg-color);
  border-bottom: 1px solid var(--border-color);
  flex-wrap: wrap;
}

.toolbar-btn {
  padding: 6px 12px;
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.2s;
}

.toolbar-btn:hover {
  background: var(--border-color);
}

.content-editor {
  width: 100%;
  min-height: 400px;
  padding: 16px;
  border: none;
  background: var(--card-bg);
  color: var(--text-color);
  font-size: 1rem;
  line-height: 1.6;
  resize: vertical;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
}

.content-editor:focus {
  outline: none;
}

.images-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
  margin-top: 8px;
}

.image-item {
  border: 1px solid var(--border-color);
  border-radius: 8px;
  overflow: hidden;
  background: var(--bg-color);
}

.image-item img {
  width: 100%;
  height: 120px;
  object-fit: cover;
}

.image-info {
  padding: 8px;
}

.image-name-input {
  width: 100%;
  padding: 4px 8px;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  background: var(--card-bg);
  color: var(--text-color);
  font-size: 0.9rem;
  margin-bottom: 4px;
}

.remove-image-btn {
  width: 100%;
  padding: 4px 8px;
  background: #ef4444;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.8rem;
}

.preview-section {
  margin-top: 32px;
  background: var(--card-bg);
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 4px 16px rgba(78,107,222,0.08);
  border: 1px solid var(--border-color);
}

.preview-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--text-color);
  margin: 0 0 24px 0;
}

.preview-content {
  color: var(--text-color);
  line-height: 1.8;
  font-size: 1rem;
}

.preview-content h1,
.preview-content h2,
.preview-content h3 {
  color: var(--tool-title-color);
  margin: 24px 0 12px 0;
}

.preview-content p {
  margin: 12px 0;
}

.preview-content img {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
  margin: 12px 0;
}

.preview-content pre {
  background: var(--bg-color);
  padding: 12px;
  border-radius: 6px;
  overflow-x: auto;
  margin: 12px 0;
}

.preview-content code {
  background: var(--bg-color);
  padding: 2px 6px;
  border-radius: 4px;
  font-family: monospace;
}

.preview-toggle {
  text-align: center;
  margin-top: 24px;
}

.preview-btn {
  padding: 8px 16px;
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  color: var(--text-color);
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.2s;
}

.preview-btn:hover {
  background: var(--border-color);
}

.error-message {
  display: block;
  color: #ef4444;
  font-size: 0.8rem;
  margin-top: 4px;
}

@media (max-width: 768px) {
  .article-create-container {
    padding: 16px 12px;
  }
  
  .create-header {
    flex-direction: column;
    align-items: stretch;
  }
  
  .action-buttons {
    justify-content: center;
  }
  
  .create-form {
    padding: 20px;
  }
  
  .editor-toolbar {
    gap: 2px;
  }
  
  .toolbar-btn {
    padding: 4px 8px;
    font-size: 0.8rem;
  }
  
  .images-list {
    grid-template-columns: 1fr;
  }
}
</style> 