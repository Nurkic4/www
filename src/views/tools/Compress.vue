<template>
  <div class="compress-container">
    <h2 class="page-title">批量压缩文件</h2>
    <!-- 上传区域 -->
    <div class="upload-section">
      <div 
        class="upload-area" 
        @dragover.prevent="dragover = true" 
        @dragleave.prevent="dragover = false" 
        @drop.prevent="onFileDrop"
        :class="{ 'dragover': dragover }"
      >
        <div class="upload-icon">
          <i class="el-icon-upload"></i>
        </div>
        <div class="upload-text">
          <p>拖拽文件或文件夹到此处</p>
          <div class="upload-buttons">
            <label class="upload-button">
              <span>上传文件</span>
              <input 
                type="file" 
                multiple 
                @change="onFileChange" 
                style="display: none;"
              >
            </label>
            <label class="upload-button folder-button">
              <span>上传文件夹</span>
              <input 
                type="file" 
                webkitdirectory 
                directory
                multiple 
                @change="onFolderChange" 
                style="display: none;"
              >
            </label>
          </div>
        </div>
      </div>
    </div>
    <!-- 文件列表 -->
    <div v-if="files.length > 0" class="files-section">
      <h3 class="section-title">待压缩文件和文件夹 ({{ files.length }})</h3>
      <div class="file-list">
        <div v-for="(file, index) in files" :key="index" class="file-item">
          <div class="file-icon">
            <i :class="getFileIcon(file)"></i>
          </div>
          <div class="file-info">
            <div class="file-name">{{ getDisplayName(file) }}</div>
            <div class="file-size">{{ formatFileSize(file.size) }}</div>
          </div>
          <div class="file-actions">
            <button class="delete-btn" @click="removeFile(index)" title="删除">×</button>
          </div>
        </div>
      </div>
    </div>
    <!-- 压缩选项 -->
    <div v-if="files.length > 0" class="options-section">
      <h3 class="section-title">压缩选项</h3>
      <div class="option-group">
        <div class="option-item">
          <label>压缩格式：</label>
          <select v-model="compressOptions.format">
            <option value="zip">ZIP (默认)</option>
          </select>
        </div>
        <div class="option-item">
          <label>压缩级别：</label>
          <select v-model="compressOptions.level">
            <option value="normal">普通 (默认)</option>
            <option value="fast">快速</option>
            <option value="best">最佳压缩</option>
          </select>
        </div>
        <div class="option-item">
          <label>压缩方式：</label>
          <div class="radio-group">
            <label>
              <input type="radio" v-model="compressOptions.mode" value="single">
              全部压缩到一个文件
            </label>
            <label>
              <input type="radio" v-model="compressOptions.mode" value="multiple">
              单独分别压缩
            </label>
          </div>
        </div>
      </div>
      <div class="compress-action">
        <button 
          class="compress-btn" 
          @click="startCompress" 
          :disabled="compressing"
        >
          <span v-if="compressing">
            <span class="loading-spinner"></span>
            压缩中 ({{ compressionProgress }}%)...
          </span>
          <span v-else>开始压缩</span>
        </button>
      </div>
    </div>
    <!-- 下载区域 -->
    <div v-if="compressedFiles.length > 0" class="download-section">
      <h3 class="section-title">压缩完成</h3>
      <div class="compressed-list">
        <div v-for="(file, index) in compressedFiles" :key="index" class="compressed-item">
          <div class="compressed-icon">
            <i class="el-icon-document-checked"></i>
          </div>
          <div class="compressed-info">
            <div class="compressed-name">{{ file.name }}</div>
            <div class="compressed-size">{{ formatFileSize(file.size) }}</div>
          </div>
          <div class="compressed-actions">
            <button class="download-btn" @click="downloadFile(file)">下载</button>
          </div>
        </div>
      </div>
      <div class="download-all">
        <button class="download-all-btn" @click="downloadAll">下载全部</button>
      </div>
    </div>
  </div>
