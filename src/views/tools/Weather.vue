<template>
  <div class="weather-page">
    <div class="location-bar">{{ weather.province || '--' }}，{{ weather.city || '--' }}</div>
    <div class="weather-glass-card">
      <div class="weather-main-content">
        <!-- 左侧主信息整体毛玻璃卡片 -->
        <div class="weather-info-glass-card">
          <div class="weather-info">
            <div class="weather-top-bar">
              <span class="location-bar-inline">{{ weather.province || '--' }}，{{ weather.city || '--' }}</span>
              <span v-if="weather.warning" class="weather-warning">⚠️ {{ weather.warning }}</span>
              <span class="weather-time">{{ nowTime }}</span>
            </div>
            <div class="weather-main-row">
              <div class="main-left">
                <span class="main-icon">{{ weather.icon || '⛅' }}</span>
                <span class="main-temp">{{ weather.temp || '--' }}</span>
                <span class="main-unit">°C</span>
              </div>
              <div class="main-info-block">
                <div class="main-type">{{ weather.text || '--' }}</div>
                <div v-if="weather.feelTemp && weather.feelTemp !== '--'" class="main-feel">体感温度 {{ weather.feelTemp }}°</div>
              </div>
            </div>
            <div class="main-summary">
              <span v-if="weather.desc && weather.desc !== '--'">{{ weather.desc }}</span>
            </div>
            <div class="main-indicators">
              <div v-for="item in mainIndicators" :key="item.label" class="indicator">
                <span>{{ item.value || '--' }}</span>
                <span class="label">{{ item.label }}</span>
              </div>
            </div>
            <!-- 新增：数据更新时间提示 -->
            <div class="update-time" style="margin-top:10px;color:#ffe066;font-size:0.98rem;">
              <span v-if="weather.time && weather.time !== '--'">数据发布时间：{{ weather.time || '--' }}</span>
              <span v-if="forecastReportTime && forecastReportTime !== '--'">，预报发布时间：{{ forecastReportTime || '--' }}</span>
            </div>
            <div v-if="errorMsg" style="color:#ffb4b4;font-size:1.08rem;margin-top:8px;">{{ errorMsg }}</div>
          </div>
        </div>
        <!-- 右侧地图 -->
        <div class="weather-map-area">
          <div id="amap-container" class="map-container"></div>
          <div class="map-overlay-bar">
            <span>地图服务：高德地图</span>
            <a href="https://ditu.amap.com/" target="_blank" rel="noopener">打开地图</a>
          </div>
        </div>
      </div>
    </div>
    <!-- 新增：未来几天预报区块 -->
    <div class="forecast-section">
      <div class="forecast-header">
        <span class="forecast-title">未来几天预报</span>
        <div class="forecast-switch">
          <button :class="{active: forecastViewType==='chart'}" @click="forecastViewType='chart'">
            <span class="icon">📈</span> 图表
          </button>
          <button :class="{active: forecastViewType==='list'}" @click="forecastViewType='list'">
            <span class="icon">☰</span> 列表
          </button>
        </div>
      </div>
      <div v-if="forecastViewType==='chart'">
        <!-- 新表头：每一列一个竖直单元格，三行合成一个大表格 -->
        <div class="forecast-header-table" v-if="forecast.length">
          <div class="custom-y-split-line"></div>
          <div v-for="(item, idx) in forecast" :key="item.date" class="forecast-header-col" :class="{last: idx === forecast.length-1}">
            <div class="header-cell date">{{ item.date }}</div>
            <div class="header-cell icon">{{ getWeatherIcon(item.dayweather) }}</div>
            <div class="header-cell temp">{{ item.daytemp }}°C</div>
          </div>
        </div>
        <div class="forecast-chart-wrap" style="position:relative;">
          <div ref="forecastChartRef" class="forecast-chart"></div>
          <div class="custom-x-split-lines" ref="customSplitLines"></div>
          <div class="custom-y-split-line"></div>
        </div>
      </div>
      <div v-else class="forecast-list forecast-list-flex">
        <!-- 左侧时间卡片 -->
        <div class="forecast-date-tabs-vertical">
          <div
            v-for="(item, idx) in forecast"
            :key="item.date"
            class="forecast-date-tab-vertical"
            :class="{active: idx === selectedForecastIdx}"
            @click="toggleDetail(idx)"
          >
            <div class="date">{{ item.date }}</div>

          </div>
        </div>
        <!-- 右侧详情卡片 -->
        <transition name="fade-slide">
          <div v-if="detailPanelVisible && forecast[selectedForecastIdx]" class="forecast-detail-panel-vertical">
            <div class="date">{{ forecast[selectedForecastIdx].date }} {{ forecast[selectedForecastIdx].week }}</div>
            <div class="weather-row">
              <span class="icon">{{ getWeatherIcon(forecast[selectedForecastIdx].dayweather) }}</span>
              <span class="temp">{{ forecast[selectedForecastIdx].daytemp }}°</span>
              <span class="desc">{{ forecast[selectedForecastIdx].dayweather }}</span>
            </div>
            <div class="weather-row night">
              <span class="icon">{{ getWeatherIcon(forecast[selectedForecastIdx].nightweather) }}</span>
              <span class="temp">{{ forecast[selectedForecastIdx].nighttemp }}°</span>
              <span class="desc">{{ forecast[selectedForecastIdx].nightweather }}</span>
            </div>
            <div class="wind">白天：{{ forecast[selectedForecastIdx].daywind }} {{ forecast[selectedForecastIdx].daypower }}级</div>
            <div class="wind">夜间：{{ forecast[selectedForecastIdx].nightwind }} {{ forecast[selectedForecastIdx].nightpower }}级</div>
          </div>
        </transition>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, computed, watch, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { request } from '@/utils/request'

