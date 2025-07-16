<template>
  <div class="about-container">
    <!-- 头部信息 -->
    <div class="about-header">
      <div class="avatar-section">
        <div class="avatar">
          <img src="../views/css/png/qingqing.png" alt="头像" />
        </div>
        <div class="name-info">
          <h1 class="name">青青空间</h1>
          <p class="title">全栈开发者 / 工具爱好者</p>
        </div>
      </div>
      <div class="contact-info">
        <div class="contact-item">
          <span class="icon">📧</span>
          <span>邮箱：2803760559@qq.com</span>
        </div>
        <div class="contact-item">
          <span class="icon">🌐</span>
          <span>网站：https://qqspace.com</span>
        </div>
        <div class="contact-item">
          <span class="icon">📱</span>
          <span>微信号：cygq150</span>
        </div>
      </div>
    </div>
    <!-- 个人简介 -->
    <div class="about-section">
      <h2 class="section-title">关于我</h2>
      <div class="bio-content">
        <p>这里是我的博客，一个存储我的想法与经历的地方。</p>
        <p>我乐于探索新技术，热衷于创造实用的工具和应用程序。这里就是我的学习实践成果之一。</p>
        <p>你可以在这里找到各种实用的小工具，如文件处理、视频下载、AI聊天、网络检测等功能。</p>
      </div>
    </div>
    <!-- 技能展示 -->
    <div class="about-section">
      <h2 class="section-title">技术栈</h2>
      <div class="skills-grid">
        <div class="skill-category">
          <h3>前端技术</h3>
          <div class="skill-tags">
            <span class="skill-tag">Vue.js</span>
            <span class="skill-tag">TypeScript</span>
            <span class="skill-tag">JavaScript</span>
            <span class="skill-tag">CSS3</span>
            <span class="skill-tag">HTML5</span>
          </div>
        </div>
        <div class="skill-category">
          <h3>后端技术</h3>
          <div class="skill-tags">
            <span class="skill-tag">Node.js</span>
            <span class="skill-tag">Express</span>
            <span class="skill-tag">Python</span>
            <span class="skill-tag">Java</span>
            <span class="skill-tag">MySQL</span>
          </div>
        </div>
        <div class="skill-category">
          <h3>开发工具</h3>
          <div class="skill-tags">
            <span class="skill-tag">IDEA</span>
            <span class="skill-tag">Docker</span>
            <span class="skill-tag">VSCode</span>
            <span class="skill-tag">Webpack</span>
            <span class="skill-tag">Vite</span>
          </div>
        </div>
      </div>
    </div>
    <!-- 项目展示 -->
    <div class="about-section">
      <h2 class="section-title">项目展示</h2>
      <div class="projects-grid">
        <div class="project-card">
          <div class="project-icon">🛠️</div>
          <h3>工具宝箱平台</h3>
          <p>一个集成了多种实用工具的在线平台，包括文件压缩、视频下载、AI聊天等功能。</p>
          <div class="project-tech">
            <span>Vue.js</span>
            <span>Node.js</span>
            <span>Element Plus</span>
          </div>
        </div>
        <div class="project-card">
          <div class="project-icon">🤖</div>
          <h3>AI聊天助手</h3>
          <p>基于星火大语言模型的智能聊天助手，支持多轮对话和多种AI模型。</p>
          <div class="project-tech">
            <span>Vue.js</span>
            <span>WebSocket</span>
            <span>AI API</span>
          </div>
        </div>
        <div class="project-card">
          <div class="project-icon">📊</div>
          <h3>数据可视化</h3>
          <p>股票行情、天气信息、网络状态等数据的实时展示和分析。</p>
          <div class="project-tech">
            <span>ECharts</span>
            <span>RESTful API</span>
            <span>WebSocket</span>
          </div>
        </div>
      </div>
    </div>
    <!-- 联系方式 -->
    <div class="about-section">
      <h2 class="section-title">联系我</h2>
      <div class="contact-grid">
        <div class="contact-card">
          <div class="contact-icon">📧</div>
          <h3>邮箱</h3>
          <p>2803760559@qq.com</p>
          <p class="contact-desc">欢迎技术交流与合作</p>
        </div>
        <div class="contact-card">
          <div class="contact-icon">💬</div>
          <h3>微信</h3>
          <p>cygq150</p>
          <p class="contact-desc">添加好友，实时交流</p>
        </div>
        <div class="contact-card">
          <div class="contact-icon">🌐</div>
          <h3>网站</h3>
          <p>https://qqspace.com</p>
          <p class="contact-desc">更多项目和技术分享</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
// 关于页面逻辑
</script>

<style scoped>
html, body, #app {
  margin: 0;
  padding: 0;
  width: 100%;
  min-height: 100vh;
  background: #202535 !important;
  box-sizing: border-box;
}

.about-container {
  min-height: calc(100vh - 64px);
  background: var(--card-bg) !important;
}
[data-theme='dark'] .about-container {
  background: var(--card-bg) !important;
}

.about-content {
  max-width: 1000px;
  width: 100%;
  background: var(--card-bg);
  border-radius: 24px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.1);
  border: 1px solid var(--border-color);
  overflow: hidden;
}

