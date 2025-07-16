<template>
  <div class="markdown-content" v-html="renderedContent"></div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  content: {
    type: String,
    default: ''
  },
  images: {
    type: Array,
    default: () => []
  }
})

const renderedContent = computed(() => {
  console.log('MarkdownRenderer content:', props.content)
  console.log('MarkdownRenderer images:', props.images)
  if (!props.content) return ''
  let content = props.content

  // 先处理图片占位符，直接生成 <img> 标签
  content = content.replace(/!\[[^\]]*\]\(#image(\d+)\)/g, (match, idx) => {
    const img = props.images[Number(idx)]
    const src = img?.imageData || img?.imageUrl
    return src ? `<img src="${src}" alt="图片" class="markdown-image" />` : match
  })

  // 标题
  content = content.replace(/^### (.*$)/gim, '<h3>$1</h3>')
  content = content.replace(/^## (.*$)/gim, '<h2>$1</h2>')
  content = content.replace(/^# (.*$)/gim, '<h1>$1</h1>')

  // 粗体和斜体
  content = content.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
  content = content.replace(/\*(.*?)\*/g, '<em>$1</em>')
  content = content.replace(/~~(.*?)~~/g, '<del>$1</del>')

  // 行内代码
  content = content.replace(/`([^`]+)`/g, '<code class="inline-code">$1</code>')

  // 代码块
  content = content.replace(/```([\s\S]*?)```/g, '<pre><code class="code-block">$1</code></pre>')

  // 链接
  content = content.replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" target="_blank" rel="noopener">$1</a>')

  // 图片（提前到段落前）
  content = content.replace(/!\[([^\]]*)\]\(([^)]+)\)/g, '<img src="$2" alt="$1" class="markdown-image" />')

  // 引用
  content = content.replace(/^> (.*$)/gim, '<blockquote>$1</blockquote>')

  // 列表
  content = content.replace(/^- (.*$)/gim, '<li>$1</li>')
  content = content.replace(/^(\d+)\. (.*$)/gim, '<li>$2</li>')

  // 将连续的li包装成ul或ol
  content = content.replace(/(<li>.*<\/li>)/gs, '<ul>$1</ul>')

  // 分割线
  content = content.replace(/^---$/gim, '<hr>')

  // 表格
  content = content.replace(/\|(.+)\|/g, (match, cells) => {
    const cellArray = cells.split('|').map(cell => cell.trim())
    return `<tr>${cellArray.map(cell => `<td>${cell}</td>`).join('')}</tr>`
  })
  content = content.replace(/(<tr>.*<\/tr>)/gs, '<table>$1</table>')

  // 段落（避免包裹HTML标签）
  content = content.replace(/^(.+)$/gm, (match) => {
    if (/^\s*<.*>\s*$/.test(match.trim())) return match;
    return `<p>${match}</p>`;
  })

  // 清理空段落
  content = content.replace(/<p><\/p>/g, '')

  return content
})
</script>

<style scoped>
.markdown-content {
  line-height: 1.8;
  color: var(--text-color);
}

.markdown-content :deep(h1) {
  font-size: 2em;
  font-weight: bold;
  margin: 1.5em 0 1em 0;
  color: var(--text-color);
  border-bottom: 2px solid var(--primary-color);
  padding-bottom: 0.3em;
}

.markdown-content :deep(h2) {
  font-size: 1.5em;
  font-weight: bold;
  margin: 1.3em 0 0.8em 0;
  color: var(--text-color);
  border-bottom: 1px solid var(--border-color);
  padding-bottom: 0.2em;
}

.markdown-content :deep(h3) {
  font-size: 1.3em;
  font-weight: bold;
  margin: 1.2em 0 0.6em 0;
  color: var(--text-color);
}

.markdown-content :deep(p) {
  margin: 0.8em 0;
  line-height: 1.8;
}

.markdown-content :deep(strong) {
  font-weight: bold;
  color: var(--text-color);
}

.markdown-content :deep(em) {
  font-style: italic;
  color: var(--text-color);
}

.markdown-content :deep(del) {
  text-decoration: line-through;
  color: var(--text-secondary);
}

.markdown-content :deep(code.inline-code) {
  background: var(--code-bg);
  color: var(--code-color);
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 0.9em;
}

.markdown-content :deep(pre) {
  background: var(--code-bg);
  border-radius: 8px;
  padding: 16px;
  margin: 1em 0;
  overflow-x: auto;
  border: 1px solid var(--border-color);
}

.markdown-content :deep(code.code-block) {
  color: var(--code-color);
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 0.9em;
  line-height: 1.5;
  white-space: pre-wrap;
}

.markdown-content :deep(a) {
  color: var(--primary-color);
  text-decoration: none;
  border-bottom: 1px solid transparent;
  transition: border-color 0.2s;
}

.markdown-content :deep(a:hover) {
  border-bottom-color: var(--primary-color);
}

.markdown-content :deep(img.markdown-image) {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
  margin: 1em 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.markdown-content :deep(blockquote) {
  border-left: 4px solid var(--primary-color);
  padding-left: 1em;
  margin: 1em 0;
  color: var(--text-secondary);
  font-style: italic;
  background: var(--quote-bg);
  padding: 1em;
  border-radius: 0 8px 8px 0;
}

.markdown-content :deep(ul), .markdown-content :deep(ol) {
  margin: 1em 0;
  padding-left: 2em;
}

.markdown-content :deep(li) {
  margin: 0.5em 0;
  line-height: 1.6;
}

.markdown-content :deep(hr) {
  border: none;
  height: 1px;
  background: var(--border-color);
  margin: 2em 0;
}

.markdown-content :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 1em 0;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.markdown-content :deep(th), .markdown-content :deep(td) {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid var(--border-color);
}

.markdown-content :deep(th) {
  background: var(--card-bg);
  font-weight: bold;
  color: var(--text-color);
}

.markdown-content :deep(td) {
  background: var(--bg-color);
}

.markdown-content :deep(tr:hover td) {
  background: var(--hover-bg);
}

@media (max-width: 768px) {
  .markdown-content :deep(h1) {
    font-size: 1.8em;
  }
  
  .markdown-content :deep(h2) {
    font-size: 1.4em;
  }
  
  .markdown-content :deep(h3) {
    font-size: 1.2em;
  }
  
  .markdown-content :deep(pre) {
    padding: 12px;
    font-size: 0.85em;
  }
  
  .markdown-content :deep(table) {
    font-size: 0.9em;
  }
  
  .markdown-content :deep(th), .markdown-content :deep(td) {
    padding: 8px;
  }
}
</style> 