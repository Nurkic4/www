<template>
  <div class="network-page">
    <h2 class="page-title">
      <span class="icon-glow">🛰️</span> 网络状态监测
    </h2>
    <div class="network-summary-bar-wrap">
      <div class="network-summary-bar">
        <div class="summary-item">
          <span class="summary-label">网络类型</span>
          <span class="summary-value">{{ networkTypeText }}</span>
        </div>
        <div class="summary-item">
          <span class="summary-label">信号强度</span>
          <span class="signal-bar-outer">
            <span :class="['signal-bar', signalLevel]"></span>
          </span>
        </div>
        <div class="summary-item">
          <span class="summary-label">HTTPS</span>
          <span class="summary-value" :class="{secure: isHttps, insecure: !isHttps}">
            <i :class="isHttps ? 'el-icon-lock' : 'el-icon-warning-outline'"></i>
            {{ isHttps ? '安全' : '不安全' }}
          </span>
        </div>
        <div class="summary-item">
          <span class="summary-label">DNS信息</span>
          <span class="summary-value">{{ dnsInfo || '获取中...' }}</span>
        </div>
      </div>
    </div>
    <div class="network-cards-masonry">
      <div class="network-card">
        <div class="section-title"><span class="icon-glow">⚡</span> 带宽测速</div>
        <el-button type="primary" class="glow-btn" @click="runSpeedTest" :loading="speedTesting">测速</el-button>
        <div class="speed-values">
          <div v-if="downloadSpeed" class="speed-card down">
            <span class="speed-label">下行</span>
            <span class="speed-value">{{ downloadSpeed }} <span class="unit">Mbps</span></span>
          </div>
          <div v-if="uploadSpeed" class="speed-card up">
            <span class="speed-label">上行</span>
            <span class="speed-value">{{ uploadSpeed }} <span class="unit">Mbps</span></span>
          </div>
        </div>
      </div>
      <div class="network-card">
        <div class="section-title"><span class="icon-glow">🌐</span> DNS测速</div>
        <el-button class="glow-btn" @click="runDnsTest" :loading="dnsTesting">DNS解析测速</el-button>
        <div v-if="dnsDelay" class="dns-delay-value">
          DNS解析耗时：<span class="highlight">{{ dnsDelay }} ms</span>
        </div>
      </div>
      <div class="network-card">
        <div class="section-title"><span class="icon-glow">🔗</span> WebRTC NAT类型检测</div>
        <el-button class="glow-btn" @click="runWebRTC" :loading="webrtcTesting">NAT检测</el-button>
        <div v-if="natType" class="nat-type-value">
          NAT类型：<span class="highlight">{{ natType }}</span>
        </div>
        <div v-if="iceCandidates.length" class="ice-candidates">
          ICE候选类型：
          <span v-for="(c, i) in iceCandidates" :key="i" class="ice-type">{{ c }}</span>
        </div>
      </div>
      <div class="network-card">
        <div class="section-title"><span class="icon-glow">🔌</span> WebSocket连通性</div>
        <el-input v-model="wsUrl" placeholder="ws(s)://..." class="input-glass" style="width:100%;margin-bottom:8px;" />
        <el-button class="glow-btn" @click="runWsTest" :loading="wsTesting">WebSocket测试</el-button>
        <div v-if="wsResult !== null" class="ws-result">
          结果：<span :class="['highlight', wsResult ? 'ok' : 'fail']">{{ wsResult ? '可连通' : '不可连通' }}</span>
        </div>
      </div>
      <div class="network-card network-card-wide">
        <div class="section-title"><span class="icon-glow">📈</span> 延迟历史与Jitter</div>
        <div class="chart-glass">
          <div ref="chartRef" class="chart-box"></div>
        </div>
        <div class="latency-jitter-row">
          <span>当前延迟：<span class="highlight">{{ latency }} ms</span></span>
          <span>Jitter（抖动）：<span class="highlight">{{ jitter }} ms</span></span>
        </div>
        <el-button class="glow-btn" @click="exportLatencyCSV">导出延迟历史CSV</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, watch } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { request } from '@/utils/request'

// 1. 网络类型、信号、HTTPS、DNS、IP
const latency = ref('--')
const latencyHistory = ref([])
const networkType = ref('unknown')
const publicIP = ref('')
const dnsInfo = ref('')
const isHttps = location.protocol === 'https:'
const downloadSpeed = ref('')
const uploadSpeed = ref('')
const speedTesting = ref(false)
const dnsDelay = ref('')
const dnsTesting = ref(false)
const natType = ref('')
const iceCandidates = ref([])
const webrtcTesting = ref(false)
const wsUrl = ref('wss://echo.websocket.org')
const wsResult = ref(null)
const wsTesting = ref(false)
const chartRef = ref(null)
let chart = null
let timer = null

