<template>
  <div class="unzip-container">
    <h2 class="page-title">批量解压文件</h2>
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
          <p>拖拽ZIP文件到此处</p>
          <div class="upload-buttons">
            <label class="upload-button">
              <span>上传ZIP文件</span>
              <input 
                type="file" 
                multiple 
                accept=".zip"
                @change="onFileChange" 
                style="display: none;"
              >
            </label>
          </div>
        </div>
      </div>
    </div>
    <!-- 文件列表 -->
    <div v-if="files.length > 0" class="files-section">
      <h3 class="section-title">待解压文件 ({{ files.length }})</h3>
      <div class="file-list">
        <div v-for="(file, index) in files" :key="index" class="file-item">
          <div class="file-icon">
            <i class="el-icon-box"></i>
          </div>
          <div class="file-info">
            <div class="file-name">{{ file.name }}</div>
            <div class="file-size">{{ formatFileSize(file.size) }}</div>
          </div>
          <div class="file-actions">
            <button class="delete-btn" @click="removeFile(index)" title="删除">×</button>
          </div>
        </div>
      </div>
    </div>
    <!-- 解压选项（已移除与 unzipOptions 相关的部分） -->
    <div v-if="files.length > 0" class="options-section">
      <div class="compress-action">
        <button 
          class="compress-btn" 
          @click="startUnzip" 
          :disabled="unzipping"
        >
          <span v-if="unzipping">
            <span class="loading-spinner"></span>
            解压中 ({{ unzipProgress }}%)...
          </span>
          <span v-else>开始解压</span>
        </button>
      </div>
    </div>
    <!-- 下载区域 -->
    <div v-if="unzipResults.length > 0" class="download-section">
      <h3 class="section-title">解压内容</h3>
      <div class="download-mode-select">
        <label>
          <input type="radio" value="zip" v-model="downloadMode" />
          合并为一个文件夹（ZIP）下载
        </label>
        <label>
          <input type="radio" value="batch" v-model="downloadMode" />
          一个一个批量下载
        </label>
      </div>
      <div class="compressed-list">
        <div v-for="(item, idx) in unzipResults" :key="idx" class="compressed-item">
          <div class="compressed-icon">
            <i :class="item.isDir ? 'el-icon-folder' : 'el-icon-document' "></i>
          </div>
          <div class="compressed-info">
            <div class="compressed-name">{{ item.fullPath }}</div>
            <div class="compressed-size" v-if="!item.isDir">{{ formatFileSize(item.size) }}</div>
          </div>
          <div class="compressed-actions">
            <button class="download-btn" v-if="!item.isDir" @click="downloadFile(item)">下载</button>
          </div>
        </div>
      </div>
      <div class="download-all">
        <button class="download-all-btn" @click="downloadAll">下载全部文件</button>
      </div>
    </div>
  </div>
</template>
<script setup>
import { ref } from 'vue';
import JSZip from 'jszip';
import { saveAs } from 'file-saver';
const dragover = ref(false);
const files = ref([]);
const unzipResults = ref([]); // {fullPath, isDir, blob, size}
const unzipping = ref(false);
const unzipProgress = ref(0);
const downloadMode = ref('zip'); // 新增，默认合并为ZIP
function onFileDrop(e) {
  dragover.value = false;
  const dropped = Array.from(e.dataTransfer.files).filter(f => f.name.endsWith('.zip'));
  addFiles(dropped);
}
function onFileChange(e) {
  const selected = Array.from(e.target.files).filter(f => f.name.endsWith('.zip'));
  addFiles(selected);
  e.target.value = '';
}
function addFiles(newFiles) {
  files.value = [...files.value, ...newFiles];
}
function removeFile(index) {
  files.value.splice(index, 1);
}
function formatFileSize(bytes) {
  if (bytes === 0) return '0 Bytes';
  const k = 1024;
  const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}