</template>
<script setup>
  import { ref, reactive, computed } from 'vue';
  import JSZip from 'jszip';
  import { saveAs } from 'file-saver';
  
  // 状态变量
  const dragover = ref(false);
  const files = ref([]);
  const compressing = ref(false);
  const compressedFiles = ref([]);
  const compressionProgress = ref(0);
  
  // 压缩选项
  const compressOptions = reactive({
    format: 'zip',
    level: 'normal',
    mode: 'single'
  });
  
  // 获取JSZip压缩级别
  const compressionLevel = computed(() => {
    switch(compressOptions.level) {
      case 'fast': return 1;
      case 'best': return 9;
      default: return 5; // normal
    }
  });
  
  // 文件拖放处理
  function onFileDrop(e) {
    dragover.value = false;
    
    // 处理文件和文件夹
    if (e.dataTransfer.items) {
      // 使用 DataTransferItemList 接口处理文件夹
      handleDataTransferItems(e.dataTransfer.items);
    } else {
      // 回退到普通文件处理
      const droppedFiles = Array.from(e.dataTransfer.files);
      addFiles(droppedFiles);
    }
  }
  
  // 处理拖放的文件和文件夹
  async function handleDataTransferItems(items) {
    const entries = [];
    
    // 处理所有被拖放的项目
    for (let i = 0; i < items.length; i++) {
      const item = items[i];
      if (item.kind === 'file') {
        const entry = item.webkitGetAsEntry ? item.webkitGetAsEntry() : item.getAsEntry();
        if (entry) {
          entries.push(entry);
        }
      }
    }
    
    // 处理文件和文件夹
    for (const entry of entries) {
      if (entry.isFile) {
        const file = await getFileFromEntry(entry);
        if (file) addFiles([file]);
      } else if (entry.isDirectory) {
        // 获取文件夹信息
        const dirInfo = await getDirectoryInfo(entry);
        addFiles([dirInfo]);
      }
    }
  }
  
  // 获取文件夹信息
  async function getDirectoryInfo(dirEntry) {
    // 创建一个文件夹对象
    const dirInfo = {
      name: dirEntry.name,
      path: dirEntry.fullPath || dirEntry.name,
      isDirectory: true,
      size: 0,
      fileCount: 0,
      files: [] // 存储文件夹内的文件，用于后续压缩
    };
    
    // 读取文件夹内容以获取大小和文件数量
    const dirReader = dirEntry.createReader();
    
    const readEntries = async () => {
      return new Promise((resolve) => {
        dirReader.readEntries(async (entries) => {
          if (entries.length === 0) {
            resolve();
          } else {
            for (const entry of entries) {
              if (entry.isFile) {
                const file = await getFileFromEntry(entry);
                if (file) {
                  dirInfo.size += file.size;
                  dirInfo.fileCount++;
                  dirInfo.files.push(file);
                }
              } else if (entry.isDirectory) {
                const subDirInfo = await getDirectoryInfo(entry);
                dirInfo.size += subDirInfo.size;
                dirInfo.fileCount += subDirInfo.fileCount;
                dirInfo.files.push(...subDirInfo.files);
              }
            }
            await readEntries();
            resolve();
          }
        }, () => resolve());
      });
    };
    
    await readEntries();
    return dirInfo;
  }
  
  // 从文件系统条目获取文件
  function getFileFromEntry(entry) {
    return new Promise((resolve) => {
      entry.file(file => {
        // 添加路径信息
        file.path = entry.fullPath || entry.name;
        resolve(file);
      }, () => resolve(null));
    });
  }
  
  // 文件选择处理
  function onFileChange(e) {
    const selectedFiles = Array.from(e.target.files);
    addFiles(selectedFiles);
    // 重置input，允许重复选择相同文件
    e.target.value = '';
  }
  
  // 文件夹选择处理
  function onFolderChange(e) {
    const selectedFiles = Array.from(e.target.files);
    
    // 获取唯一的文件夹名称
    const folderPaths = selectedFiles.map(file => {
      const parts = file.webkitRelativePath.split('/');
      return parts[0]; // 第一部分是顶级文件夹名称
    });
    
    // 获取唯一的文件夹
    const uniqueFolders = [...new Set(folderPaths)];
    
    // 为每个文件夹创建一个对象
    uniqueFolders.forEach(folderName => {
      const folderFiles = selectedFiles.filter(file => 
        file.webkitRelativePath.startsWith(`${folderName}/`)
      );
      
      const totalSize = folderFiles.reduce((sum, file) => sum + file.size, 0);
      
      const folderInfo = {
        name: folderName,
        path: folderName,
        isDirectory: true,
        size: totalSize,
        fileCount: folderFiles.length,
        files: folderFiles
      };
      
      addFiles([folderInfo]);
    });
    
    e.target.value = '';
  }
  
  // 添加文件到列表
  function addFiles(newFiles) {
    files.value = [...files.value, ...newFiles];
  }
  
  // 移除文件
  function removeFile(index) {
    files.value.splice(index, 1);
  }
  
  // 获取文件图标
  function getFileIcon(file) {
    // 检查是否是文件夹
    if (file.isDirectory) {
      return 'el-icon-folder';
    }
    
    // 根据文件类型返回不同图标
    const extension = file.name.split('.').pop()?.toLowerCase();
    
    if (['jpg', 'jpeg', 'png', 'gif', 'bmp', 'svg'].includes(extension)) {
      return 'el-icon-picture';
    } else if (['doc', 'docx', 'txt', 'pdf'].includes(extension)) {
      return 'el-icon-document';
    } else if (['xls', 'xlsx', 'csv'].includes(extension)) {
      return 'el-icon-s-grid';
    } else if (['zip', 'rar', '7z', 'tar', 'gz'].includes(extension)) {
      return 'el-icon-box';
    } else {
      return 'el-icon-document';
    }
  }
  
  // 获取显示名称
  function getDisplayName(file) {
    if (file.isDirectory) {
      return `${file.name} (${file.fileCount} 个文件)`;
    }
    return file.path || file.name;
  }
  
  // 格式化文件大小
  function formatFileSize(bytes) {
    if (bytes === 0) return '0 Bytes';
    
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  }
  
  // 开始压缩
  async function startCompress() {
    if (files.value.length === 0) return;
    
    compressing.value = true;
    compressionProgress.value = 0;
    
    try {
      // 生成日期时间格式的文件名
      const now = new Date();
      const dateStr = now.toISOString().split('T')[0]; // YYYY-MM-DD
      const timeStr = now.toTimeString().split(' ')[0].replace(/:/g, '-'); // HH-MM-SS
      const timestamp = `${dateStr}_${timeStr}`;
      
      // 清空之前的压缩文件
      compressedFiles.value = [];
      
      if (compressOptions.mode === 'single') {
        // 创建单个压缩文件
        await createSingleZip(timestamp);
      } else {
        // 创建多个压缩文件
        await createMultipleZips(timestamp);
      }
    } catch (error) {
      console.error('压缩过程中出错:', error);
      alert('压缩过程中出错: ' + error.message);
    } finally {
      compressing.value = false;
      compressionProgress.value = 100;
    }
  }
  
  // 创建单个压缩文件
  async function createSingleZip(timestamp) {
    const zip = new JSZip();
    let processedItems = 0;
    const totalItems = getTotalFileCount();
    
    // 处理所有文件和文件夹
    for (const item of files.value) {
      if (item.isDirectory) {
        // 添加文件夹中的所有文件
        for (const file of item.files) {
          const relativePath = file.webkitRelativePath || `${item.name}/${file.name}`;
          zip.file(relativePath, file);
          processedItems++;
          updateProgress(processedItems, totalItems);
        }
      } else {
        // 添加单个文件
        zip.file(item.name, item);
        processedItems++;
        updateProgress(processedItems, totalItems);
      }
    }
    
    // 生成并保存zip文件
    const content = await zip.generateAsync({
      type: 'blob',
      compression: 'DEFLATE',
      compressionOptions: {
        level: compressionLevel.value
      },
      onUpdate: (metadata) => {
        if (metadata.percent) {
          compressionProgress.value = Math.round(metadata.percent);
        }
      }
    });
    
    const zipName = `compressed_${timestamp}.zip`;
    
    // 添加到已压缩文件列表
    compressedFiles.value.push({
      name: zipName,
      size: content.size,
      blob: content
    });
  }
  
  // 创建多个压缩文件
  async function createMultipleZips(timestamp) {
    let processedItems = 0;
    const totalItems = files.value.length;
    
    // 为每个文件或文件夹创建单独的zip
    for (let i = 0; i < files.value.length; i++) {
      const item = files.value[i];
      const zip = new JSZip();
      
      if (item.isDirectory) {
        // 添加文件夹中的所有文件
        for (const file of item.files) {
          // 从webkitRelativePath中提取相对路径
          const relativePath = file.webkitRelativePath || file.name;
          const pathParts = relativePath.split('/');
          // 移除顶级文件夹名称，保留内部结构
          const innerPath = pathParts.slice(1).join('/');
          zip.file(innerPath, file);
        }
      } else {
        // 添加单个文件
        zip.file(item.name, item);
      }
      
      // 生成并保存zip文件
      const content = await zip.generateAsync({
        type: 'blob',
        compression: 'DEFLATE',
        compressionOptions: {
          level: compressionLevel.value
        },
        onUpdate: (metadata) => {
          if (metadata.percent) {
            // 计算总体进度
            const itemProgress = metadata.percent / 100;
            const overallProgress = ((processedItems + itemProgress) / totalItems) * 100;
            compressionProgress.value = Math.round(overallProgress);
          }
        }
      });
      
      const zipName = `${item.name}_${timestamp}.zip`;
      
      // 添加到已压缩文件列表
      compressedFiles.value.push({
        name: zipName,
        size: content.size,
        blob: content
      });
      
      processedItems++;
      updateProgress(processedItems, totalItems);
    }
  }
  
  // 更新进度
  function updateProgress(current, total) {
    compressionProgress.value = Math.round((current / total) * 100);
  }
  
  // 获取总文件数
  function getTotalFileCount() {
    return files.value.reduce((count, item) => {
      if (item.isDirectory) {
        return count + item.fileCount;
      }
      return count + 1;
    }, 0);
  }
  
  // 下载单个文件
  function downloadFile(file) {
    if (file.blob) {
      saveAs(file.blob, file.name);
    }
  }
  
  // 下载所有文件
  function downloadAll() {
    compressedFiles.value.forEach(file => {
      downloadFile(file);
    });
  }
  </script>
