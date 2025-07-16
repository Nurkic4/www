<template>
  <div class="markets-container">
    <h2 class="page-title">A股主要指数行情</h2>
    
    <div class="data-info">
      <div class="data-source">数据来源: 聚合数据</div>
      <div class="data-update">上次更新: {{ lastUpdateTime }}</div>
    </div>
    
    <!-- 指数卡片网格 -->
    <div class="markets-grid">
      <div 
        v-for="index in indices" 
        :key="index.name"
        class="index-card"
      >
        <div class="card-header">
          <div class="index-name">{{ index.name }}</div>
        </div>
        <div class="card-body">
          <div 
            class="current-price"
            :class="getUpDownClass(index.increase)"
          >
            {{ index.nowPri }}
          </div>
          <div class="change-info">
            <span 
              class="change-value"
              :class="getUpDownClass(index.increase)"
            >
              {{ index.increase }}
            </span>
            <span 
              class="change-percent"
              :class="getUpDownClass(index.increPer)"
            >
              {{ index.increPer }}%
            </span>
          </div>
        </div>
        <div class="card-footer">
          <div class="volume">成交额: {{ formatAmount(index.traAmount) }}</div>
          <div class="time">{{ index.time }}</div>
        </div>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-overlay">
      <div class="loading-spinner"></div>
      <div class="loading-text">正在获取指数数据...</div>
    </div>
    
    <!-- 手动刷新按钮 -->
    <div class="refresh-container">
      <button class="refresh-btn" @click="manualRefresh" :disabled="loading">
        <span v-if="!loading">手动刷新数据</span>
        <span v-else>刷新中...</span>
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { request } from '@/utils/request'

const JUHE_API_URL = '/juheapi/finance/stock/hs';
const JUHE_API_KEY = 'f25f2858fdb7e0211e5d4cfe5fdb2d10';
const STORAGE_KEY = 'juhe_market_data';
const STORAGE_DATE_KEY = 'juhe_market_last_date';

const juheIndices = [
  { name: '上证指数', type: 0, gid: null },
  { name: '深证成指', type: 1, gid: null },
  { name: '沪深300', type: null, gid: 'sh000300' },
  { name: '科创50', type: null, gid: 'sh000688' },
  { name: '中证500', type: null, gid: 'sh000905' },
  { name: '创业板指', type: null, gid: 'sz399006' }
];

const indices = ref([])
const loading = ref(false)
const lastUpdateTime = ref('--')

function todayStr() {
  return new Date().toISOString().slice(0, 10);
}

function shouldAutoRefresh() {
  const lastDate = localStorage.getItem(STORAGE_DATE_KEY);
  return lastDate !== todayStr();
}

async function fetchJuheIndex({ type, gid, name }) {
  let url = `${JUHE_API_URL}?key=${JUHE_API_KEY}`;
  if (type !== null && type !== undefined) url += `&type=${type}`;
  if (gid) url += `&gid=${gid}`;
  try {
    const { data, error } = await request({ url, method: 'GET' });
    if (error) {
      console.error(`Failed to fetch data for ${name}:`, error);
      return {
        name,
        nowPri: '--',
        increPer: '--',
        increase: '--',
        traAmount: '--',
        time: '--',
      };
    }
    if (type === 0 || type === 1) {
      // 兼容 result.data 或 result
      const d = data.result?.data || data.result || {};
      return {
        name: d.name || name,
        nowPri: d.nowpri || '--',
        increPer: d.increPer || '--',
        increase: d.increase || '--',
        traAmount: d.dealPri || '--',
        time: d.time || '--',
      };
    } else {
      // 其它指数
      const d = data.result?.[0]?.data || {};
      return {
        name: d.name || name,
        nowPri: d.nowPri || '--',
        increPer: d.increPer || '--',
        increase: d.increase || '--',
        traAmount: d.traAmount || '--',
        time: d.time || '--',
      };
    }
  } catch (e) {
    console.error(`Failed to fetch data for ${name}:`, e);
    return {
      name,
      nowPri: '--',
      increPer: '--',
      increase: '--',
      traAmount: '--',
      time: '--',
    };
  }
}

function formatAmount(amount) {
  if (!amount || amount === '--') return '--';
  const n = Number(amount);
  if (isNaN(n)) return amount;
  if (n >= 1e8) return (n / 1e8).toFixed(2) + '亿';
  if (n >= 1e4) return (n / 1e4).toFixed(2) + '万';
  return n.toLocaleString();
}

function getUpDownClass(val) {
  const n = Number(val);
  if (isNaN(n)) return '';
  if (n > 0) return 'up';
  if (n < 0) return 'down';
  return '';
}

