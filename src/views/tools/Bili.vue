<template>
  <div class="video-download-center-wrap">
    <div class="video-download-container">
      <h2 class="page-title">视频解析下载</h2>
      <div class="video-form-card">
        <div class="form-group">
          <label class="form-label">B站视频链接：</label>
          <input v-model="videoUrl" class="form-input" type="text" placeholder="粘贴B站视频链接，如 https://www.bilibili.com/video/BV..." />
        </div>
        <div class="form-group">
          <button class="parse-btn" @click="parseVideo" :disabled="parsing || !videoUrl">解析并下载</button>
        </div>
      </div>
      <div class="result-section">
        <div v-if="parsing" class="parsing-tip">
          <span class="loading-spinner"></span> 正在解析，请稍候...
        </div>
        <div v-else-if="parseError" class="error-tip">
          {{ parseError }}
        </div>
        <div v-else-if="videoInfo">
          <div class="video-meta">
            <img
              :src="coverImg || fixImgUrl(videoInfo.imgurl)"
              class="cover-img"
              @error="onCoverError"
            />
            <div class="meta-info">
              <div class="video-title">{{ videoInfo.title }}</div>
              <div class="video-author">
                <img :src="fixImgUrl(videoInfo.user.user_img)" class="author-img" @error="onAuthorError" />
                <span>{{ videoInfo.user.name }}</span>
              </div>
              <div class="video-desc">{{ videoInfo.desc }}</div>
            </div>
          </div>
          <div class="quality-select" v-if="videoInfo.data[0].accept && videoInfo.data[0].accept.length > 0">
            <label>清晰度：</label>
            <select v-model="selectedQuality">
              <option v-for="(q, idx) in videoInfo.data[0].accept" :key="q" :value="idx">{{ q }}</option>
            </select>
          </div>
          <a :href="getDownloadUrl()" target="_blank" class="download-link">下载视频</a>
        </div>
        <div v-else class="info-tip">
          请输入B站视频链接，点击解析
        </div>
      </div>
    </div>
  </div>
</template>
<script setup>
import { ref } from 'vue';
import biliLogo from '../css/png/bilibili-logo1.png';
import biliLogo2 from '../css/png/bilibili-logo2.png';
import { request } from '@/utils/request'
const videoUrl = ref('')
const parsing = ref(false)
const parseError = ref('')
const videoInfo = ref(null)
const selectedQuality = ref(0)
const coverImg = ref('')