<style scoped>
:global(body), :global(#app) {
  background: var(--bg-color) !important;
  min-height: 100vh;
  width: 100vw;
  margin: 0;
  padding: 0;
}
.compress-container {
  max-width: 900px;
  margin: 60px auto 0 auto;
  width: 100%;
  min-height: calc(100vh - 100px);
  color: var(--text-color);
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 48px 0 40px 0;
}
.page-title {
  font-size: 2.6rem;
  font-weight: 900;
  margin-bottom: 38px;
  color: var(--tool-title-color);
  text-align: center;
  letter-spacing: 2px;
}
.section-title {
  font-size: 1.4rem;
  font-weight: 700;
  margin: 32px 0 18px;
  color: var(--tool-title-color);
  padding-left: 16px;
  border-left: 5px solid var(--tool-title-color);
  background: none;
  -webkit-background-clip: initial;
  color: var(--tool-title-color);
  -webkit-text-fill-color: initial;
}
.upload-section,
.files-section,
.options-section,
.download-section {
  width: 100%;
  max-width: 800px;
  margin: 0 auto 36px auto;
}
.upload-area {
  width: 100%;
  border: 2.5px dashed var(--tool-title-color);
  border-radius: 22px;
  padding: 56px 0 40px 0;
  text-align: center;
  background: var(--bg-color);
  transition: box-shadow 0.3s, border-color 0.3s, background 0.3s;
  cursor: pointer;
  box-shadow: 0 4px 24px rgba(59,130,246,0.08);
  font-size: 1.2rem;
  color: var(--text-color);
  margin: 0 auto;
}
.upload-area.dragover {
  border-color: #06b6d4;
  background: var(--border-color);
  box-shadow: 0 0 0 8px #bae6fd;
}
.upload-buttons {
  display: flex;
  justify-content: center;
  gap: 36px;
  margin-top: 22px;
}
.upload-button {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  color: #fff;
  font-weight: 700;
  font-size: 1.15rem;
  padding: 16px 38px;
  border: none;
  border-radius: 16px;
  background: linear-gradient(90deg, var(--tool-title-color) 0%, #06b6d4 100%);
  transition: all 0.3s;
  box-shadow: 0 2px 12px rgba(59,130,246,0.10);
  cursor: pointer;
}
.upload-button:hover {
  background: linear-gradient(90deg, #06b6d4 0%, var(--tool-title-color) 100%);
  transform: translateY(-2px) scale(1.06);
}
.file-list,
.compressed-list {
  border: 1.5px solid var(--border-color);
  border-radius: 18px;
  background: var(--card-bg);
  overflow: hidden;
  font-size: 1.1rem;
  width: 100%;
  margin: 0 auto;
  box-shadow: 0 2px 12px rgba(59,130,246,0.06);
}
.file-item,
.compressed-item {
  display: flex;
  align-items: center;
  padding: 22px 32px;
  border-bottom: 1px solid var(--border-color);
  transition: background 0.2s;
  color: var(--text-color);
}
.file-item:last-child,
.compressed-item:last-child {
  border-bottom: none;
}
.file-item:hover,
.compressed-item:hover {
  background: var(--border-color);
}
.file-icon,
.compressed-icon {
  font-size: 2.1rem;
  margin-right: 24px;
  color: var(--tool-title-color);
}
.file-info,
.compressed-info {
  flex: 1;
}
.file-name,
.compressed-name {
  font-size: 1.13rem;
  color: var(--text-color);
  margin-bottom: 6px;
  font-weight: 600;
  word-break: break-all;
}
.file-size,
.compressed-size {
  font-size: 1rem;
  color: var(--tool-desc-color);
}
.file-actions,
.compressed-actions {
  margin-left: 18px;
}
.delete-btn,
.download-btn,
.download-all-btn,
.compress-btn {
  border: none;
  border-radius: 12px;
  font-size: 1.13rem;
  font-weight: 700;
  padding: 12px 32px;
  background: linear-gradient(90deg, var(--tool-title-color) 0%, #06b6d4 100%);
  color: #fff;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 10px rgba(59,130,246,0.10);
}
.delete-btn {
  padding: 0 18px;
  font-size: 1.5rem;
  background: linear-gradient(135deg, #ef4444 0%, #f59e42 100%);
  min-width: 44px;
  min-height: 44px;
}
.delete-btn:hover {
  background: linear-gradient(135deg, #dc2626 0%, #f59e42 100%);
  transform: scale(1.13);
}
.download-btn:hover,
.download-all-btn:hover,
.compress-btn:hover {
  background: linear-gradient(90deg, #06b6d4 0%, var(--tool-title-color) 100%);
  transform: translateY(-2px) scale(1.06);
}
.download-all {
  text-align: center;
  margin-top: 32px;
}
.download-all-btn {
  font-size: 1.2rem;
  padding: 16px 44px;
}
input[type='file'],
input[type='radio'],
input[type='checkbox'] {
  accent-color: var(--tool-title-color);
  font-size: 1.1rem;
}
.options-section select,
.options-section input[type='text'] {
  font-size: 1.1rem;
  padding: 12px 20px;
  border-radius: 12px;
  border: 1.5px solid var(--border-color);
  background: var(--bg-color);
  margin-left: 8px;
  margin-right: 8px;
  color: var(--text-color);
}
.options-section select:focus,
.options-section input[type='text']:focus {
  border: 1.5px solid var(--tool-title-color);
  outline: none;
}
@media (max-width: 900px) {
  .compress-container {
    max-width: 100vw;
    border-radius: 0;
    padding: 12px 0 24px 0;
  }
  .upload-section,
  .files-section,
  .options-section,
  .download-section {
    max-width: 99vw;
    padding: 0 2vw;
  }
  .upload-area,
  .file-list,
  .compressed-list {
    max-width: 99vw;
    width: 100%;
  }
}
</style> 