// 1. 读取高德key（环境变量）
const AMAP_KEY = import.meta.env.VITE_AMAP_KEY || 'aac99aa500b26b630bfa3865933e44ac'

// 优化：初始化所有字段为兜底值，使用reactive保证响应式
const weather = reactive({
  province: '--',
  city: '--',
  icon: '⛅',
  temp: '--',
  text: '--',
  feelTemp: '--',
  time: '--',
  humidity: '--',
  winddirection: '--',
  windpower: '--',
  aqi: '--',
  aqiLevel: '--',
  desc: '--',
  warning: ''
})
const forecast = ref([])
const forecastReportTime = ref('--')
const chartRef = ref(null)
const tempChartRef = ref(null)
const windChartRef = ref(null)
const humidityChartRef = ref(null)
const aqiChartRef = ref(null)
let chart = null
const errorMsg = ref('')
const nowTime = ref('')
const forecastViewType = ref('chart')
const forecastChartRef = ref(null)
const customSplitLines = ref(null)
const selectedForecastIdx = ref(1)
const detailPanelVisible = ref(true)

// 实时本地时间
function updateNowTime() {
  const d = new Date()
  nowTime.value = d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit', hour12: false })
}
updateNowTime()
const intervalId = setInterval(updateNowTime, 1000)

const detailList = computed(() => [
  { label: '湿度', value: weather.humidity + '%' },
  { label: '风向', value: weather.winddirection },
  { label: '风力', value: weather.windpower + '级' },
  { label: '白天天气', value: forecast.value[0]?.dayweather || '--' },
  { label: '夜间天气', value: forecast.value[0]?.nightweather || '--' },
  { label: '白天温度', value: (forecast.value[0]?.daytemp || '--') + '°' },
  { label: '夜间温度', value: (forecast.value[0]?.nighttemp || '--') + '°' },
  { label: '白天风向/风力', value: (forecast.value[0]?.daywind || '--') + ' ' + (forecast.value[0]?.daypower || '--') + '级' },
  { label: '夜间风向/风力', value: (forecast.value[0]?.nightwind || '--') + ' ' + (forecast.value[0]?.nightpower || '--') + '级' },
  { label: '预报发布时间', value: forecastReportTime.value }
])

// 新增：更丰富的详情卡片
const detailList2 = computed(() => {
  const arr = [
    weather.aqi ? { label: '空气质量', value: weather.aqi } : null,
    { label: '风速', value: weather.winddirection + ' ' + weather.windpower + '级' },
    { label: '湿度', value: weather.humidity + '%' },
    forecast.value[0]?.dayweather ? { label: '白天天气', value: forecast.value[0].dayweather } : null,
    forecast.value[0]?.nightweather ? { label: '夜间天气', value: forecast.value[0].nightweather } : null,
    forecast.value[0]?.daytemp ? { label: '白天温度', value: forecast.value[0].daytemp + '°' } : null,
    forecast.value[0]?.nighttemp ? { label: '夜间温度', value: forecast.value[0].nighttemp + '°' } : null
  ]
  return arr.filter(Boolean)
})

function getWeatherIcon(text) {
  if (!text) return '⛅';
  if (text.includes('雨')) return '🌧️';
  if (text.includes('晴')) return '☀️';
  if (text.includes('雪')) return '❄️';
  if (text.includes('雾')) return '🌫️';
  if (text.includes('雷')) return '⛈️';
  if (text.includes('云') || text.includes('阴')) return '⛅';
  return '⛅';
}

function renderTempChart() {
  if (!tempChartRef.value || !forecast.value.length) return
  const chart = echarts.init(tempChartRef.value)
  chart.setOption({
    grid: { left: 30, right: 10, top: 20, bottom: 20 },
    xAxis: { type: 'category', data: forecast.value.map(f => f.date), axisLabel: { color: 'var(--tool-desc-color)' } },
    yAxis: { type: 'value', axisLabel: { color: 'var(--tool-desc-color)' } },
    series: [
      { name: '高温', type: 'line', data: forecast.value.map(f => Number(f.daytemp)), smooth: true, lineStyle: { color: '#ef4444', width: 3 } },
      { name: '低温', type: 'line', data: forecast.value.map(f => Number(f.nighttemp)), smooth: true, lineStyle: { color: '#3b82f6', width: 3 } }
    ]
  })
}
function renderWindChart() {
  if (!windChartRef.value) return
  const chart = echarts.init(windChartRef.value)
  chart.setOption({
    series: [{
      type: 'gauge',
      min: 0, max: 12, splitNumber: 6,
      axisLine: { lineStyle: { width: 10, color: [[0.5, '#4e6bde'], [1, '#b3d0f7']] } },
      pointer: { width: 5 },
      detail: { formatter: '{value}级', fontSize: 18, color: 'var(--tool-title-color)' },
      data: [{ value: Number(weather.windpower) || 0, name: '风力' }],
      title: { show: false }
    }]
  })
}
function renderHumidityChart() {
  if (!humidityChartRef.value) return
  const chart = echarts.init(humidityChartRef.value)
  chart.setOption({
    grid: { left: 20, right: 10, top: 20, bottom: 20 },
    xAxis: { type: 'category', data: ['当前'], axisLabel: { color: 'var(--tool-desc-color)' } },
    yAxis: { type: 'value', max: 100, axisLabel: { color: 'var(--tool-desc-color)' } },
    series: [{ type: 'bar', data: [Number(weather.humidity) || 0], itemStyle: { color: '#4e6bde' }, barWidth: 24 }]
  })
}
function renderAqiChart() {
  if (!aqiChartRef.value || !weather.aqi) return
  const chart = echarts.init(aqiChartRef.value)
  chart.setOption({
    series: [{
      type: 'gauge',
      startAngle: 225, endAngle: -45,
      min: 0, max: 300,
      axisLine: { lineStyle: { width: 14, color: [[0.3, '#10b981'], [0.6, '#facc15'], [1, '#ef4444']] } },
      pointer: { show: false },
      progress: { show: true, width: 14 },
      detail: { valueAnimation: true, fontSize: 22, color: 'var(--tool-title-color)' },
      data: [{ value: Number(weather.aqi), name: 'AQI' }],
      title: { show: false }
    }]
  })
}