async function startUnzip() {
  if (files.value.length === 0) return;
  unzipping.value = true;
  unzipProgress.value = 0;
  unzipResults.value = [];
  let totalFiles = 0, processed = 0;
  // 统计所有zip内文件数
  for (const file of files.value) {
    const zip = await JSZip.loadAsync(file);
    totalFiles += Object.keys(zip.files).length;
  }
  for (const file of files.value) {
    const zip = await JSZip.loadAsync(file);
    for (const [path, entry] of Object.entries(zip.files)) {
      if (entry.dir) {
        unzipResults.value.push({ fullPath: path, isDir: true });
      } else {
        const blob = await entry.async('blob');
        unzipResults.value.push({ fullPath: path, isDir: false, blob, size: blob.size });
      }
      processed++;
      unzipProgress.value = Math.round((processed / totalFiles) * 100);
    }
  }
  unzipping.value = false;
  unzipProgress.value = 100;
}
function downloadFile(item) {
  if (item.blob) {
    saveAs(item.blob, item.fullPath.split('/').pop());
  }
}
async function downloadAll() {
  if (downloadMode.value === 'zip') {
    // 合并为一个ZIP，文件夹名为实时时间
    const zip = new JSZip();
    const now = new Date();
    const dateStr = now.toISOString().replace(/[:.]/g, '-').replace('T', '_').slice(0,19);
    const folder = zip.folder(dateStr);
    unzipResults.value.forEach(item => {
      if (!item.isDir && item.blob) {
        folder.file(item.fullPath, item.blob);
      }
    });
    const content = await zip.generateAsync({ type: 'blob' });
    saveAs(content, `${dateStr}.zip`);
  } else {
    // 批量下载
    unzipResults.value.forEach(item => {
      if (!item.isDir && item.blob) downloadFile(item);
    });
  }
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
.unzip-container {
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
  font-size: 2.4rem;
  font-weight: 900;
  margin-bottom: 36px;
  color: var(--tool-title-color);
  text-align: center;
  letter-spacing: 1px;
}
.section-title {
  font-size: 1.4rem;
  font-weight: 700;
  margin: 32px 0 18px;
  color: var(--tool-title-color);
  padding-left: 16px;
  border-left: 5px solid var(--tool-title-color);
  background: none;
  color: var(--tool-title-color);
  -webkit-text-fill-color: initial;
}
.upload-section,
.options-section,
.files-section,
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
.unzipped-list {
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
.unzipped-item {
  display: flex;
  align-items: center;
  padding: 22px 32px;
  border-bottom: 1px solid var(--border-color);
  transition: background 0.2s;
  color: var(--text-color);
}
.file-item:last-child,
.unzipped-item:last-child {
  border-bottom: none;
}
.file-item:hover,
.unzipped-item:hover {
  background: var(--border-color);
}
.file-icon,
.unzipped-icon {
  font-size: 2.1rem;
  margin-right: 24px;
  color: var(--tool-title-color);
}
.file-info,
.unzipped-info {
  flex: 1;
}
.file-name,
.unzipped-name {
  font-size: 1.13rem;
  color: var(--text-color);
  margin-bottom: 6px;
  font-weight: 600;
  word-break: break-all;
}
.file-size,
.unzipped-size {
  font-size: 1rem;
  color: var(--tool-desc-color);
}
.file-actions,
.unzipped-actions {
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
.download-mode-select {
  display: flex;
  gap: 32px;
  justify-content: center;
  margin-bottom: 24px;
  font-size: 16px;
  color: var(--text-color);
}
.download-mode-select label {
  cursor: pointer;
  user-select: none;
}
.download-mode-select input[type="radio"] {
  margin-right: 6px;
}
.compressed-list {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 18px;
  margin-bottom: 18px;
}
.compressed-item {
  display: flex;
  align-items: center;
  background: var(--card-bg);
  border-radius: 14px;
  box-shadow: 0 2px 8px rgba(59,130,246,0.06);
  padding: 18px 28px;
  gap: 18px;
}
.compressed-icon {
  font-size: 2rem;
  color: var(--tool-title-color);
  margin-right: 12px;
}
.compressed-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.compressed-name {
  font-size: 1.1rem;
  color: var(--text-color);
  font-weight: 600;
  word-break: break-all;
}
.compressed-size {
  font-size: 1rem;
  color: var(--tool-desc-color);
}
.compressed-actions {
  margin-left: 12px;
}
.download-btn {
  border: none;
  border-radius: 10px;
  font-size: 1.1rem;
  font-weight: 700;
  padding: 10px 28px;
  background: linear-gradient(90deg, var(--tool-title-color) 0%, #06b6d4 100%);
  color: #fff;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(59,130,246,0.08);
}
.download-btn:hover {
  background: linear-gradient(90deg, #06b6d4 0%, var(--tool-title-color) 100%);
  transform: translateY(-2px) scale(1.04);
}
.download-all {
  text-align: center;
  margin-top: 0;
}
.download-all-btn {
  font-size: 1.1rem;
  padding: 12px 38px;
  border-radius: 14px;
  margin-top: 8px;
}
@media (max-width: 900px) {
  .unzip-container {
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
  .unzipped-list {
    max-width: 99vw;
    width: 100%;
  }
}
@media (max-width: 600px) {
  .compressed-item {
    flex-direction: column;
    align-items: flex-start;
    padding: 14px 8px;
    gap: 8px;
  }
  .download-mode-select {
    flex-direction: column;
    gap: 10px;
    font-size: 15px;
  }
}
</style> 