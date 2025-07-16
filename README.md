
# 智能多功能工具箱平台

## 项目简介
本项目为一站式智能工具箱平台，采用前后端分离架构，前端基于 Vue3、TypeScript、Vite、Element Plus，后端采用 Spring Boot、MySQL，集成多种第三方API（如AI对话、天气、股票、B站视频解析等），为用户提供文件处理、数据可视化、AI助手、网络检测等多样化服务。

## 技术栈
- **前端**：Vue3 + TypeScript + Vite + Pinia + Element Plus + ECharts
- **后端**：Spring Boot 3.x + Spring Security + JPA + MySQL
- **通信**：RESTful API，Axios 封装请求，JWT 认证
- **外部API**：集成 AI 聊天、天气、股票、B站视频等第三方服务
- **工具链**：npm、Git、Postman、VSCode、Swagger

## 主要功能
- 用户注册、登录、登出、个人信息管理，支持 JWT 鉴权与权限控制
- 多功能工具箱：文件压缩/解压、B站视频解析、AI 聊天、天气查询、网络检测等
- 外部API集成：对接讯飞星火AI、必应天气、聚合数据等，实现智能对话、天气、股票等功能
- 数据可视化：使用 ECharts 展示天气、股票等数据
- 响应式布局与日夜主题切换，优化多端体验
- 前后端接口联调，详细接口文档，统一异常处理

## 项目亮点
- 前后端分离，模块化设计，易于扩展和维护
- TypeScript 全面应用，代码类型安全
- 多外部API集成，功能丰富，体验智能
- 数据可视化与响应式设计，提升用户体验

## 启动方式

### 前端（www-vue3）
1. 安装依赖：
   ```bash
   npm install
   ```
2. 启动开发环境：
   ```bash
   npm run dev
   ```
3. 访问： http://localhost:3000

### 后端（Spring Boot）
1. 配置数据库（MySQL），并修改 application.yml 中的数据库连接信息。
2. 启动 Spring Boot 项目（推荐使用 IDEA 或命令行）：
   ```bash
   mvn spring-boot:run
   ```
3. 默认端口：8082

### 其他说明
- 前端开发环境端口为 3000，后端为 8082，已配置代理解决跨域问题。
- 外部API需根据实际申请密钥配置。
- 更多接口和功能说明详见后端API文档。

---