onMounted(async () => {
  // 1. 定位
  let locData
  let city = '--'
  let adcode = ''
  try {
    const { data: locRes, error: locError } = await request({ url: `https://restapi.amap.com/v3/ip?key=${AMAP_KEY}` })
    if (locError) throw new Error(locError.message || '定位失败')
    locData = locRes
    console.log('定位数据', locData)
    if (locData.status !== '1') throw new Error(locData.info || '定位失败')
    city = locData.city || '--'
    adcode = locData.adcode
    weather.city = locData.city || '--'
    weather.province = locData.province || '--'
  } catch (e) {
    console.error('定位失败', e)
    weather.city = '--'
    weather.province = '--'
    errorMsg.value = '定位失败，无法获取天气数据。'
  }
  // 2. 实况天气
  if (adcode) {
    try {
      const { data: nowRes, error: nowError } = await request({ url: `https://restapi.amap.com/v3/weather/weatherInfo?city=${adcode}&key=${AMAP_KEY}&extensions=base` })
      if (nowError) throw new Error(nowError.message || '获取实况天气失败')
      if (nowRes.status !== '1') throw new Error(nowRes.info || '获取实况天气失败')
      if (nowRes.lives && nowRes.lives.length > 0) {
        const now = nowRes.lives[0]
        weather.temp = now.temperature || '--'
        weather.text = now.weather || '--'
        weather.icon = getWeatherIcon(now.weather)
        weather.feelTemp = now.temperature || '--'
        weather.time = now.reporttime ? now.reporttime.slice(11, 16) : '--'
        weather.humidity = now.humidity || '--'
        weather.winddirection = now.winddirection || '--'
        weather.windpower = now.windpower || '--'
        weather.aqi = now.aqi || '--'
        weather.desc = (now.weather ? now.weather + '。' : '') + (now.temperature ? '当前气温' + now.temperature + '°。' : '')
        console.log('weather', JSON.stringify(weather))
      } else {
        errorMsg.value = '天气数据获取失败';
      }
    } catch (e) {
      console.error('获取实况天气失败', e)
      weather.temp = '--'
      weather.text = '--'
      weather.icon = '⛅'
      weather.feelTemp = '--'
      weather.time = '--'
      weather.humidity = '--'
      weather.winddirection = '--'
      weather.windpower = '--'
      weather.aqi = '--'
      weather.desc = '--'
      errorMsg.value = '获取实况天气失败，请稍后重试。'
    }
    // 2.5 获取天气预警
    try {
      const { data: warningRes, error: warningError } = await request({ url: `https://restapi.amap.com/v3/weather/warning?city=${adcode}&key=${AMAP_KEY}` })
      if (warningError) throw new Error(warningError.message || '获取天气预警失败')
      if (warningRes.status === '1' && warningRes.warnings && warningRes.warnings.length > 0) {
        // 只显示第一条预警
        const w = warningRes.warnings[0]
        weather.warning = `${w.title || ''}${w.typeName ? ' - ' + w.typeName : ''}${w.level ? ' - ' + w.level + '预警' : ''}`
      } else {
        weather.warning = ''
      }
    } catch (e) {
      console.error('获取天气预警失败', e)
      weather.warning = ''
    }
    // 3. 预报天气
    try {
      const { data: forecastRes, error: forecastError } = await request({ url: `https://restapi.amap.com/v3/weather/weatherInfo?city=${adcode}&key=${AMAP_KEY}&extensions=all` })
      if (forecastError) throw new Error(forecastError.message || '获取预报天气失败')
      if (forecastRes.status !== '1') throw new Error(forecastRes.info || '获取预报天气失败')
      if (forecastRes.forecasts && forecastRes.forecasts.length > 0) {
        forecast.value = forecastRes.forecasts[0].casts
        forecastReportTime.value = forecastRes.forecasts[0].reporttime || '--'
      } else {
        forecast.value = []
        forecastReportTime.value = '--'
        errorMsg.value = '预报天气数据获取失败';
      }
    } catch (e) {
      console.error('获取预报天气失败', e)
      forecast.value = []
      forecastReportTime.value = '--'
      errorMsg.value = '获取预报天气失败，请稍后重试。'
    }
  }
  await nextTick()
  // 4. 初始化高德地图
  if (!window.AMap) {
    const script = document.createElement('script')
    script.src = `https://webapi.amap.com/maps?v=2.0&key=${AMAP_KEY}`
    script.onload = () => nextTick(() => initMap(city))
    document.body.appendChild(script)
  } else {
    nextTick(() => initMap(city))
  }
  // 5. 渲染ECharts
  renderChart()
  nextTick(() => {
    renderTempChart()
    renderWindChart()
    renderHumidityChart()
    renderAqiChart()
  })
})

