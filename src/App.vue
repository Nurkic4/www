<template>
  <transition name="slide-down">
    <Header v-if="showHeader" :is-night="isNight" @toggle="handleThemeToggle" />
  </transition>
  <router-view v-slot="{ Component }">
  <transition :name="transitionName" mode="out-in">
      <component :is="Component" :key="route.path || ''" />
  </transition>
  </router-view>
  <StagewiseToolbar v-if="isDev" :config="toolbarConfig" />
</template>

<script lang="ts" setup>
import { ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { StagewiseToolbar } from '@stagewise/toolbar-vue'
import * as VuePlugin from '@stagewise-plugins/vue'
import Header from './components/Header.vue'
import './assets/theme.css'

const isDev = import.meta.env.DEV
const toolbarConfig = {
  plugins: [VuePlugin as unknown as any] // 类型断言，兼容 ToolbarConfig
}

const route = useRoute()
const transitionName = ref<string>('')
const showHeader = ref(false)
let lastFrom = ''
const isNight = ref(localStorage.getItem('isNight') === 'true')

watch(
  () => route.path,
  (to, from) => {
    const toPath = to || ''
    const fromPath = from || ''
    if (toPath === '/home' && fromPath === '/login') {
      showHeader.value = false
      setTimeout(() => {
        showHeader.value = true
      }, 300)
    } else if (!['/login', '/register'].includes(toPath)) {
      showHeader.value = true
    } else {
      showHeader.value = false
    }
    lastFrom = fromPath
    if (fromPath === '/login' && toPath === '/home') {
      transitionName.value = 'login-to-home'
    } else {
      transitionName.value = ''
    }
    // 主题切换逻辑
    if (["/login", "/register"].includes(toPath)) {
      document.documentElement.setAttribute('data-theme', 'light')
      document.documentElement.classList.remove('dark')
    } else {
      document.documentElement.setAttribute('data-theme', isNight.value ? 'dark' : 'light')
      if (isNight.value) {
        document.documentElement.classList.add('dark')
      } else {
        document.documentElement.classList.remove('dark')
      }
    }
  },
  { immediate: true }
)

function handleThemeToggle(val: boolean) {
  isNight.value = val
  localStorage.setItem('isNight', String(val))
  if (!["/login", "/register"].includes(route.path)) {
    document.documentElement.setAttribute('data-theme', isNight.value ? 'dark' : 'light')
    if (isNight.value) {
      document.documentElement.classList.add('dark')
    } else {
      document.documentElement.classList.remove('dark')
    }
  }
}
</script>

<style>
body, #app {
  background: var(--bg-color);
  color: var(--text-color);
}
.login-to-home-enter-active {
  animation: home-in 0.8s ease;
}
.login-to-home-leave-active {
  animation: login-out 0.5s ease;
  position: absolute;
  width: 100%;
}
@keyframes login-out {
  0% { transform: translateX(0); opacity: 1; }
  100% { transform: translateX(-100vw); opacity: 0; }
}
@keyframes home-in {
  0% { transform: translateY(100vh); opacity: 0; }
  100% { transform: translateY(0); opacity: 1; }
}
.slide-left-enter-active, .slide-left-leave-active {
  transition: all 0.4s ease;
}
.slide-left-enter-from {
  transform: translateX(100vw);
  opacity: 0;
}
.slide-left-leave-to {
  transform: translateX(-100vw);
  opacity: 0;
}
.slide-down-enter-active {
  animation: slideDown 0.7s cubic-bezier(.4,0,.2,1);
}
@keyframes slideDown {
  0% {
    transform: translateY(-100%);
    opacity: 0;
  }
  100% {
    transform: translateY(0);
    opacity: 1;
  }
}
</style> 