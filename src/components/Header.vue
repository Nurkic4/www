// Header.vue 的类型声明
// @ts-nocheck
<template>
  <header class="header-blur">
    <div class="header-container">
      <div class="logo" @click="goHome">
        <span class="logo-box">QQ</span>
        <span class="logo-text">青青空间</span>
      </div>
      <nav class="nav-menu" v-if="!isMobile && adminMenu">
        <router-link
          v-for="item in adminMenu || []"
          :key="item && item.name"
          :to="item.link"
          class="nav-link"
          :class="{ active: isActive(item.link) }"
        >
          {{ item.name }}
        </router-link>
        <div class="nav-link tools-dropdown-trigger" @mouseenter="showTools = true" @mouseleave="showTools = false">
          <span :class="{ active: isActive('/tools') }">工具箱</span>
          <transition name="tools-dropdown-fade">
            <div v-show="showTools" class="tools-dropdown-card" @mouseenter="showTools = true" @mouseleave="showTools = false">
              <div class="tools-grid">
                <div v-for="tool in toolsList" :key="tool.name" class="tool-item" @click="goTool(tool.path)">
                  <div class="tool-title">{{ tool.title }}</div>
                </div>
              </div>
            </div>
          </transition>
        </div>
      </nav>
      <div class="header-right-group">
        <ThemeSwitch class="theme-switch-right" :is-night="isNight" @toggle="$emit('toggle', $event)" />
        <UserDropdown class="user-dropdown-right" />
      </div>
      <div class="menu-btn" v-if="isMobile" @click="drawer = true">
        <svg width="28" height="28" viewBox="0 0 24 24"><path fill="currentColor" d="M3 6h18M3 12h18M3 18h18"/></svg>
      </div>
      <transition name="drawer-fade">
        <div class="mobile-drawer" v-if="drawer" @click.self="drawer = false">
          <div class="drawer-content">
            <div class="drawer-header">
              <span>菜单</span>
              <span class="close-btn" @click="drawer = false">&times;</span>
            </div>
            <a v-for="item in adminMenu || []" :key="item && item.name" :href="item.link" class="drawer-link" @click="drawer = false">
              {{ item && item.name }}
            </a>
          </div>
        </div>
      </transition>
    </div>
  </header>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import ThemeSwitch from './ThemeSwitch.vue'
import UserDropdown from './UserDropdown.vue'

const props = defineProps({
  isNight: Boolean
})
const emit = defineEmits(['toggle'])

const router = useRouter()
const route = useRoute()
const menu = [
  { name: '首页', link: '/home' },
  { name: '文章', link: '/articles' },
  { name: 'AI', link: '/tools/sparkai' },
  { name: '关于', link: '/about' }
]

// 动态添加管理员菜单
const adminMenu = computed(() => {
  const userType = localStorage.getItem('userType') || 'USER'
  if (userType === 'ADMIN') {
    return [
      { name: '首页', link: '/home' },
      { name: '文章', link: '/articles' },
      { name: '审核', link: '/admin/articles/review' },
      { name: 'AI', link: '/tools/sparkai' },
      { name: '关于', link: '/about' }
    ]
  }
  return menu
})

const drawer = ref(false)
const isMobile = ref(false)
const showTools = ref(false)
const toolsList = [
  { name: 'compress', title: '批量压缩文件', desc: '多文件/文件夹一键压缩', path: '/tools/compress' },
  { name: 'unzip', title: '批量解压文件', desc: 'ZIP批量解压与下载', path: '/tools/unzip' },
  { name: 'bili', title: 'B站视频解析下载', desc: 'B站视频一键解析下载', path: '/tools/bili' },
  { name: 'market', title: 'A股主要指数行情', desc: '实时A股指数数据', path: '/tools/market' },
  { name: 'weather', title: '实时天气基本情况', desc: '本地天气与地图', path: '/tools/weather' },
  { name: 'network', title: '网络基本状态', desc: '网络延迟与类型检测', path: '/tools/network' }
]



function checkMobile() {
  isMobile.value = window.innerWidth < 800
}
onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
})
onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
})

function goHome() {
  router.push('/home')
}

function isActive(link) {
  return route.path === link
}

const goTool = (path) => {
  showTools.value = false
  router.push(path)
}
</script>

