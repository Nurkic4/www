<template>
  <div class="login-bg">
    <div class="paper-planes">
      <img
        v-for="(plane, idx) in planes"
        :key="plane.id"
        :src="planeSvg"
        class="paper-plane"
        :class="'plane-' + idx"
        :style="plane.style"
      />
    </div>
    <div class="login-left">
      <div class="lottie-area">
        <transition name="fade-lottie" mode="out-in">
          <div class="lottie-group" :key="isLogin">
            <lottie-player
              v-if="isLogin"
              :src="animationSrc"
              background="transparent"
              speed="1"
              style="width: 300%; max-width: 800px; min-width: 750px; height: auto; margin-right: 500px;"
              loop
              autoplay
              key="login"
            />
            <lottie-player
              v-else
              :src="animationSrc"
              background="transparent"
              speed="1"
              style="width: 300%; max-width: 800px; min-width: 750px; height: auto; margin-right: 500px;"
              loop
              autoplay
              key="register"
            />
            <div class="custom-shapes">
              <svg class="rotating-triangle" width="40" height="40" viewBox="0 0 40 40">
                <polygon points="20,4 36,36 4,36" fill="#8ca6db" />
              </svg>
              <div class="rotating-square"></div>
            </div>
          </div>
        </transition>
      </div>
      <div class="icon-burst">
        <img
          v-for="(icon, idx) in iconList"
          :key="icon"
          :src="icon"
          class="burst-icon"
          :class="iconAnimClass"
          :style="{
            left: '35%',
            top: '55%',
            transform: iconAnimClass === 'burst-in' && iconTransforms[idx] ? iconTransforms[idx].to : (iconTransforms[idx] ? iconTransforms[idx].from : 'translate(-600%, -50%) scale(0.2)')
          }"
        />
      </div>
    </div>
    <div class="login-circle"></div>
    <div class="login-card">
      <div class="logo-top">
        <div class="logo-bar">
          <div class="logo-group">
                          <span class="logo-box">QQ</span>
            <span class="logo-text">青青空间</span>
          </div>
          <div class="card-icons">
            <a
              href="https://qm.qq.com/q/LMj3zuOXwA"
              target="_blank"
              rel="noopener"
              title="加我QQ"
            >
              <img :src="qqPng" alt="QQ" class="icon-img" />
            </a>
            <span class="icon-img" title="加我微信" @click="showWeixin = true" style="cursor:pointer;">
              <img :src="weixinPng" alt="微信" />
            </span>
          </div>
        </div>
        <div class="logo-divider"></div>
      </div>
      <el-dialog v-model="showWeixin" width="320px" :show-close="true" center>
        <div style="text-align:center;">
          <img :src="weixinQrPng" alt="微信二维码" style="width:200px;" />
          <div style="margin-top:10px;">微信扫码加好友</div>
        </div>
      </el-dialog>
      <transition name="svg-fade" mode="out-in">
        <img
          v-if="isLogin"
          :src="loginSvg"
          alt="登录"
          class="auth-svg"
          key="login-svg"
        />
        <img
          v-else
          :src="registerSvg"
          alt="注册"
          class="auth-svg"
          key="register-svg"
        />
      </transition>
      <transition name="card-flip" mode="out-in">
        <div :key="isLogin">
          <!-- 登录表单 -->
          <template v-if="isLogin">
            <div class="auth-header">
              <h2 class="auth-title">登录</h2>
              <p class="auth-desc">这是我的个人网页，系统包含工具箱、小游戏、AI智能体以及各种类型的信息等，你需要登录以进入主页。</p>
            </div>
            <form @submit.prevent="doLogin" class="login-form">
              <div class="form-group">
                <el-input
                  v-model="loginUsername"
                  :prefix-icon="User"
                  placeholder="User name"
                  :error="loginErrors.username"
                />
              </div>
              <div class="form-group">
                <el-input
                  v-model="loginPassword"
                  :prefix-icon="Lock"
                  type="password"
                  placeholder="Password"
                  :error="loginErrors.password"
                />
              </div>
              <div class="form-group">
                <button class="login-btn" :disabled="loading">
                  立 即 登 录 <span class="arrow">&rarr;</span>
                </button>
              </div>
            </form>
          </template>
          <!-- 注册表单 -->
          <template v-else>
            <div class="auth-header">
              <h2 class="auth-title">注册</h2>
              <p class="auth-desc">这是我的个人网页，系统包含工具箱、小游戏、AI智能体以及各种类型的信息等，你需要注册成为普通用户。</p>
            </div>
            <form @submit.prevent="doRegister" class="login-form">
              <div class="form-group">
                <el-input
                  v-model="registerUsername"
                  :prefix-icon="User"
                  placeholder="User name"
                  :error="registerErrors.username"
                />
              </div>
              <div class="form-group">
                <el-input
                  v-model="registerPassword"
                  :prefix-icon="Lock"
                  type="password"
                  placeholder="Password"
                  :error="registerErrors.password"
                />
              </div>
              <div class="form-group">
                <el-input
                  v-model="registerPhone"
                  :prefix-icon="Phone"
                  placeholder="Phone"
                  :error="registerErrors.phone"
                />
              </div>
              <div class="form-group">
                <el-input
                  v-model="registerEmail"
                  :prefix-icon="Message"
                  placeholder="Email"
                  :error="registerErrors.email"
                />
              </div>
              <div class="form-group">
                <button class="login-btn" :disabled="loading">
                  即 刻 注 册 <span class="arrow">&rarr;</span>
                </button>
              </div>
            </form>
          </template>
        </div>
      </transition>
      <div class="login-social">
        <a class="login-link" href="#" @click.prevent="switchForm">
          {{ isLogin ? '没有账号？去注册' : '已有账号？去登录' }}
        </a>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { request } from '@/utils/request.ts'