function renderChart() {
  if (!chartRef.value) return
  if (!forecast.value || forecast.value.length === 0) return
  if (!chart) chart = echarts.init(chartRef.value)
  const xData = forecast.value.map(i => i.date + '\n' + i.week)
  const high = forecast.value.map(i => Number(i.daytemp))
  const low = forecast.value.map(i => Number(i.nighttemp))
  const humidity = forecast.value.map(() => Number(weather.humidity)) // 预报接口无湿度，暂用实况
  const wind = forecast.value.map(i => parseInt(i.daypower))
  chart.setOption({
    backgroundColor: 'transparent',
    tooltip: { 
      trigger: 'axis',
      axisPointer: {
        type: 'none'
      }
    },
    legend: { data: ['白天高温', '夜间低温', '湿度', '白天风力'], textStyle: { color: 'var(--tool-title-color)' } },
    grid: { left: 40, right: 20, top: 30, bottom: 30 },
    xAxis: [
      {
        type: 'category',
        data: xData,
        axisLine: { lineStyle: { color: 'var(--border-color)' } },
        axisLabel: { color: 'var(--tool-desc-color)' },
        boundaryGap: false,
        splitLine: { show: false }
      },
      {
        type: 'category',
        data: xData,
        boundaryGap: true,
        axisLine: { lineStyle: { color: 'var(--border-color)' } },
        axisLabel: { color: 'var(--tool-desc-color)' },
        splitLine: { show: true, lineStyle: { color: 'rgba(0,0,0,0.1)', type: 'solid' } }
      }
    ],
    yAxis: [
      {
        type: 'value',
        name: '温度(°C)',
        min: Math.min(...low) - 2,
        max: Math.max(...high) + 2,
        axisLine: {
          show: true,
          lineStyle: {
            color: 'rgba(255,255,255,0.18)',
            width: 2
          }
        },
        axisLabel: {
          color: '#666'
        },
        splitLine: { lineStyle: { color: 'var(--border-color)', type: 'solid' } }
      },
      {
        type: 'value',
        name: '湿度(%)',
        min: 0,
        max: 100,
        axisLine: { lineStyle: { color: 'var(--border-color)' } },
        axisLabel: { color: 'var(--tool-desc-color)' },
        splitLine: { show: false }
      },
      {
        type: 'value',
        name: '风力(级)',
        min: 0,
        max: 12,
        axisLine: { lineStyle: { color: 'var(--border-color)' } },
        axisLabel: { color: 'var(--tool-desc-color)' },
        splitLine: { show: false }
      }
    ],
    series: [
      {
        name: '高温',
        type: 'line',
        smooth: true,
        data: high,
        yAxisIndex: 0,
        lineStyle: { color: '#ef4444', width: 3 },
        areaStyle: { color: 'rgba(239,68,68,0.08)' }
      },
      {
        name: '低温',
        type: 'line',
        smooth: true,
        data: low,
        yAxisIndex: 0,
        lineStyle: { color: '#3b82f6', width: 3 },
        areaStyle: { color: 'rgba(59,130,246,0.08)' }
      },
      {
        name: '湿度',
        type: 'bar',
        data: humidity,
        yAxisIndex: 1,
        barWidth: 12,
        itemStyle: { color: '#a6c8ff' }
      },
      {
        name: '白天风力',
        type: 'bar',
        data: wind,
        yAxisIndex: 2,
        barWidth: 12,
        itemStyle: { color: '#10b981' }
      }
    ]
  })
}

function initMap(city) {
  const mapDiv = document.getElementById('amap-container');
  if (!mapDiv) return;
  if (window._amapInstance) {
    window._amapInstance.destroy();
  }
  window._amapInstance = new window.AMap.Map('amap-container', {
    resizeEnable: true,
    zoom: 11,
    city: city
  })
  // 可扩展：添加天气图层、预警等
}

// 自动渲染/更新图表
watch([forecast, () => weather.temp], () => nextTick(() => renderTempChart()))
watch(() => weather.windpower, () => nextTick(() => renderWindChart()))
watch(() => weather.humidity, () => nextTick(() => renderHumidityChart()))
watch(() => weather.aqi, () => nextTick(() => renderAqiChart()))

// 主信息区下方指标动态生成，只显示高德API真实返回的数据字段
const mainIndicators = computed(() => {
  const arr = []
  // 只用我们有的数据
  if (weather.winddirection) arr.push({ label: '风向', value: weather.winddirection })
  if (weather.windpower) arr.push({ label: '风力', value: weather.windpower + '级' })
  if (weather.humidity) arr.push({ label: '湿度', value: weather.humidity + '%' })
  // 预报接口字段
  if (forecast.value[0]?.dayweather) arr.push({ label: '白天天气', value: forecast.value[0].dayweather })
  if (forecast.value[0]?.nightweather) arr.push({ label: '夜间天气', value: forecast.value[0].nightweather })
  if (forecast.value[0]?.daytemp) arr.push({ label: '白天温度', value: forecast.value[0].daytemp + '°' })
  if (forecast.value[0]?.nighttemp) arr.push({ label: '夜间温度', value: forecast.value[0].nighttemp + '°' })
  if (forecast.value[0]?.daywind && forecast.value[0]?.daypower) arr.push({ label: '白天风', value: forecast.value[0].daywind + ' ' + forecast.value[0].daypower + '级' })
  if (forecast.value[0]?.nightwind && forecast.value[0]?.nightpower) arr.push({ label: '夜间风', value: forecast.value[0].nightwind + ' ' + forecast.value[0].nightpower + '级' })
  return arr.slice(0, 6)
})

