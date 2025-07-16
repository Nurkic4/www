<template>
  <div
    class="theme-switch"
    :class="{ night: isNight }"
    @click="toggle"
    tabindex="0"
    :aria-label="isNight ? '切换到日间模式' : '切换到夜间模式'"
  >
    <div class="switch-bg">
      <div class="clouds" v-if="!isNight">
        <div class="cloud"></div>
        <div class="cloud cloud2"></div>
      </div>
      <div class="stars" v-if="isNight">
        <div v-for="n in 8" :key="n" class="star" :style="starStyle(n)"></div>
      </div>
    </div>
    <div class="switch-ball">
      <div v-if="!isNight" class="sun"></div>
      <div v-else class="moon">
        <div class="moon-hole hole1"></div>
        <div class="moon-hole hole2"></div>
        <div class="moon-hole hole3"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  isNight: Boolean
})
const emit = defineEmits(['toggle'])

function toggle() {
  emit('toggle', !props.isNight)
}

function starStyle(n) {
  const pos = [
    [18, 18], [38, 12], [60, 22], [80, 10],
    [28, 32], [52, 38], [70, 30], [90, 36]
  ]
  return {
    left: pos[n - 1][0] + '%',
    top: pos[n - 1][1] + '%'
  }
}
</script>

<style scoped>
.theme-switch {
  width: 100px;
  height: 30px;
  border-radius: 20px;
  background: linear-gradient(120deg, #e3eaf6 0%, #dbe3ef 100%);
  box-shadow: 0 2px 16px 0 rgba(140,166,219,0.18);
  border: 3px solid #fff;
  display: flex;
  align-items: center;
  position: relative;
  cursor: pointer;
  transition: background 0.5s;
  overflow: hidden;
  user-select: none;
}
.theme-switch:focus {
  outline: 2px solid #8ca6db;
}
.theme-switch .switch-bg {
  position: absolute;
  left: 0; top: 0; right: 0; bottom: 0;
  z-index: 1;
  transition: background 0.5s;
  background: linear-gradient(120deg, #b3d0f7 0%, #e3eaf6 100%);
}
.theme-switch.night .switch-bg {
  background: linear-gradient(120deg, #23253a 0%, #5a5e6e 100%);
}
.clouds {
  position: absolute;
  left: 0; right: 0; bottom: 0;
  height: 60%;
  z-index: 2;
  display: flex;
  align-items: flex-end;
}
.cloud, .cloud2 {
  width: 22px; height: 10px;
  margin-left: 7px;
  margin-top: 7px;
  background: #fff;
  border-radius: 50px 50px 40px 40px;
  box-shadow: 0 2px 8px #e3eaf6;
  opacity: 0.85;
  transition: all 0.5s;
}
.cloud2 {
  width: 14px; height: 6px;
  margin-left: -4px;
  margin-top: 12px;
  opacity: 0.7;
}
.stars {
  position: absolute;
  left: 0; top: 0; right: 0; bottom: 0;
  z-index: 2;
}
.star {
  position: absolute;
  width: 4px; height: 4px;
  background: #fff;
  border-radius: 50%;
  opacity: 0.85;
  box-shadow: 0 0 6px #fff, 0 0 2px #fff;
  transition: opacity 0.5s;
}
.theme-switch .switch-ball {
  position: absolute;
  z-index: 3;
  width: 38px; height: 38px;
  border-radius: 50%;
  left: 4px; top: 3px;
  background: none;
  transition: left 0.5s cubic-bezier(.4,0,.2,1);
  box-shadow: 0 4px 16px 0 rgba(140,166,219,0.18);
  display: flex;
  align-items: center;
  justify-content: center;
}
.theme-switch.night .switch-ball {
  left: 62px;
}
.sun {
  width: 38px; height: 38px;
  background: #ffd43b;
  border-radius: 50%;
  box-shadow: 0 4px 24px #e6c200;
  transition: background 0.5s;
}
.moon {
  width: 38px; height: 38px;
  background: #e0e3e9;
  border-radius: 50%;
  box-shadow: 0 4px 24px #b0b3b9;
  position: relative;
  transition: background 0.5s;
}
.moon-hole {
  position: absolute;
  background: #bfc2c7;
  border-radius: 50%;
  opacity: 0.5;
}
.hole1 { width: 8px; height: 8px; left: 18px; top: 8px; }
.hole2 { width: 5px; height: 5px; left: 8px; top: 18px; }
.hole3 { width: 3px; height: 3px; left: 24px; top: 24px; }
</style> 