<template>
  <div class="markdown-editor">
    <div class="editor-toolbar">
      <input type="file" ref="imageInput" accept="image/*" style="display:none" @change="handleImageUpload" />
      <button @click="triggerImageUpload" class="toolbar-btn" title="插入图片">🖼️</button>
    </div>
    
    <div class="editor-content">
      <textarea 
        :value="modelValue"
        @input="$emit('update:modelValue', $event.target.value)"
        placeholder="请输入文章内容，支持Markdown格式..."
        class="markdown-textarea"
        ref="textareaRef"
      ></textarea>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, computed } from 'vue'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  images: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue', 'update:images'])
const textareaRef = ref(null)
const imageInput = ref(null)

// 删除 computed images

const triggerImageUpload = () => {
  imageInput.value?.click()
}

const handleImageUpload = (event) => {
  const file = event.target.files[0]
  if (!file) return
  const reader = new FileReader()
  reader.onload = (e) => {
    insertImage(e.target.result, file.name)
  }
  reader.readAsDataURL(file)
}

const insertImage = (base64, fileName = '插图.png') => {
  // 下标应为插入前 images 的长度
  const idx = props.images.length
  const newImages = [
    ...props.images,
    {
      imageData: base64,
      imageName: fileName,
      sortOrder: idx,
      imageUrl: ''
    }
  ]
  emit('update:images', newImages)
  const textarea = textareaRef.value
  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const value = props.modelValue
  // markdown 占位符用 #image{idx}，前后加换行，保证图片单独一行
  const markdown = `\n![图片](#image${idx})\n`
  const newValue = value.substring(0, start) + markdown + value.substring(end)
  emit('update:modelValue', newValue)
  nextTick(() => {
    textarea.focus()
    textarea.setSelectionRange(start + markdown.length, start + markdown.length)
  })
}
</script>

<style scoped>
.markdown-editor {
  border: 1px solid var(--border-color);
  border-radius: 8px;
  overflow: hidden;
  background: var(--bg-color);
}

.editor-toolbar {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  background: var(--card-bg);
  border-bottom: 1px solid var(--border-color);
  flex-wrap: wrap;
  gap: 4px;
}

.toolbar-btn {
  padding: 6px 10px;
  border: 1px solid var(--border-color);
  background: var(--bg-color);
  color: var(--text-color);
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s;
  min-width: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.toolbar-btn:hover {
  background: var(--primary-color);
  color: white;
  border-color: var(--primary-color);
}

.toolbar-divider {
  width: 1px;
  height: 20px;
  background: var(--border-color);
  margin: 0 4px;
}

.editor-content {
  position: relative;
}

.markdown-textarea {
  width: 100%;
  min-height: 400px;
  padding: 16px;
  border: none;
  outline: none;
  resize: vertical;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 14px;
  line-height: 1.6;
  background: var(--bg-color);
  color: var(--text-color);
}

.markdown-textarea::placeholder {
  color: var(--text-secondary);
}

@media (max-width: 768px) {
  .editor-toolbar {
    padding: 6px 8px;
  }
  
  .toolbar-btn {
    padding: 4px 8px;
    font-size: 12px;
    min-width: 28px;
  }
  
  .markdown-textarea {
    min-height: 300px;
    padding: 12px;
    font-size: 13px;
  }
}
</style> 