// 网络类型切换提醒
onMounted(() => {
  if (navigator.connection) {
    navigator.connection.addEventListener('change', () => {
      ElMessage.info('网络类型已切换为：' + (navigator.connection.effectiveType || '未知'))
    })
  }
})

// 公网IP
async function fetchPublicIP() {
  try {
    const { data, error } = await request({ url: '/api/public-ip', method: 'GET' })
    if (error) {
      publicIP.value = data.error || '获取失败'
    } else {
      publicIP.value = data.ip || '获取失败'
    }
  } catch (err) {
    publicIP.value = '获取失败'
    ElMessage.error('获取公网IP失败：' + err.message)
  }
}
fetchPublicIP()

// DNS信息
async function fetchDnsInfo() {
  try {
    const { data, error } = await request({ url: '/api/dns-info?domain=www.baidu.com', method: 'GET' })
    if (error) {
      dnsInfo.value = data.error || '未知'
    } else {
      dnsInfo.value = data.ips ? data.ips.join(', ') : '未知'
    }
  } catch (err) {
    dnsInfo.value = '获取失败'
    ElMessage.error('获取DNS信息失败：' + err.message)
  }
}
fetchDnsInfo()

// DNS解析测速
async function runDnsTest() {
  dnsTesting.value = true
  try {
    const t0 = performance.now()
    const { data, error } = await request({ url: '/api/dns-info?domain=www.baidu.com', method: 'GET' })
    if (error) {
      dnsDelay.value = '失败'
    } else {
      dnsDelay.value = data.duration || (performance.now() - t0).toFixed(2)
    }
  } catch (err) {
    dnsDelay.value = '失败'
    ElMessage.error('DNS解析测速失败：' + err.message)
  }
  dnsTesting.value = false
}

// 带宽测速
async function runSpeedTest() {
  speedTesting.value = true
  downloadSpeed.value = ''
  uploadSpeed.value = ''
  // 下行测速
  try {
    const t0 = Date.now()
    const { data, error } = await request({ url: '/api/speedtest/download?size=5242880', method: 'GET' })
    if (error) {
      downloadSpeed.value = '失败'
    } else {
      const duration = (Date.now() - t0) / 1000
      const sizeMB = 5
      downloadSpeed.value = ((sizeMB * 8) / duration).toFixed(2)
    }
  } catch (err) {
    downloadSpeed.value = '失败'
    ElMessage.error('带宽测速失败：' + err.message)
  }
  // 上行测速（需后端支持，可选实现）
  // uploadSpeed.value = '需后端支持'
  speedTesting.value = false
}

// WebRTC/ICE NAT检测
function runWebRTC() {
  webrtcTesting.value = true
  natType.value = ''
  iceCandidates.value = []
  const pc = new RTCPeerConnection({ iceServers: [{ urls: 'stun:stun.l.google.com:19302' }] })
  pc.createDataChannel('')
  pc.createOffer().then(offer => pc.setLocalDescription(offer))
  pc.onicecandidate = (e) => {
    if (e.candidate) {
      const cand = e.candidate.candidate
      if (cand.includes('typ host')) iceCandidates.value.push('host')
      if (cand.includes('typ srflx')) iceCandidates.value.push('srflx')
      if (cand.includes('typ relay')) iceCandidates.value.push('relay')
      // 简单NAT类型判断
      if (iceCandidates.value.includes('relay')) natType.value = '受限/对称型NAT'
      else if (iceCandidates.value.includes('srflx')) natType.value = '普通NAT'
      else if (iceCandidates.value.includes('host')) natType.value = '开放型'
    } else {
      webrtcTesting.value = false
    }
  }
}

// WebSocket连通性
async function runWsTest() {
  wsTesting.value = true
  wsResult.value = null
  try {
    const ws = new WebSocket(wsUrl.value)
    ws.onopen = () => { ws.close(); wsResult.value = true; wsTesting.value = false }
    ws.onerror = () => { wsResult.value = false; wsTesting.value = false }
  } catch (err) {
    wsResult.value = false
    wsTesting.value = false
    ElMessage.error('WebSocket测试失败：' + err.message)
  }
}