function fixImgUrl(url) {
  if (!url) return biliLogo;
  return url.replace(/^http:\/\//, 'https://');
}

function onCoverError(e) {
  e.target.src = biliLogo;
}

function onAuthorError(e) {
  e.target.src = biliLogo2;
}

async function parseVideo() {
  parsing.value = true;
  parseError.value = '';
  videoInfo.value = null;
  selectedQuality.value = 0;
  try {
    const { data, error } = await request({ url: `https://api.mir6.com/api/bzjiexi?url=${encodeURIComponent(videoUrl.value)}&type=json`, method: 'GET' });
    if (data && data.code === 200 && data.data && data.data.length > 0) {
      videoInfo.value = data;
      coverImg.value = '';
    } else {
      parseError.value = data?.msg || '解析失败，请检查链接';
    }
  } catch (e) {
    parseError.value = '网络错误或接口异常';
  } finally {
    parsing.value = false;
  }
}
function getDownloadUrl() {
  if (!videoInfo.value) return '';
  return videoInfo.value.data[0].video_url;
}
</script>
<style scoped>
.video-download-center-wrap {
  width: 100%;
  min-height: calc(100vh - 120px);
  display: flex;
  justify-content: center;
  align-items: center;
  background: none;
}
.video-download-container {
  max-width: 700px;
  width: 100%;
  padding: 36px 18px 28px 18px;
  min-height: 420px;
  color: var(--text-color);
  display: flex;
  flex-direction: column;
  align-items: center;
}
.page-title {
  font-size: 2.2rem;
  font-weight: 900;
  color: var(--tool-title-color);
  text-align: center;
  letter-spacing: 1.5px;
  margin-bottom: 32px;
}
.video-form-card {
  background: var(--card-bg);
  border-radius: 22px;
  box-shadow: 0 4px 24px rgba(59,130,246,0.10);
  padding: 32px 18px 18px 18px;
  margin-bottom: 28px;
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
}
.form-group {
  margin-bottom: 18px;
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  max-width: 520px;
}
.form-label {
  width: 120px;
  color: var(--tool-title-color);
  font-weight: 700;
  font-size: 1.08rem;
}
.form-input {
  flex: 1;
  padding: 12px 16px;
  border-radius: 10px;
  border: 2px solid var(--border-color);
  background: var(--bg-color);
  font-size: 1.05rem;
  color: var(--text-color);
}
.form-input:focus {
  border-color: var(--tool-title-color);
  outline: none;
}
.parse-btn {
  background: linear-gradient(90deg, var(--tool-title-color) 0%, #6ed0ff 100%);
  color: white;
  border: none;
  padding: 12px 32px;
  border-radius: 12px;
  font-size: 1.1rem;
  font-weight: 800;
  cursor: pointer;
  transition: all 0.3s;
  min-width: 120px;
  box-shadow: 0 2px 8px rgba(59,130,246,0.10);
  margin-top: 6px;
  letter-spacing: 1px;
}
.parse-btn:disabled {
  background: #b3e5fc;
  cursor: not-allowed;
  box-shadow: none;
}
.parse-btn:hover:not(:disabled) {
  background: linear-gradient(90deg, #6ed0ff 0%, var(--tool-title-color) 100%);
  transform: translateY(-2px) scale(1.04);
}
.result-section {
  margin-top: 24px;
  text-align: center;
}
.loading-spinner {
  display: inline-block;
  width: 20px;
  height: 20px;
  border: 3px solid #b3e5fc;
  border-radius: 50%;
  border-top-color: var(--tool-title-color);
  animation: spin 1s ease-in-out infinite;
  margin-right: 12px;
}
@keyframes spin {
  to { transform: rotate(360deg); }
}
.success-tip {
  color: #10b981;
  font-size: 1.08rem;
  margin-bottom: 10px;
  font-weight: bold;
}
.download-link {
  display: inline-block;
  margin-top: 14px;
  padding: 12px 36px;
  background: linear-gradient(90deg, var(--tool-title-color) 0%, #6ed0ff 100%);
  color: #fff;
  border-radius: 14px;
  font-size: 1.18rem;
  font-weight: 800;
  text-decoration: none;
  transition: all 0.3s;
  box-shadow: 0 2px 12px rgba(0,161,214,0.10);
  letter-spacing: 1px;
}
.download-link:hover {
  background: linear-gradient(90deg, #6ed0ff 0%, var(--tool-title-color) 100%);
  transform: translateY(-2px) scale(1.04);
}
.info-tip {
  color: #888;
  font-size: 1.05rem;
  margin-top: 10px;
  line-height: 1.7;
}
.parsing-tip {
  color: var(--tool-title-color);
  font-size: 1.08rem;
  margin-top: 12px;
  font-weight: 700;
}
.error-tip {
  color: #ef4444;
  font-size: 1.08rem;
  margin: 12px 0;
  font-weight: 700;
}
.video-meta {
  display: flex;
  gap: 32px;
  align-items: center;
  justify-content: center;
  margin-bottom: 24px;
  background: linear-gradient(90deg, var(--bg-color) 0%, var(--card-bg) 100%);
  border-radius: 20px;
  padding: 24px 32px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  border: 1.5px solid var(--border-color);
  max-width: 700px;
  margin-left: auto;
  margin-right: auto;
}
.cover-img {
  width: 120px;
  height: 120px;
  object-fit: cover;
  border-radius: 16px;
  box-shadow: 0 2px 12px #e0e7ef;
  background: var(--bg-color);
}
.meta-info { 
  flex: 1; 
  text-align: left;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  justify-content: center;
  gap: 10px;
}
.video-title { 
  font-size: 1.3rem; 
  font-weight: 900; 
  margin-bottom: 4px; 
  color: var(--text-color);
  line-height: 1.4;
  font-family: inherit;
}
.video-author { 
  display: flex; 
  align-items: center; 
  gap: 10px; 
  margin-bottom: 4px; 
}
.author-img { 
  width: 36px; 
  height: 36px; 
  border-radius: 50%; 
  border: 2px solid var(--tool-title-color);
}
.video-desc { 
  color: var(--tool-desc-color); 
  font-size: 1.02rem; 
  line-height: 1.5;
  margin-top: 2px;
}
.quality-select { 
  margin-bottom: 12px; 
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}
.quality-select label {
  font-size: 1.08rem;
  font-weight: 700;
  color: var(--tool-title-color);
}
.quality-select select {
  padding: 8px 18px;
  border-radius: 10px;
  border: 2px solid var(--border-color);
  background: var(--card-bg);
  font-size: 1.08rem;
  transition: border-color 0.3s;
  font-family: inherit;
  color: var(--text-color);
}
.quality-select select:focus {
  border-color: var(--tool-title-color);
  outline: none;
}
@media (max-width: 700px) {
  .video-download-container {
    max-width: 99vw;
    padding: 12px 2vw 12px 2vw;
  }
  .video-form-card {
    padding: 18px 4vw 12px 4vw;
  }
  .form-group {
    max-width: 99vw;
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  .form-label {
    width: 100%;
    margin-bottom: 2px;
  }
}
</style> 