import { User, Lock, Phone, Message } from '@element-plus/icons-vue'
import '@lottiefiles/lottie-player'
import loginJson from './css/json/login.json'
import registerJson from './css/json/register.json'
// 新增图片import
import qqPng from './css/png/QQ.png'
import weixinPng from './css/png/微信.png'
import weixinQrPng from './css/png/weixin.png'
import loginSvg from './css/svg/登录.svg'
import registerSvg from './css/svg/注册.svg'
import planeSvg from './css/svg/纸飞机.svg'
import cloudPng from './css/png/背景云朵.png'
import { useUserStore } from '@/store/user'
// vee-validate 集成
import { useForm, useField } from 'vee-validate'
import * as yup from 'yup'

const router = useRouter()
const isLogin = ref(true)
const loading = ref(false)
const showWeixin = ref(false)

// 登录表单校验
const loginSchema = yup.object({
  username: yup.string().required('请输入用户名'),
  password: yup.string().required('请输入密码')
})
const { handleSubmit: handleLoginSubmit, errors: loginErrors, resetForm: resetLoginForm } = useForm({
  validationSchema: loginSchema,
  initialValues: { username: '', password: '' }
})
const { value: loginUsername } = useField('username')
const { value: loginPassword } = useField('password')

// 注册表单校验
const registerSchema = yup.object({
  username: yup.string().required('请输入用户名'),
  password: yup.string().required('请输入密码'),
  phone: yup.string().required('请输入手机号').matches(/^1[3-9]\d{9}$/, '手机号格式不正确'),
  email: yup.string().required('请输入邮箱').email('邮箱格式不正确')
})
const { handleSubmit: handleRegisterSubmit, errors: registerErrors, resetForm: resetRegisterForm } = useForm({
  validationSchema: registerSchema,
  initialValues: { username: '', password: '', phone: '', email: '' }
})
const { value: registerUsername } = useField('username')
const { value: registerPassword } = useField('password')
const { value: registerPhone } = useField('phone')
const { value: registerEmail } = useField('email')

const userStore = useUserStore()

function switchForm() {
  isLogin.value = !isLogin.value
  // 重置表单
  if (isLogin.value) {
    resetLoginForm()
  } else {
    resetRegisterForm()
  }
}

const doLogin = handleLoginSubmit(async () => {
  loading.value = true
  try {
    const formData = {
      username: loginUsername.value,
      password: loginPassword.value
    }
    const { data, error } = await request({ url: '/api/user/login', method: 'POST', data: formData })
    if (error) throw error
    userStore.setToken(data)
    router.push('/home')
  } catch (e) {
    alert(e.message || '登录失败')
  } finally {
    loading.value = false
  }
})

const doRegister = handleRegisterSubmit(async () => {
  loading.value = true
  try {
    const formData = {
      username: registerUsername.value,
      password: registerPassword.value,
      phone: registerPhone.value,
      email: registerEmail.value
    }
    const { data, error } = await request({ url: '/api/user/register', method: 'POST', data: formData })
    if (error) throw error
    alert('注册成功，请登录')
    isLogin.value = true
    resetRegisterForm()
  } catch (e) {
    alert(e.message || '注册失败')
  } finally {
    loading.value = false
  }
})