async function updateMarketData(force = false) {
  if (!force && !shouldAutoRefresh()) {
    const cached = localStorage.getItem(STORAGE_KEY);
    const cachedDate = localStorage.getItem(STORAGE_DATE_KEY);
    if (cached && cachedDate) {
      indices.value = JSON.parse(cached);
      lastUpdateTime.value = cachedDate;
      return;
    }
  }
  loading.value = true;
  const data = [];
  for (const idx of juheIndices) {
    const d = await fetchJuheIndex(idx);
    data.push(d);
  }
  indices.value = data;
  const now = new Date();
  const nowStr = now.toLocaleString('zh-CN', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit', second: '2-digit', hour12: false
  });
  localStorage.setItem(STORAGE_KEY, JSON.stringify(data));
  localStorage.setItem(STORAGE_DATE_KEY, nowStr);
  lastUpdateTime.value = nowStr;
  loading.value = false;
}

async function manualRefresh() {
  await updateMarketData(true);
}

onMounted(() => {
  updateMarketData();
});
</script>

<style scoped>
.markets-container {
  padding: 40px 32px 32px 32px;
  max-width: 1200px;
  margin: 48px auto 0 auto;
  background: none;
  border-radius: 0;
  box-shadow: none;
  min-height: 700px;
  position: relative;
  color: var(--text-color);
}

.page-title {
  font-size: 2.4rem;
  font-weight: 900;
  margin-bottom: 36px;
  color: var(--tool-title-color);
  text-align: center;
  letter-spacing: 1px;
}

.data-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
  background: var(--card-bg);
  padding: 16px 24px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  border: 1px solid var(--border-color);
}

.data-source,
.data-update {
  font-size: 1.1rem;
  color: var(--tool-desc-color);
  font-weight: 600;
  display: flex;
  align-items: center;
}

.data-source:before {
  content: "📊";
  margin-right: 8px;
  font-size: 1.2rem;
}

.data-update:before {
  content: "🕒";
  margin-right: 8px;
  font-size: 1.2rem;
}

.markets-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 24px;
  margin-bottom: 32px;
}

.index-card {
  background: var(--card-bg);
  border-radius: 18px;
  padding: 24px;
  box-shadow: 0 4px 16px rgba(59,130,246,0.08);
  transition: all 0.3s ease;
  border: 2px solid transparent;
  color: var(--text-color);
}

.index-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(59,130,246,0.15);
}

.index-card.up {
  border-color: #ef4444;
  background: linear-gradient(135deg, rgba(239, 68, 68, 0.1) 0%, var(--card-bg) 100%);
}

.index-card.down {
  border-color: #10b981;
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.1) 0%, var(--card-bg) 100%);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.index-name {
  font-size: 1.4rem;
  font-weight: 700;
  color: var(--text-color);
}

.index-code {
  font-size: 1rem;
  color: var(--tool-desc-color);
  background: var(--bg-color);
  padding: 4px 8px;
  border-radius: 6px;
}

.card-body {
  margin-bottom: 16px;
}

.current-price {
  font-size: 2.2rem;
  font-weight: 900;
  margin-bottom: 8px;
  transition: color 0.2s;
  color: var(--text-color);
}

.current-price.up {
  color: #ef4444;
  font-weight: 900;
}

.current-price.down {
  color: #10b981;
  font-weight: 900;
}

.change-info {
  display: flex;
  gap: 12px;
  align-items: center;
}

.change-value,
.change-percent {
  font-size: 1.1rem;
  font-weight: 900;
  padding: 4px 8px;
  border-radius: 6px;
  transition: color 0.2s;
}

.change-value.up,
.change-percent.up {
  color: #ef4444;
  background: rgba(239, 68, 68, 0.1);
  font-weight: 900;
}

.change-value.down,
.change-percent.down {
  color: #10b981;
  background: rgba(16, 185, 129, 0.1);
  font-weight: 900;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.9rem;
  color: var(--tool-desc-color);
}

.volume {
  font-weight: 500;
}

.time {
  font-family: monospace;
}

/* 加载状态 */
.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: var(--card-bg);
  opacity: 0.9;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-radius: 24px;
  z-index: 10;
  backdrop-filter: blur(4px);
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid var(--border-color);
  border-top: 4px solid var(--tool-title-color);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

.loading-text {
  font-size: 1.1rem;
  color: var(--tool-desc-color);
  font-weight: 600;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .markets-container {
    padding: 20px 16px;
    margin: 20px 10px;
  }
  
  .markets-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .index-card {
    padding: 20px;
  }
  
  .current-price {
    font-size: 1.8rem;
  }
}

.refresh-container {
  margin-top: 32px;
  text-align: center;
}

.refresh-btn {
  padding: 14px 32px;
  background: linear-gradient(90deg, var(--tool-title-color) 0%, #06b6d4 100%);
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 1.1rem;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(59,130,246,0.18);
}

.refresh-btn:hover {
  background: linear-gradient(90deg, #06b6d4 0%, var(--tool-title-color) 100%);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(59,130,246,0.25);
}

.refresh-btn:disabled {
  background: var(--border-color);
  color: var(--tool-desc-color);
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}
</style> 