// 网络类型、信号
const networkTypeText = computed(() => {
  if (!navigator.connection) return '未知'
  const t = navigator.connection.effectiveType || navigator.connection.type
  switch (t) {
    case 'wifi': return 'WiFi'
    case 'ethernet': return '有线'
    case 'cellular': return '蜂窝'
    case '4g': return '4G'
    case '3g': return '3G'
    case '2g': return '2G'
    case 'none': return '无网络'
    default: return t || '未知'
  }
})
const signalLevel = computed(() => {
  if (typeof latency.value === 'string' || latency.value === null) return 'bad'
  if (latency.value <= 80) return 'good'
  if (latency.value <= 200) return 'normal'
  return 'bad'
})

// 延迟与jitter
async function pingTest() {
  try {
    const t0 = Date.now()
    const { data, error } = await request({ url: '/api/proxy?url=https://www.baidu.com/favicon.ico&method=HEAD', method: 'GET' })
    if (error) {
      latency.value = null
      addLatencyPoint(null)
      ElMessage.error('延迟测试失败：' + error.message)
    } else {
      const delay = data.duration || (Date.now() - t0)
      latency.value = delay
      addLatencyPoint(delay)
    }
  } catch (err) {
    latency.value = null
    addLatencyPoint(null)
    ElMessage.error('延迟测试失败：' + err.message)
  }
}
function addLatencyPoint(val) {
  const now = new Date()
  latencyHistory.value.push({
    time: now.toLocaleTimeString('zh-CN', { hour12: false }),
    value: typeof val === 'number' ? val : null
  })
  if (latencyHistory.value.length > 60) latencyHistory.value.shift()
  updateChart()
}
function calcJitter() {
  const arr = latencyHistory.value.map(d => d.value).filter(v => typeof v === 'number')
  if (arr.length < 2) return '--'
  const avg = arr.reduce((a, b) => a + b, 0) / arr.length
  const variance = arr.reduce((a, b) => a + Math.pow(b - avg, 2), 0) / arr.length
  return Math.sqrt(variance).toFixed(2)
}
const jitter = computed(() => calcJitter())

// 图表
function getThemeColors() {
  const theme = document.documentElement.getAttribute('data-theme')
  if (theme === 'dark') {
    return {
      line: '#4e9eff',
      axis: '#a6c8ff',
      split: '#44485a',
      area: 'rgba(78,158,255,0.13)',
      tooltipBg: '#23253a',
      tooltipText: '#a6c8ff'
    }
  } else {
    return {
      line: 'var(--tool-title-color)',
      axis: 'var(--tool-title-color)',
      split: 'var(--border-color)',
      area: 'rgba(78,107,222,0.08)',
      tooltipBg: '#fff',
      tooltipText: '#333'
    }
  }
}
function updateChart() {
  if (!chart) return
  const colors = getThemeColors()
  const times = latencyHistory.value.map(d => d.time)
  const values = latencyHistory.value.map(d => d.value)
  chart.setOption({
    xAxis: {
      data: times,
      axisLine: { lineStyle: { color: colors.axis } },
      axisLabel: { color: colors.axis }
    },
    yAxis: {
      axisLine: { lineStyle: { color: colors.axis } },
      axisLabel: { color: colors.axis },
      splitLine: { lineStyle: { color: colors.split, type: 'dashed' } }
    },
    series: [{
      data: values,
      lineStyle: { color: colors.line, width: 3 },
      areaStyle: { color: colors.area }
    }],
    tooltip: {
      backgroundColor: colors.tooltipBg,
      textStyle: { color: colors.tooltipText }
    }
  })
}
function initChart() {
  const colors = getThemeColors()
  chart = echarts.init(chartRef.value)
  chart.setOption({
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      backgroundColor: colors.tooltipBg,
      textStyle: { color: colors.tooltipText }
    },
    grid: { left: 40, right: 20, top: 30, bottom: 30 },
    xAxis: {
      type: 'category',
      data: [],
      axisLine: { lineStyle: { color: colors.axis } },
      axisLabel: { color: colors.axis }
    },
    yAxis: {
      type: 'value',
      min: 0,
      max: 1000,
      axisLine: { lineStyle: { color: colors.axis } },
      axisLabel: { color: colors.axis },
      splitLine: { lineStyle: { color: colors.split, type: 'dashed' } }
    },
    series: [{
      name: '延迟',
      type: 'line',
      smooth: true,
      showSymbol: false,
      data: [],
      lineStyle: { color: colors.line, width: 3 },
      areaStyle: { color: colors.area }
    }]
  })
}
function refreshChartTheme() {
  if (chart) updateChart()
}
onMounted(() => {
  getNetworkInfo()
  if (navigator.connection) {
    navigator.connection.addEventListener('change', getNetworkInfo)
  }
  initChart()
  pingTest()
  timer = setInterval(pingTest, 5000)
  // 监听主题切换
  const observer = new MutationObserver(() => {
    refreshChartTheme()
  })
  observer.observe(document.documentElement, { attributes: true, attributeFilter: ['data-theme'] })
})
onUnmounted(() => {
  if (timer) clearInterval(timer)
  if (navigator.connection) {
    navigator.connection.removeEventListener('change', getNetworkInfo)
  }
  if (chart) {
    chart.dispose()
    chart = null
  }
})