// 预报视图切换

function toggleDetail(idx) {
  if (selectedForecastIdx.value === idx) {
    detailPanelVisible.value = !detailPanelVisible.value
  } else {
    selectedForecastIdx.value = idx
    detailPanelVisible.value = true
  }
}

function drawCustomSplitLines() {
  const chartEl = forecastChartRef.value
  const overlay = customSplitLines.value
  if (!chartEl || !overlay) return
  overlay.innerHTML = ''
  const colCount = forecast.value.length
  if (colCount < 2) return
  const chartRect = chartEl.getBoundingClientRect()
  const yAxisWidth = 50 // 统一常量
  const colWidth = (chartRect.width - yAxisWidth) / colCount
  for (let i = 0; i <= colCount; i++) {
    const left = i * colWidth
    const line = document.createElement('div')
    line.style.position = 'absolute'
    line.style.left = `${left}px`
    line.style.top = '0'
    line.style.height = '100%'
    line.style.width = '0'
    line.style.borderLeft = '1px solid rgba(0,0,0,0.1)'
    line.style.pointerEvents = 'none'
    overlay.appendChild(line)
  }
  overlay.style.position = 'absolute'
  overlay.style.left = `${yAxisWidth}px`
  overlay.style.top = 0
  overlay.style.width = (chartRect.width - yAxisWidth) + 'px'
  overlay.style.height = chartRect.height + 'px'
  overlay.style.pointerEvents = 'none'
  overlay.style.zIndex = 2
}

onMounted(() => {
  nextTick(drawCustomSplitLines)
  window.addEventListener('resize', drawCustomSplitLines)
})
onUnmounted(() => {
  clearInterval(intervalId)
  window.removeEventListener('resize', drawCustomSplitLines)
})
watch(forecast, () => nextTick(drawCustomSplitLines))

function renderForecastChart() {
  if (!forecastChartRef.value || !forecast.value.length) return
  const chart = echarts.init(forecastChartRef.value)
  const icons = forecast.value.map(f => getWeatherIcon(f.dayweather))
  const highTemps = forecast.value.map(f => Number(f.daytemp))
  const lowTemps = forecast.value.map(f => Number(f.nighttemp))
  const dates = forecast.value.map(f => f.date)
  chart.setOption({
    backgroundColor: 'transparent',
    grid: {
      left: 50,
      right: 0,
      top: 16,
      bottom: 60,
      containLabel: false,
      show: false
    },
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255,255,255,0.9)',
      borderRadius: 18,
      borderWidth: 1,
      borderColor: 'rgba(0,0,0,0.1)',
      padding: 0,
      extraCssText: 'backdrop-filter: blur(10px); box-shadow: 0 4px 18px rgba(0,0,0,0.1);',
      textStyle: { color: '#333', fontSize: 16 },
      formatter: params => {
        const idx = params[0].dataIndex
        const f = forecast.value[idx]
        return `
          <div style=\"padding:18px 22px 14px 22px; border-radius:18px; text-align:center; min-width:90px;\">
            <div style=\"font-size:1.08rem;color:#666;margin-bottom:2px;\">${f.date}</div>
            <div style=\"font-size:2.1rem;line-height:2.2rem;\">${getWeatherIcon(f.dayweather)}</div>
            <div style=\"font-size:1.5rem;font-weight:bold;color:#333;\">${f.daytemp}°C</div>
            <div style=\"font-size:1.08rem;color:#666;\">${f.dayweather}</div>
          </div>
        `
      },
      axisPointer: {
        type: 'none'
      }
    },
    xAxis: [
      {
        type: 'category',
        data: dates,
        boundaryGap: false,
        axisLabel: { show: false },
        axisLine: { lineStyle: { color: 'rgba(0,0,0,0.15)' } },
        axisTick: { show: false },
        splitLine: { show: false },
        axisPointer: { show: false }
      },
      {
        type: 'category',
        data: dates,
        boundaryGap: true,
        position: 'bottom',
        offset: 0,
        axisLabel: {
          show: true,
          color: '#666',
          fontSize: 15,
          align: 'center'
        },
        axisLine: { show: false },
        axisTick: { show: false },
        splitLine: { 
          show: true, 
          lineStyle: { 
            color: 'rgba(0,0,0,0.1)', 
            type: 'solid',
            width: 1
          }
        },
        axisPointer: {
          type: 'none'
        }
      }
    ],
    yAxis: {
      type: 'value',
      min: Math.min(...lowTemps) - 2,
      max: Math.max(...highTemps) + 2,
      axisLabel: {
        color: '#666',
        fontSize: 13
      },
      splitLine: { lineStyle: { color: 'rgba(0,0,0,0.08)', type: 'solid' } },
      axisLine: {
        show: true,
        lineStyle: {
          color: 'rgba(0,0,0,0.15)',
          width: 1
        }
      },
    },
    series: [
      {
        name: '高温',
        type: 'line',
        smooth: true,
        data: highTemps,
        xAxisIndex: 0,
        symbol: 'none',
        lineStyle: { color: '#f97316', width: 3 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(249,115,22,0.3)' },
            { offset: 1, color: 'rgba(249,115,22,0.05)' }
          ])
        },
        z: 3
      },
      {
        name: '低温',
        type: 'line',
        smooth: true,
        data: lowTemps,
        xAxisIndex: 0,
        symbol: 'none',
        lineStyle: { color: '#3b82f6', width: 3 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(59,130,246,0.3)' },
            { offset: 1, color: 'rgba(59,130,246,0.05)' }
          ])
        },
        z: 2
      }
    ]
  })
}