const animationSrc = computed(() => {
  const json = isLogin.value ? loginJson : registerJson
  return 'data:application/json;base64,' + btoa(unescape(encodeURIComponent(JSON.stringify(json))))
})

// 纸飞机动画
const planes = ref([])
function launchPlanes() {
  // 三只纸飞机，左上、右中上、偏下方
  const directions = [
    { x: '-90vw', y: '-45vh', tx: '-10vw', ty: '-38vh', rotate: 45 },   // 左上
    { x: '5vw', y: '-90vh', tx: '40vw', ty: '-38vh', rotate: 135 },   // 右中上
    { x: '20vw', y: '60vh', tx: '-10vw', ty: '32vh', rotate: -90 }        // 偏下方
  ]
  planes.value = directions.map((d, i) => ({
    id: Date.now() + '-' + i + '-' + Math.random(),
    style: {
      '--from-x': d.x,
      '--from-y': d.y,
      '--to-x': d.tx,
      '--to-y': d.ty,
      '--rot': d.rotate + 'deg'
    }
  }))
}
watch(isLogin, () => {
  planes.value = []
  setTimeout(launchPlanes, 100)
})
launchPlanes()

// 小图标路径
const iconList = Array.from({length: 11}, (_, i) =>
  new URL(`./css/png/登录注册图标 (${i+1}).png`, import.meta.url).href
)
// 随机分布生成器（初始都在lottie正下方，弹出到四周）
function randomIconTransforms() {
  const N = iconList.length;
  return iconList.map((_, idx) => {
    const angle = (2 * Math.PI / N) * idx;
    // 轻微扰动
    const radius = 330 + Math.random() * 100;
    const deg = angle * 180 / Math.PI;
    const iconDeg = deg - 90; // 让图标上方指向弹出方向
    return {
      from: `translate(-600%, -50%) scale(0.2) rotate(${iconDeg}deg)`,
      to: `translate(-600%, -50%) translate(${-Math.cos(angle) * radius}px, ${-Math.sin(angle) * radius}px) scale(1) rotate(${iconDeg}deg)`
    }
  })
}
const iconTransforms = ref([])
const iconAnimClass = ref('burst-in')

// Initialize iconTransforms immediately
iconTransforms.value = randomIconTransforms()

function burstIcons() {
  iconTransforms.value = randomIconTransforms()
  iconAnimClass.value = 'burst-in'
}
watch(isLogin, async () => {
  iconAnimClass.value = 'burst-out'
  await nextTick()
  setTimeout(() => {
    burstIcons()
  }, 600)
})
onMounted(() => {
  setTimeout(() => { burstIcons() }, 100)
})
</script>

<style>
body, #app {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  background: transparent;
}
</style>