.about-header {
  background: linear-gradient(135deg, #e3f0ff 0%, #f7f8fa 100%);
  color: #1e47a0ef;
  padding: 140px 40px 40px 40px;
  text-align: center;
}
[data-theme='dark'] .about-header {
  background: linear-gradient(135deg, #6578d6 0%, #7a52f3 100%);
  color: #fff;
}

.avatar-section {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 24px;
  margin-bottom: 32px;
}

.avatar {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  overflow: hidden;
  border: 4px solid rgba(255,255,255,0.3);
  box-shadow: 0 8px 24px rgba(0,0,0,0.2);
}

.avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.name-info {
  text-align: left;
}

.name {
  font-size: 2.4rem;
  font-weight: 900;
  margin: 0 0 8px 0;
  letter-spacing: 1px;
}

.title {
  font-size: 1.2rem;
  margin: 0;
  opacity: 0.9;
  font-weight: 500;
}

.contact-info {
  display: flex;
  justify-content: center;
  gap: 32px;
  flex-wrap: wrap;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 1rem;
  opacity: 0.9;
}

.contact-item .icon {
  font-size: 1.2rem;
}

/* 内容区卡片化，提升日间模式层次感 */
.about-section {
  background: rgba(255,255,255,0.85);
  border-radius: 18px;
  box-shadow: 0 4px 24px rgba(59,130,246,0.08);
  margin: 40px auto;
  padding: 40px 32px;
  max-width: 1000px;
  border: none;
}
.section-title {
  color: #2563eb;
}
.skill-tag {
  background: linear-gradient(135deg, #3b82f6 0%, #06b6d4 100%);
  color: #fff;
}

/* 夜间模式下自动切换为深色卡片 */
[data-theme='dark'] .about-section {
  background: rgba(34,37,54,0.95);
  color: #fff;
}
[data-theme='dark'] .section-title {
  color: #90b4ff;
}

.bio-content {
  line-height: 1.8;
  font-size: 1.1rem;
  color: var(--text-color);
  text-align: center;
  max-width: 800px;
  margin: 0 auto;
}

.bio-content p {
  margin: 0 0 16px 0;
}

.skills-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 32px;
}

.skill-category h3 {
  font-size: 1.3rem;
  font-weight: 700;
  color: var(--tool-title-color);
  margin: 0 0 16px 0;
  text-align: center;
}

.skill-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  justify-content: center;
}

.skill-tag {
  background: linear-gradient(135deg, var(--tool-title-color) 0%, #06b6d4 100%);
  color: white;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 0.9rem;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(78,107,222,0.2);
  transition: transform 0.2s;
}

.skill-tag:hover {
  transform: translateY(-2px);
}

.projects-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 24px;
}

.project-card {
  background: var(--bg-color);
  border-radius: 16px;
  padding: 24px;
  border: 1px solid var(--border-color);
  transition: transform 0.2s, box-shadow 0.2s;
  text-align: center;
}

.project-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(78,107,222,0.15);
}

.project-icon {
  font-size: 3rem;
  margin-bottom: 16px;
}

.project-card h3 {
  font-size: 1.3rem;
  font-weight: 700;
  color: var(--tool-title-color);
  margin: 0 0 12px 0;
}

.project-card p {
  color: var(--tool-desc-color);
  line-height: 1.6;
  margin: 0 0 16px 0;
}

.project-tech {
  display: flex;
  gap: 8px;
  justify-content: center;
  flex-wrap: wrap;
}

.project-tech span {
  background: var(--border-color);
  color: var(--text-color);
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 500;
}

.contact-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 24px;
}

.contact-card {
  background: var(--bg-color);
  border-radius: 16px;
  padding: 24px;
  text-align: center;
  border: 1px solid var(--border-color);
  transition: transform 0.2s;
}

.contact-card:hover {
  transform: translateY(-2px);
}

.contact-icon {
  font-size: 2.5rem;
  margin-bottom: 16px;
}

.contact-card h3 {
  font-size: 1.2rem;
  font-weight: 700;
  color: var(--tool-title-color);
  margin: 0 0 8px 0;
}

.contact-card p {
  color: var(--text-color);
  margin: 0 0 4px 0;
  font-weight: 500;
}

.contact-desc {
  color: var(--tool-desc-color) !important;
  font-size: 0.9rem;
  font-weight: 400 !important;
}

@media (max-width: 768px) {
  .about-container {
    padding: 60px 10px 20px 10px;
  }
  
  .about-header {
    padding: 100px 20px 30px 20px;
  }
  
  .avatar-section {
    flex-direction: column;
    gap: 16px;
  }
  
  .avatar {
    width: 100px;
    height: 100px;
  }
  
  .name {
    font-size: 2rem;
    text-align: center;
  }
  
  .contact-info {
    gap: 16px;
  }
  
  .about-section {
    padding: 30px 20px;
  }
  
  .skills-grid,
  .projects-grid,
  .contact-grid {
    grid-template-columns: 1fr;
    gap: 20px;
  }
  
  .section-title {
    font-size: 1.5rem;
  }
}
</style> 