watch([forecast, forecastViewType], () => {
  if (forecastViewType.value === 'chart') {
    nextTick(() => {
      renderForecastChart();
      drawCustomSplitLines(); // 关键：每次切回图表都重绘虚线
    });
  }
});
</script>

<style scoped>
.weather-page {
  padding: 80px 0 0 0;
  min-height: 100vh;
  background: var(--bg-color);
  display: flex;
  flex-direction: column;
  align-items: center;
}
.location-bar {
  margin-bottom: 24px;
  padding: 10px 32px;
  background: var(--card-bg);
  border-radius: 16px;
  color: var(--text-color);
  font-size: 1.15rem;
  font-weight: 500;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
  display: none;
}
.location-bar-inline {
  display: inline-block;
  background: var(--card-bg);
  border-radius: 12px;
  color: var(--text-color);
  font-size: 1.08rem;
  font-weight: 500;
  padding: 2px 18px;
  margin-right: 16px;
  vertical-align: middle;
}
.weather-top-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: relative;
  margin-bottom: 8px;
}
.weather-warning {
  margin-right: auto;
}
.weather-time {
  position: absolute;
  right: 0;
  top: 0;
  font-size: 1.08rem;
  color: #ffe066;
  background: none;
  padding: 0;
  font-weight: 500;
}
.weather-glass-card {
  width: 100%;
  max-width: 1200px;
  margin: 0 32px;
  border-radius: 28px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.1);
  background: var(--card-bg);
  backdrop-filter: blur(28px) saturate(1.7);
  border: 1px solid rgba(0,0,0,0.1);
  padding: 36px 40px 24px 40px;
  display: flex;
  flex-direction: column;
  gap: 18px;
  margin-bottom: 32px;
}
.weather-main-content {
  display: flex;
  gap: 32px;
  align-items: stretch; /* 顶部对齐且底部对齐 */
}
.weather-info-glass-card {
  background: var(--card-bg);
  backdrop-filter: blur(22px) saturate(1.5);
  border-radius: 22px;
  box-shadow: 0 4px 18px rgba(0,0,0,0.08);
  border: 1px solid rgba(0,0,0,0.1);
  padding: 32px 32px 18px 32px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: stretch;
  min-width: 340px;
  max-width: 100%;
  flex: 1;
}
.weather-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  color: var(--text-color);
}
.weather-main-row {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 2px;
  margin-bottom: 13px;
  font-size: 2.3rem;
}
.main-left {
  display: flex;
  align-items: flex-end;
  gap: 2px;
}
.main-icon {
  font-size: 4.2rem;
  margin-right: 0;
}
.main-temp {
  font-size: 4.6rem;
  font-weight: bold;
  margin-right: 0;
}
.main-unit {
  font-size: 2.6rem;
  color: var(--text-color);
  opacity: 0.7;
  margin-right: 0;
}
.main-info-block {
  display: flex;
  flex-direction: column;
  gap: 10px;
  font-size: 2.2rem;
  color: var(--text-color);
  align-items: flex-end;
  text-align: right;
}
.main-type {
  font-size: 2.2rem;
  font-weight: 500;
  color: var(--text-color);
}
.main-feel {
  font-size: 1.2rem;
  color: var(--tool-title-color);
}
.main-summary {
  font-size: 1.1rem;
  color: var(--text-color);
  opacity: 0.8;
  margin: 10px 0 18px 0;
}
.main-indicators {
  display: flex;
  gap: 18px;
  margin-top: 8px;
  flex-wrap: wrap;
}
.indicator {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 70px;
  font-size: 1.08rem;
  color: var(--text-color);
  border-radius: 8px;
}
.indicator .label {
  font-size: 0.98rem;
  color: var(--tool-title-color);
  margin-top: 2px;
}
.weather-map-area {
  flex: 1;
  min-width: 320px;
  display: flex;
  flex-direction: column;
  align-items: stretch;
  justify-content: flex-start;
  position: relative;
  margin: 0;
  padding: 0;
}
.map-container {
  width: 100%;
  height: 260px;
  border-radius: 22px 22px 0 0;
  box-shadow: 0 2px 12px rgba(59,130,246,0.10);
  background: #eaeaea;
  margin: 0;
  padding: 0;
}
.map-overlay-bar {
  background: rgba(40, 50, 80, 0.22);
  backdrop-filter: blur(22px) saturate(1.5);
  border: 1.2px solid rgba(255,255,255,0.13);
  border-radius: 0 0 22px 22px;
  width: inherit;
  max-width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 28px 0 28px;
  color: #fff;
  font-size: 1.08rem;
  box-shadow: 0 2px 12px rgba(59,130,246,0.10);
  margin: 0;
  flex: 1;
  min-height: 54px;
}
.map-overlay-bar a {
  color: #7ec7ff;
  font-size: 1.08rem;
  text-decoration: none;
  font-weight: bold;
  transition: color 0.2s;
}
.map-overlay-bar a:hover {
  color: #fff;
}
@media (max-width: 900px) {
  .weather-page { padding: 56px 0 0 0; }
  .weather-glass-card { padding: 18px 8px 12px 8px; border-radius: 16px; }
  .weather-main-content { flex-direction: column; gap: 12px; }
  .weather-map-area { min-width: 0; }
  .map-container { height: 180px; border-radius: 0; }
  .map-overlay-bar { height: 40px; font-size: 0.98rem; padding: 0 12px; border-radius: 0; margin: 0; }
  .weather-main-row { font-size: 1.1rem; gap: 8px; margin-bottom: 5px; }
  .main-icon { font-size: 2.2rem; }
  .main-temp { font-size: 2.4rem; }
  .main-unit { font-size: 1.1rem; }
  .main-info-block { font-size: 0.95rem; gap: 3px; }
  .main-type { font-size: 0.95rem; }
  .main-feel { font-size: 0.9rem; }
  .main-indicators { gap: 18px; }
  .indicator { font-size: 0.98rem; min-width: 50px; border-radius: 6px; }
  .indicators-glass-card {
    padding: 10px 4px 6px 4px;
    border-radius: 10px;
    min-width: unset;
    width: 100%;
  }
  .weather-info-glass-card {
    padding: 16px 6px 10px 6px;
    border-radius: 12px;
    min-width: unset;
    width: 100%;
  }
}