// 网络信息
function getNetworkInfo() {
  const nav = navigator
  if (nav.connection) {
    if (nav.connection.type) networkType.value = nav.connection.type
    else if (nav.connection.effectiveType) networkType.value = nav.connection.effectiveType
    else networkType.value = 'unknown'
  } else {
    networkType.value = 'unknown'
  }
}

// 导出延迟历史
function exportLatencyCSV() {
  const arr = latencyHistory.value.map(d => [d.time, d.value])
  const csv = '时间,延迟(ms)\n' + arr.map(row => row.join(',')).join('\n')
  const blob = new Blob([csv], { type: 'text/csv' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = 'latency_history.csv'
  a.click()
  URL.revokeObjectURL(url)
}
</script>

<style scoped>
.network-page {
  min-height: 100vh;
  background: var(--bg-color);
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 48px 0 0 0;
  max-width: 100vw;
}
.page-title {
  font-size: 2.1rem;
  font-weight: 900;
  color: var(--tool-title-color);
  margin-bottom: 18px;
  text-align: center;
  letter-spacing: 1px;
  text-shadow: 0 2px 12px rgba(78,107,222,0.13);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}
.network-summary-bar-wrap,
.network-cards-masonry {
  max-width: 1200px;
}
.network-summary-bar-wrap {
  max-width: none;
  width: 1100px;
  margin: auto auto;
  margin-bottom: 25px;
}
.network-summary-bar {
  width: 100%;
  background: var(--card-bg);
  border-radius: 14px;
  box-shadow: 0 2px 8px rgba(78,107,222,0.08);
  border: 1.4px solid var(--ai-border);
  padding: 70px 150px 60px 140px;
  box-sizing: border-box;
  display: flex;
  flex-wrap: wrap;
  gap: 18px 24px;
  align-items: center;
  justify-content: space-between;
  min-height: 180px;
}
.network-summary-bar .summary-item {
  flex: 1 1 0;
  min-width: 120px;
  text-align: center;
  font-size: 2.4rem;
  gap: 18px;
}
.network-cards-masonry {
  width: 100%;
  max-width: 1100px;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
  margin: 0 auto 24px auto;
}
.network-card {
  background: var(--card-bg);
  border-radius: 16px;
  box-shadow: 0 2px 8px rgba(78,107,222,0.08);
  border: 1.2px solid var(--ai-border);
  padding: 18px 18px 14px 18px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  transition: box-shadow 0.2s, background 0.2s;
  min-width: 0;
}
.network-card-wide {
  grid-column: 1 / span 2;
  min-width: 0;
  padding: 24px 32px 18px 32px;
}
.network-card {
  background: var(--card-bg);
  border-radius: 18px;
  box-shadow: 0 4px 18px rgba(78,107,222,0.10);
  border: 1.5px solid var(--ai-border);
  display: flex;
  flex-direction: column;
  gap: 12px;
  transition: box-shadow 0.2s, background 0.2s;
}
.network-summary-card {
  max-width: 260px;
  min-width: 200px;
  flex: 1 1 200px;
  padding: 16px 12px 12px 12px;
  margin-bottom: 0;
}
.network-summary {
  display: flex;
  flex-wrap: wrap;
  gap: 24px 36px;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.summary-item {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  min-width: 120px;
  font-size: 1.08rem;
  color: var(--text-color);
  gap: 4px;
  background: rgba(120,140,220,0.06);
  border-radius: 14px;
  padding: 10px 18px;
  box-shadow: 0 2px 8px rgba(78,107,222,0.06);
}
.summary-label {
  color: var(--tool-desc-color);
  font-size: 0.98rem;
  font-weight: 500;
}
.summary-value {
  font-size: 1.18rem;
  font-weight: bold;
  color: var(--tool-title-color);
  letter-spacing: 1px;
}
.ip-value {
  font-size: 1.18rem;
  color: #10b981;
  letter-spacing: 1px;
}
.secure {
  color: #10b981;
}
.insecure {
  color: #f56c6c;
}
.signal-bar-outer {
  display: flex;
  align-items: center;
  height: 22px;
}
.signal-bar {
  display: inline-block;
  width: 22px;
  height: 22px;
  background: url('data:image/svg+xml;utf8,<svg width="22" height="22" xmlns="http://www.w3.org/2000/svg"><g><rect x="2" y="14" width="3" height="6" fill="%2333cc33"/><rect x="7" y="10" width="3" height="10" fill="%2333cc33"/><rect x="12" y="6" width="3" height="14" fill="%2333cc33"/><rect x="17" y="2" width="3" height="18" fill="%2333cc33"/></g></svg>') no-repeat center/contain;
  margin-right: 2px;
  filter: drop-shadow(0 0 4px #33cc33);
  transition: background 0.2s;
}
.signal-bar.normal {
  background: url('data:image/svg+xml;utf8,<svg width="22" height="22" xmlns="http://www.w3.org/2000/svg"><g><rect x="2" y="14" width="3" height="6" fill="%23ffb300"/><rect x="7" y="10" width="3" height="10" fill="%23ffb300"/><rect x="12" y="6" width="3" height="14" fill="%23cccccc"/><rect x="17" y="2" width="3" height="18" fill="%23cccccc"/></g></svg>') no-repeat center/contain;
  filter: drop-shadow(0 0 4px #ffb300);
}
.signal-bar.bad {
  background: url('data:image/svg+xml;utf8,<svg width="22" height="22" xmlns="http://www.w3.org/2000/svg"><g><rect x="2" y="14" width="3" height="6" fill="%23cccccc"/><rect x="7" y="10" width="3" height="10" fill="%23cccccc"/><rect x="12" y="6" width="3" height="14" fill="%23cccccc"/><rect x="17" y="2" width="3" height="18" fill="%23cccccc"/></g></svg>') no-repeat center/contain;
  filter: drop-shadow(0 0 4px #aaa);
}
.divider {
  width: 100%;
  height: 1.5px;
  background: linear-gradient(90deg, var(--ai-border) 0%, var(--tool-title-color) 50%, var(--ai-border) 100%);
  opacity: 0.18;
  margin: 18px 0 8px 0;
  border-radius: 1px;
}
.network-section {
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.section-title {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--tool-title-color);
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
  letter-spacing: 1px;
  justify-content: center;
}
.glow-btn {
  width: 100%;
  background: linear-gradient(90deg, #4e6bde 0%, #7b98d2 100%);
  color: #fff !important;
  border: none;
  border-radius: 12px;
  font-size: 1.08rem;
  font-weight: 600;
  padding: 10px 0;
  box-shadow: 0 2px 12px rgba(78,107,222,0.13);
  transition: box-shadow 0.2s, filter 0.2s, background 0.2s;
  margin-bottom: 8px;
}
.glow-btn:hover:not(:disabled) {
  filter: brightness(1.1) drop-shadow(0 0 8px #4e6bde);
  box-shadow: 0 4px 18px rgba(78,107,222,0.22);
}
.input-glass {
  background: rgba(120,140,220,0.08) !important;
  border-radius: 10px !important;
  border: 1.5px solid var(--ai-border) !important;
  color: var(--text-color) !important;
  box-shadow: 0 2px 8px rgba(78,107,222,0.06);
  transition: border 0.2s, box-shadow 0.2s;
}
.input-glass:focus {
  border: 2px solid var(--tool-title-color) !important;
  box-shadow: 0 4px 18px rgba(78,107,222,0.13);
}
.speed-values {
  display: flex;
  gap: 24px;
  margin-top: 8px;
}
.speed-card {
  background: linear-gradient(90deg, #e0e7ff 0%, #f0f4ff 100%);
  border-radius: 16px;
  box-shadow: 0 2px 8px rgba(78,107,222,0.08);
  padding: 18px 32px;
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 120px;
  font-size: 1.18rem;
  font-weight: 700;
  color: #2563eb;
  border: 1.5px solid var(--ai-border);
  position: relative;
}
.speed-card.down {
  background: linear-gradient(90deg, #4e6bde 0%, #7b98d2 100%);
  color: #fff;
}
.speed-card.up {
  background: linear-gradient(90deg, #10b981 0%, #7b98d2 100%);
  color: #fff;
}
.speed-label {
  font-size: 1.02rem;
  color: var(--tool-desc-color);
  margin-bottom: 4px;
}
.speed-value {
  font-size: 1.6rem;
  font-weight: bold;
  letter-spacing: 1px;
}
.unit {
  font-size: 1.1rem;
  color: #ffe066;
  margin-left: 2px;
}
.dns-delay-value, .nat-type-value, .ws-result, .latency-jitter-row {
  font-size: 1.15rem;
  margin-top: 6px;
  color: var(--tool-title-color);
  font-weight: 600;
}
.ice-candidates {
  margin-top: 4px;
  font-size: 1.05rem;
  color: var(--tool-desc-color);
}
.ice-type {
  display: inline-block;
  background: linear-gradient(90deg, #4e6bde 0%, #7b98d2 100%);
  color: #fff;
  border-radius: 8px;
  padding: 2px 10px;
  margin-right: 6px;
  font-size: 0.98rem;
  font-weight: 500;
  box-shadow: 0 1px 4px rgba(78,107,222,0.13);
}
.ws-result .ok, .port-result-item .ok {
  color: #10b981;
  text-shadow: 0 0 6px #10b98144;
}
.ws-result .fail, .port-result-item .fail {
  color: #f56c6c;
  text-shadow: 0 0 6px #f56c6c44;
}
.port-results {
  margin-top: 6px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px 24px;
}
.port-label {
  color: var(--tool-title-color);
  font-weight: 600;
  font-size: 1.08rem;
}
.highlight {
  color: #ffe066;
  font-weight: bold;
  font-size: 1.18em;
  text-shadow: 0 0 8px #ffe06644;
}
.chart-section {
  margin-top: 0;
  align-items: flex-start;
  gap: 10px;
}
.chart-glass {
  width: 100%;
  min-width: 260px;
  height: 320px;
  background: rgba(120,140,220,0.10);
  border-radius: 22px;
  box-shadow: 0 2px 12px rgba(78,107,222,0.10);
  border: 1.5px solid var(--ai-border);
  padding: 8px 8px 8px 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 8px;
  transition: background 0.3s;
}
[data-theme='dark'] .chart-glass {
  background: rgba(34,36,54,0.88);
  box-shadow: 0 2px 12px rgba(34,36,54,0.22);
}
.chart-box {
  width: 100%;
  height: 100%;
}
.latency-jitter-row {
  display: flex;
  gap: 32px;
  margin-bottom: 8px;
  color: var(--tool-title-color);
  font-size: 1.12rem;
}
@media (max-width: 1200px) {
  .network-card-row-4 .network-card,
  .network-summary-card {
    min-width: 160px;
    max-width: 100vw;
    flex: 1 1 160px;
  }
  .network-card-wide {
    min-width: unset;
    max-width: 100vw;
    padding: 14px 8px 10px 8px;
    border-radius: 12px;
  }
}
@media (max-width: 900px) {
  .network-card-row,
  .network-card-row-4,
  .network-card-row-wide {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  .network-card,
  .network-summary-card {
    min-width: unset;
    max-width: 100vw;
    padding: 14px 8px 10px 8px;
    border-radius: 12px;
  }
  .page-title {
    font-size: 1.5rem;
  }
  .network-summary {
    gap: 12px 8px;
  }
  .summary-item {
    min-width: 90px;
    font-size: 0.98rem;
    padding: 8px 8px;
    border-radius: 10px;
  }
  .section-title {
    font-size: 1.05rem;
  }
  .speed-card {
    padding: 10px 12px;
    font-size: 1.05rem;
    border-radius: 10px;
  }
  .chart-glass {
    height: 180px;
    border-radius: 12px;
  }
  .latency-jitter-row {
    gap: 12px;
    font-size: 1rem;
  }
  .network-summary-bar-wrap {
    max-width: 99vw;
    padding: 0;
  }
  .network-summary-bar {
    padding: 56px 24px 44px 24px;
    border-radius: 10px;
    min-height: 160px;
  }
  .network-summary-bar .summary-item {
    font-size: 1.8rem;
  }
  .network-cards-masonry {
    grid-template-columns: 1fr;
    gap: 16px;
    max-width: 99vw;
  }
  .network-card-wide {
    grid-column: 1 / span 1;
    padding: 14px 8px 10px 8px;
    border-radius: 12px;
  }
  .network-card {
    padding: 12px 6px 8px 6px;
    border-radius: 10px;
  }
}
</style> 