<style scoped>
.header-blur {
  width: 100vw;
  position: fixed;
  top: 0; left: 0;
  z-index: 100;
  background: transparent !important;
  backdrop-filter: blur(16px) saturate(180%);
  -webkit-backdrop-filter: blur(16px) saturate(180%);
  border-bottom: none !important;
  box-shadow: var(--header-shadow);
}
.header-container {
  width: 100%;
  max-width: 100vw;
  margin: 0 auto;
  height: 64px;
  display: flex;
  align-items: center;
  position: relative;
  padding: 0 12px;
  box-sizing: border-box;
  background: transparent !important;
}
.logo {
  display: flex;
  align-items: center;
  cursor: pointer;
  font-weight: bold;
  font-size: 20px;
  user-select: none;
  gap: 2px;
}
.logo-box {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 35px;
  height: 35px;
  background: #7b98d2;
  color: #fff;
  font-weight: bold;
  font-size: 20px;
  border-radius: 6px;
  letter-spacing: 1px;
  font-family: 'Arial', 'Helvetica', 'sans-serif';
}
.logo-text {
  color: var(--text-color);
  font-size: 20px;
  font-weight: 500;
  letter-spacing: 1px;
  font-family: 'Arial', 'Helvetica', 'sans-serif';
  margin-left: 2px;
}
.nav-menu {
  display: flex;
  gap: 32px;
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  background: transparent !important;
}
.nav-link {
  position: relative;
  color: var(--text-color);
  font-size: 16px;
  text-decoration: none;
  font-weight: 500;
  padding: 6px 12px;
  border-radius: 0;
  transition: color 0.2s;
  margin: 0 12px;
  background: none;
  outline: none;
  display: inline-block;
}
.nav-link.active {
  font-weight: bold;
  color: #5d91d2;
}
.nav-link.active::after {
  content: '';
  position: absolute;
  left: 0; right: 0; bottom: -2px;
  height: 3px;
  background: #5d91d2;
  border-radius: 2px;
  transition: all 0.2s;
}
.nav-link:not(.active):hover {
  color: #7b98d2;
}
.header-right-group {
  display: flex;
  align-items: center;
  gap: 18px;
  margin-left: auto;
  height: 100%;
  position: relative;
  min-width: 0;
  max-width: 100%;
  padding-right: 150px;
}
.menu-btn {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: #4a5a7a;
}
.mobile-drawer {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.18);
  z-index: 999;
  display: flex;
  justify-content: flex-end;
}
.drawer-content {
  width: 220px;
  height: 100%;
  background: rgba(255,255,255,0.85);
  backdrop-filter: blur(16px) saturate(180%);
  -webkit-backdrop-filter: blur(16px) saturate(180%);
  box-shadow: -2px 0 16px 0 rgba(140,166,219,0.12);
  display: flex;
  flex-direction: column;
  padding: 24px 16px;
  animation: drawer-in 0.3s;
}
@keyframes drawer-in {
  from { transform: translateX(100%); }
  to { transform: translateX(0); }
}
.drawer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
  font-size: 18px;
  margin-bottom: 24px;
}
.close-btn {
  font-size: 28px;
  cursor: pointer;
  color: #888;
  line-height: 1;
}
.drawer-link {
  color: #4a5a7a;
  font-size: 16px;
  text-decoration: none;
  font-weight: 500;
  padding: 10px 0;
  border-bottom: 1px solid #eee;
  transition: color 0.2s;
  display: block;
}
.drawer-link:hover {
  color: #7b98d2;
}
@media (max-width: 800px) {
  .nav-menu {
    display: none;
  }
  .menu-btn {
    display: flex;
  }
  .header-container {
    padding: 0 12px;
  }
}
@media (max-width: 500px) {
  .drawer-content {
    width: 70vw;
    min-width: 140px;
    padding: 18px 8px;
  }
  .header-container {
    height: 54px;
  }
}
@media (max-width: 900px) {
  .header-container {
    padding: 0 4px;
  }
  .header-right-group {
    gap: 8px;
    padding-right: 12px;
  }
}
.drawer-fade-enter-active, .drawer-fade-leave-active {
  transition: opacity 0.2s;
}
.drawer-fade-enter-from, .drawer-fade-leave-to {
  opacity: 0;
}
.drawer-fade-enter-to, .drawer-fade-leave-from {
  opacity: 1;
}
.theme-switch-right {
  margin-left: auto;
}
.tools-dropdown-trigger {
  position: relative;
  display: inline-block;
  cursor: pointer;
  font-weight: 500;
  font-size: 16px;
  color: var(--text-color);
  margin: 0 12px;
}
.tools-dropdown-trigger .active {
  color: #5d91d2;
  font-weight: bold;
}
.tools-dropdown-card {
  will-change: transform, opacity;
  position: absolute;
  top: 38px;
  left: 50%;
  transform: translateX(-50%);
  min-width: 420px;
  max-width: 520px;
  background: var(--card-bg);
  color: var(--text-color);
  box-shadow: 0 8px 32px rgba(0,0,0,0.13);
  border-radius: 18px;
  padding: 24px 32px 18px 32px;
  z-index: 2000;
  border: 1px solid var(--border-color);
  transition: opacity 0.18s, transform 0.18s;
  opacity: 1;
}
.tools-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 18px 32px;
}
.tool-item {
  background: var(--bg-color);
  color: var(--text-color);
  border-radius: 10px;
  padding: 16px 18px;
  cursor: pointer;
  transition: background 0.18s, box-shadow 0.18s, border 0.18s;
  box-shadow: 0 2px 8px rgba(140,166,219,0.06);
  border: 1px solid var(--border-color);
}
.tool-item:hover {
  background: var(--border-color);
}
.tool-title {
  font-size: 1.13rem;
  font-weight: bold;
  color: var(--tool-title-color);
  margin-bottom: 6px;
}
.tool-desc {
  font-size: 0.98rem;
  color: var(--tool-desc-color);
}
.tools-dropdown-fade-enter-active, .tools-dropdown-fade-leave-active {
  transition: opacity 0.18s, transform 0.18s;
}
.tools-dropdown-fade-enter-from, .tools-dropdown-fade-leave-to {
  opacity: 0;
  transform: translateX(-50%) translateY(20px);
}
.tools-dropdown-fade-enter-to, .tools-dropdown-fade-leave-from {
  opacity: 1;
  transform: translateX(-50%) translateY(0);
}
.header-blur::after,
.header-blur::before,
.header-container::after,
.header-container::before {
  display: none !important;
  background: none !important;
  border: none !important;
  height: 0 !important;
  min-height: 0 !important;
  max-height: 0 !important;
  margin: 0 !important;
  padding: 0 !important;
  box-shadow: none !important;
  outline: none !important;
}
.header-blur + div,
.header-blur + * {
  background: transparent !important;
  border: none !important;
  box-shadow: none !important;
}

</style> 