/* 未来几天预报区块样式 */
.forecast-section {
  margin-top: 32px;
  width: 100%;
  max-width: 1200px;
  margin: 0 32px;
  border-radius: 28px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.1);
  background: var(--card-bg);
  backdrop-filter: blur(28px) saturate(1.7);
  border: 1px solid rgba(0,0,0,0.1);
  padding: 36px 40px 24px 40px;
  display: flex;
  flex-direction: column;
  gap: 18px;
}
.forecast-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.forecast-title {
  font-size: 1.25rem;
  color: var(--text-color);
  font-weight: 600;
}
.forecast-switch {
  display: flex;
  gap: 8px;
}
.forecast-switch button {
  background: none;
  border: none;
  color: var(--text-color);
  opacity: 0.7;
  font-size: 1.08rem;
  padding: 4px 16px;
  border-radius: 12px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
  font-weight: 500;
  transition: all 0.2s;
}
.forecast-switch button.active {
  color: var(--text-color);
  opacity: 1;
  border: 2px solid var(--text-color);
  background: var(--card-bg);
}
.forecast-header-table {
  position: relative;
  padding-left: 50px;
  box-sizing: border-box;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(0, 1fr));
  border-radius: 18px 18px 0 0;
  border: 1px solid rgba(0,0,0,0.1);
  border-bottom: 0 !important;
  margin-bottom: 0 !important;
  padding-bottom: 0 !important;
  box-sizing: border-box;
  line-height: normal;
  overflow: hidden;
  z-index: 3;
  box-shadow: none !important;
}
.forecast-header-col {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: stretch;
  border-right: 1px solid rgba(0,0,0,0.1);
}
.forecast-header-col.last {
  border-right: none;
}
.header-cell {
  width: 100%;
  text-align: center;
  color: var(--text-color);
  opacity: 0.8;
  font-size: 1.08rem;
  font-weight: 500;
  padding: 6px 0 4px 0;
  background: none;
  border-bottom: none;
}
.header-cell.date {
  font-size: 1.08rem;
  color: var(--text-color);
  font-weight: 500;
  border-top: none;
}
.header-cell.icon {
  font-size: 1.5rem;
  padding: 2px 0 2px 0;
  border-bottom: none;
}
.header-cell.temp {
  color: var(--tool-title-color);
  font-size: 1.12rem;
  font-weight: bold;
  border-bottom: none !important;
}
.forecast-chart {
  width: 100%;
  height: 320px;
  background: transparent;
  border: 1px solid rgba(0,0,0,0.1);
  border-top: 0 !important;
  border-radius: 0 0 18px 18px;
  box-shadow: none !important;
  position: relative;
  box-sizing: border-box;
  margin-top: -3px;
  line-height: normal;
  z-index: 2;
  top: -1px;
}
.forecast-list {
  display: flex;
  gap: 18px;
  overflow-x: auto;
  padding-bottom: 8px;
}
.forecast-list-flex {
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  gap: 48px;
  min-height: 420px;
  justify-content: center;
}
.forecast-card {
  min-width: 160px;
  background: rgba(60, 60, 90, 0.18);
  border-radius: 14px;
  box-shadow: 0 2px 8px rgba(30,34,54,0.08);
  padding: 14px 12px 10px 12px;
  color: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  font-size: 1.08rem;
}
.forecast-card .date {
  font-size: 1.05rem;
  color: #ffe066;
  margin-bottom: 6px;
}
.forecast-card .weather-row {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 2px;
}
.forecast-card .weather-row.night {
  opacity: 0.85;
}
.forecast-card .icon {
  font-size: 1.5rem;
}
.forecast-card .temp {
  font-size: 1.3rem;
  font-weight: 600;
}
.forecast-card .desc {
  font-size: 1.08rem;
}
.forecast-card .wind {
  font-size: 0.98rem;
  color: #bfcfff;
  margin-top: 2px;
}
.forecast-chart-wrap {
  position: relative;
  margin-top: 0;
  padding-top: 0;
}
.custom-x-split-lines {
  position: absolute;
  left: 0; top: 0; width: 100%; height: 100%;
  pointer-events: none;
  z-index: 2;
}
.custom-y-split-line {
  position: absolute;
  left: 50px;
  top: 0;
  width: 0;
  height: 100%;
  border-left: 1px solid rgba(0,0,0,0.1);
  pointer-events: none;
  z-index: 10;
}
.forecast-date-tabs {
  display: flex;
  gap: 18px;
  margin-bottom: 18px;
  justify-content: center;
}
.forecast-date-tabs-scroll {
  overflow-x: auto;
  padding-bottom: 8px;
  margin-bottom: 0;
}
.forecast-date-tab {
  min-width: 120px;
  background: rgba(60, 60, 90, 0.18);
  border-radius: 14px;
  box-shadow: 0 2px 8px rgba(30,34,54,0.08);
  padding: 14px 12px 10px 12px;
  color: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  font-size: 1.08rem;
  cursor: pointer;
  border: 2px solid transparent;
  transition: border 0.2s, background 0.2s;
}
.forecast-date-tab.active {
  border: 2px solid #ffe066;
  background: rgba(60, 60, 90, 0.32);
  color: #ffe066;
}
.forecast-date-tab .date {
  font-size: 1.12rem;
  font-weight: bold;
  margin-bottom: 2px;
}
.forecast-date-tab .week {
  font-size: 1.02rem;
  opacity: 0.85;
}
.forecast-card-large {
  min-width: 240px;
  max-width: 340px;
  margin: 0 auto;
  font-size: 1.18rem;
  padding: 22px 18px 16px 18px;
  border-radius: 18px;
  box-shadow: 0 4px 18px rgba(30,34,54,0.12);
  background: rgba(60, 60, 90, 0.22);
  color: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.forecast-detail-panel {
  min-width: 240px;
  max-width: 340px;
  margin: 18px auto 0 auto;
  font-size: 1.18rem;
  padding: 22px 18px 16px 18px;
  border-radius: 18px;
  box-shadow: 0 4px 18px rgba(30,34,54,0.12);
  background: rgba(60, 60, 90, 0.22);
  color: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  transition: all 0.25s cubic-bezier(.4,0,.2,1);
}
.fade-slide-enter-active, .fade-slide-leave-active {
  transition: all 0.25s cubic-bezier(.4,0,.2,1);
}
.fade-slide-enter-from, .fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-16px);
}
.forecast-info-card {
  width: 100%;
  max-width: 320px;
  margin: 18px auto 0 auto;
  background: rgba(40, 50, 80, 0.18);
  border-radius: 14px;
  box-shadow: 0 2px 8px rgba(30,34,54,0.10);
  padding: 18px 22px 12px 22px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  align-items: flex-start;
}
.info-row {
  display: flex;
  justify-content: space-between;
  width: 100%;
  font-size: 1.08rem;
  color: #e3eaf6;
}
.info-label {
  color: #bfcfff;
  font-weight: 500;
}
.info-value {
  color: #ffe066;
  font-weight: bold;
}
.forecast-date-tabs-vertical {
  display: flex;
  flex-direction: column;
  gap: 18px;
  min-width: 160px;
  margin-top: 0;
  align-items: flex-end;
}
.forecast-date-tab-vertical {
  background: var(--card-bg);
  border-radius: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  padding: 18px 24px;
  color: var(--text-color);
  font-size: 1.12rem;
  cursor: pointer;
  border: 2px solid transparent;
  transition: all 0.2s;
  margin-bottom: 6px;
  min-width: 120px;
  text-align: center;
}
.forecast-date-tab-vertical.active {
  border: 2px solid var(--tool-title-color);
  background: var(--bg-color);
  color: var(--tool-title-color);
  box-shadow: 0 4px 16px rgba(78,107,222,0.10);
}
.forecast-date-tab-vertical:hover {
  background: var(--tool-title-color);
  color: #fff;
}
.forecast-detail-panel-vertical {
  min-width: 320px;
  max-width: 420px;
  margin: 0 0 0 32px;
  font-size: 1.18rem;
  padding: 32px 32px 22px 32px;
  border-radius: 22px;
  box-shadow: 0 4px 18px rgba(0,0,0,0.10);
  background: var(--card-bg);
  color: var(--text-color);
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  border: 1px solid rgba(0,0,0,0.08);
  gap: 10px;
}
.forecast-detail-panel-vertical .weather-row {
  display: flex;
  align-items: center;
  gap: 18px;
  margin-bottom: 8px;
}
.forecast-detail-panel-vertical .icon {
  font-size: 2.2rem;
}
.forecast-detail-panel-vertical .temp {
  font-size: 2rem;
  font-weight: 600;
  color: var(--tool-title-color);
}
.forecast-detail-panel-vertical .desc {
  font-size: 1.2rem;
  color: var(--tool-desc-color);
}
.forecast-detail-panel-vertical .wind {
  font-size: 1.08rem;
  color: var(--tool-desc-color);
  margin-top: 6px;
  border-top: 1px dashed var(--tool-title-color);
  padding-top: 6px;
  width: 100%;
}
@media (max-width: 900px) {
  .forecast-list-flex {
    flex-direction: column;
    gap: 24px;
    align-items: stretch;
    padding: 10px 0;
  }
  .forecast-detail-panel-vertical {
    margin: 24px 0 0 0;
    min-width: unset;
    max-width: 100%;
    padding: 24px 10px 18px 10px;
    border-radius: 18px;
    font-size: 1.08rem;
    gap: 10px;
  }
  .forecast-date-tabs-vertical {
    flex-direction: row;
    gap: 10px;
    min-width: unset;
    align-items: flex-start;
    justify-content: center;
  }
  .forecast-date-tab-vertical {
    min-width: 90px;
    padding: 12px 8px;
    font-size: 1rem;
    border-radius: 12px;
  }
}
</style>