import { createRouter, createWebHistory, RouterView } from 'vue-router'
import { h } from 'vue'
import { useUserStore } from '@/store/user'

const Home = () => import('../views/Home.vue')
const Auth = () => import('../views/Auth.vue')
const About = () => import('../views/About.vue')

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', component: Auth, meta: { title: '登录' } },
  { path: '/register', component: Auth, meta: { title: '注册' } },
  { path: '/home', component: Home, meta: { title: '首页', requiresAuth: true } },
  { path: '/ai', component: Home, meta: { title: 'AI', requiresAuth: true } },
  { path: '/essay', component: Home, meta: { title: '小作文', requiresAuth: true } },
  { path: '/about', component: About, meta: { title: '关于我', requiresAuth: true } },
  // 文章系统路由
  { path: '/articles', component: () => import('../views/article/List.vue'), meta: { title: '文章列表', requiresAuth: true } },
  { path: '/articles/create', component: () => import('../views/article/Create.vue'), meta: { title: '创建文章', requiresAuth: true } },
  { path: '/articles/edit/:id', component: () => import('../views/article/Edit.vue'), meta: { title: '编辑文章', requiresAuth: true } },
  { path: '/articles/:id', component: () => import('../views/article/Detail.vue'), meta: { title: '文章详情', requiresAuth: true } },
  { path: '/articles/my', component: () => import('../views/article/My.vue'), meta: { title: '我的文章', requiresAuth: true } },
  { path: '/admin/articles/review', component: () => import('../views/article/Review.vue'), meta: { title: '文章审核', requiresAuth: true } },
  {
    path: '/tools',
    component: { render: () => h(RouterView) },
    meta: { requiresAuth: true },
    children: [
      { path: 'compress', component: () => import('../views/tools/Compress.vue'), meta: { title: '批量压缩', requiresAuth: true } },
      { path: 'unzip', component: () => import('../views/tools/Unzip.vue'), meta: { title: '批量解压', requiresAuth: true } },
      { path: 'bili', component: () => import('../views/tools/Bili.vue'), meta: { title: 'B站解析', requiresAuth: true } },
      { path: 'market', component: () => import('../views/tools/Market.vue'), meta: { title: 'A股行情', requiresAuth: true } },
      { path: 'weather', component: () => import('../views/tools/Weather.vue'), meta: { title: '天气', requiresAuth: true } },
      { path: 'network', component: () => import('../views/tools/Network.vue'), meta: { title: '网络', requiresAuth: true } },
      { path: 'sparkai', name: 'SparkAI', component: () => import('../views/tools/SparkAI.vue'), meta: { title: 'AI 聊天', icon: 'icon-ai', requiresAuth: true } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  if (to.matched.some(record => record.meta.requiresAuth) && !userStore.token) {
    next('/login')
  } else {
    next()
  }
})

export default router 