<style scoped>
.login-bg {
  position: relative;
  min-height: 100vh;
  height: 100vh;
  background: linear-gradient(135deg, #b993d6 0%, #8ca6db 100%);
  display: flex;
  flex-direction: row;
  align-items: stretch;
  justify-content: flex-end;
  overflow: hidden;
}
.login-bg::before {
  content: '';
  position: absolute;
  left: -100px; top: -50px;
  width: 100vw;
  height: 210vh;
  background: var(--cloud-bg, url('./css/png/背景云朵.png')) no-repeat center center;
  background-size: cover;
  opacity: 0.08; /* 可根据需要调整透明度 */
  z-index: 0;
  pointer-events: none;
}
.login-bg > * {
  position: relative;
  z-index: 1;
}
.login-left {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 50vw;
  min-width: 400px;
  max-width: 600px;
  height: 100vh;
  min-height: 180px;
  background: transparent;
  z-index: 2;
  margin-left: 40px;
}
.login-circle {
  position: absolute;
  left: 0;
  top: 50%;
  transform: translate(-40%, -50%);
  width: 600px;
  height: 600px;
  border-radius: 50%;
  border: 8px solid rgba(185, 147, 214, 0.18);
  background: radial-gradient(circle at 60% 40%, rgba(185, 147, 214, 0.10), transparent 70%);
  z-index: 1;
  pointer-events: none;
}
.login-card {
  position: relative;
  z-index: 2;
  width: 75vw;
  height: 100vh;
  background: linear-gradient(135deg, #f3eff5 0%, #ecf3ff 100%);
  box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.2);
  padding: 0px 32px 32px 32px;
  display: flex;
  flex-direction: column;
  align-items: stretch;
  justify-content: center;
  color: #333;
}
.login-form {
  margin-bottom: 30px;
}
.form-group {
  margin-bottom: 15px;
}
.form-group:last-child {
  margin-bottom: 0;
}
.login-form .el-input__wrapper {
  background: transparent;
  border: none;
  border-bottom: 1.5px solid #b993d6;
  border-radius: 0;
  box-shadow: none;
  padding: 12px 0;
  min-height: 48px;
}
.login-form .el-input__inner {
  padding: 12px 0;
  font-size: 16px;
}
.login-btn {
  width: 100%;
  background: #8ca6db;
  color: #fff;
  border: none;
  border-radius: 30px;
  padding: 12px 0;
  font-weight: bold;
  font-size: 16px;
  margin-top: 10px;
  box-shadow: 0 2px 8px rgba(140,166,219,0.15);
  cursor: pointer;
  transition: background 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}
.login-btn .arrow {
  margin-left: 10px;
  font-size: 18px;
}
.login-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}
.login-btn:hover:not(:disabled) {
  background: #b993d6;
}
.login-social {
  text-align: right;
  color: #888;
  font-size: 14px;
}
.login-link {
  color: #8ca6db;
  text-decoration: underline;
  font-weight: 500;
  transition: color 0.2s;
}
.login-link:hover {
  color: #b993d6;
}
@media (max-width: 900px) {
  .login-bg {
    flex-direction: column;
    align-items: stretch;
    justify-content: flex-start;
    padding: 0;
  }
  .login-left {
    width: 100vw;
    max-width: 100vw;
    min-width: 0;
    height: auto;
    margin: 0;
    justify-content: flex-start;
    display: none;
  }
  .login-card {
    width: 100vw;
    min-width: 0;
    max-width: 100vw;
    height: auto;
    min-height: 400px;
    border-radius: 0;
    padding: 16px 4vw 24px 4vw;
    box-shadow: none;
    position: static;
  }
  .logo-top, .card-icons {
    position: static;
    left: auto;
    right: auto;
    padding-left: 4vw;
    padding-right: 4vw;
  }
  .auth-svg {
    width: 120px;
    height: 120px;
    margin: 0 auto 24px auto;
  }
  .login-circle {
    display: none;
  }
}
@media (max-width: 700px) {
  .login-card {
    width: 100vw;
    min-width: 0;
    max-width: 100vw;
    height: auto;
    min-height: 400px;
    border-radius: 0;
    padding: 12px 2vw 16px 2vw;
    box-shadow: none;
    position: static;
    align-items: center;
  }
  .logo-top {
    position: static !important;
    width: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 0;
    z-index: 10;
  }
  .logo-bar {
    flex-direction: row !important;
    align-items: center !important;
    gap: 8px !important;
    justify-content: center;
  }
  .logo-divider {
    margin: 8px 0 12px 0;
  }
  .card-icons {
    margin-top: 4px;
    gap: 16px;
  }
  .auth-svg {
    width: 80px !important;
    height: 80px !important;
    margin: 0 auto 12px auto !important;
  }
  .auth-title {
    font-size: 1.2rem;
  }
  .auth-desc {
    font-size: 13px;
  }
  .login-form {
    margin-bottom: 18px;
  }
  .form-group {
    margin-bottom: 12px;
  }
  .login-form .el-input__wrapper {
    padding: 10px 0;
    min-height: 44px;
  }
  .login-form .el-input__inner {
    padding: 10px 0;
    font-size: 15px;
  }
  .login-btn {
    font-size: 15px;
    padding: 10px 0;
  }
  .login-circle {
    display: none;
  }
}
.card-flip-enter-active, .card-flip-leave-active {
  transition: transform 0.5s cubic-bezier(.68,-0.55,.27,1.55), opacity 0.5s;
}
.card-flip-enter-from, .card-flip-leave-to {
  transform: scale(0.95) rotateY(45deg);
  opacity: 0;
}
.card-flip-enter-to, .card-flip-leave-from {
  transform: scale(1) rotateY(0deg);
  opacity: 1;
}
.auth-header {
  text-align: center;
  margin-bottom: 18px;
  margin-top: -100px;
}
.auth-title {
  font-size: 1.8rem;
  margin-bottom: 8px;
  font-family: 'SimHei', 'Heiti SC', 'Arial', sans-serif;
}
.auth-desc {
  color: #888;
  font-size: 15px;
  line-height: 1.6;
  margin: 0 auto 0 auto;
  max-width: 480px;
}
.fade-lottie-enter-active, .fade-lottie-leave-active {
  transition: opacity 0.5s;
}
.fade-lottie-enter-from, .fade-lottie-leave-to {
  opacity: 0;
}
.fade-lottie-enter-to, .fade-lottie-leave-from {
  opacity: 1;
}
.paper-planes {
  pointer-events: none;
  position: absolute;
  left: 0; top: 0;
  width: 100vw; height: 100vh;
  z-index: 10;
}
.paper-plane {
  position: absolute;
  left: 50%; top: 50%;
  width: 64px; height: 64px;
  opacity: 0;
  filter: opacity(0.85);
  transform: translate(0,0) rotate(0deg);
  animation: fly-plane-in 2s cubic-bezier(.68,-0.55,.27,1.55) forwards;
}
.paper-plane.plane-0 { animation-delay: 0s; }
.paper-plane.plane-1 { animation-delay: 0.1s; }
.paper-plane.plane-2 { animation-delay: 0.2s; }
@keyframes fly-plane-in {
  0% {
    opacity: 0;
    transform: translate(var(--from-x), var(--from-y)) scale(0.7) rotate(0deg);
  }
  40% {
    opacity: 0.85;
  }
  100% {
    opacity: 0.7;
    transform: translate(var(--to-x), var(--to-y)) scale(1) rotate(var(--rot));
  }
}
.auth-svg {
  width: 200px;
  height: 200px;
  display: block;
  margin: -50px auto 50px auto;
}
.svg-fade-enter-active, .svg-fade-leave-active {
  transition: opacity 0.5s;
}
.svg-fade-enter-from, .svg-fade-leave-to {
  opacity: 0;
}
.svg-fade-enter-to, .svg-fade-leave-from {
  opacity: 1;
}
.icon-burst {
  position: absolute;
  left: 0; top: 0; width: 100%; height: 100%;
  pointer-events: none;
  z-index: 0; /* lottie-player 默认z-index: auto, 这里设为更低 */
}
.burst-icon {
  position: absolute;
  width: 32px; height: 32px;
  opacity: 0.05;
  transition: transform 0.6s cubic-bezier(.68,-0.55,.27,1.55), opacity 0.6s;
}
.burst-in {
  opacity: 1;
}
.burst-out {
  opacity: 0;
}
.login-bg > *:not(.bg-cloud) {
  position: relative;
  z-index: 1;
}
.logo-top {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  z-index: 10;
}
.login-card > .logo-top + * {
  margin-top: 0px;
}
.logo-bar {
  display: flex;
  flex-direction: row !important;
  align-items: center !important;
  padding: 18px 32px 0 32px;
  box-sizing: border-box;
  background: transparent;
  z-index: 10;
  justify-content: space-between;
}
.logo-group {
  display: flex;
  align-items: center;
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
  color: #888;
  font-size: 20px;
  font-weight: 500;
  letter-spacing: 1px;
  font-family: 'Arial', 'Helvetica', 'sans-serif';
  margin-left: 5px;
}
.logo-divider {
  width: 100%;
  height: 1.5px;
  background: linear-gradient(90deg, #b993d6 0%, #ecf3ff 100%);
  opacity: 0.35;
  margin: 20px 50px 10px 0;
  border: none;
}
.lottie-area {
  width: 100%;
  max-width: 400px;
  min-width: 180px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: center;
}
.lottie-group lottie-player {
  width: 100% !important;
  max-width: 350px;
  min-width: 120px;
  height: auto !important;
  margin: 0 auto;
  display: block;
}
.custom-shapes {
  position: absolute;
  left: -170px;
  top: 450px;
  z-index: 20;
  display: flex;
  gap: 25px;
  flex-direction: column
}
.rotating-triangle {
  animation: rotate-shape 2.5s linear infinite;
  margin-left: -100px;
  opacity: 0.7;
}
.rotating-square {
  width: 20px;
  height: 20px;
  border: 3px solid #8ca6db;
  animation: rotate-shape 2.5s linear infinite reverse;
  opacity: 0.7;
}
@keyframes rotate-shape {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
.card-icons {
  display: flex;
  gap: 16px;
}
.icon-img {
  width: 32px;
  height: 32px;
  transition: transform 0.2s;
  display: inline-block;
}
.icon-img:hover {
  transform: scale(1.15) rotate(-8deg);
  filter: brightness(1.2);
}
.icon-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
</style> 