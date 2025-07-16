//git config --global user.name "Nurkic4"
//git config --global user.email "nurkic4@example.com"

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

## 推荐目录结构（单仓库 Monorepo 示例）

```
your-project-root/
│
├── frontend/      # 前端项目目录（如 Vue、React、Angular 等）
│   ├── src/
│   ├── public/
│   ├── package.json
│   ├── ...（前端相关文件）
│
├── backend/       # 后端项目目录（如 Spring Boot、Node.js、Python 等）
│   ├── src/
│   ├── pom.xml / package.json / requirements.txt
│   ├── ...（后端相关文件）
│
├── README.md
├── .gitignore
└── ...（通用文档、设计文档等）
```

---

## 如何操作

### 1. 新建目录结构

在你的项目根目录下，执行：

```bash
mkdir frontend
mkdir backend
```

### 2. 移动已有代码

- **前端代码**（如 Vue 项目）全部移动到 `frontend/` 目录下。
- **后端代码**（如 Spring Boot 项目）全部移动到 `backend/` 目录下。

例如：
```bash
# 假设你当前在 your-project-root 目录下
mv src/ package.json package-lock.json vite.config.ts tsconfig.json ... frontend/
# 后端相关文件同理
mv 后端src/ pom.xml ... backend/
```
> Windows 下可以用资源管理器拖拽，也可以用 `move` 命令。

### 3. 修改 .gitignore

在项目根目录下的 `.gitignore` 文件中，分别忽略前后端的依赖和编译产物：

```gitignore
# 前端
/frontend/node_modules/
/frontend/dist/

# 后端（以 Java/Spring Boot 为例）
/backend/target/
/backend/.idea/
/backend/.vscode/

# 通用
*.log
.env
```

### 4. 更新 README.md

在 `README.md` 里说明你的项目结构和启动方式，例如：

```markdown
<code_block_to_apply_changes_from>
```

---

## 5. 提交更改

```bash
git add .
git commit -m "重构项目为前后端分离目录结构"
git push
```

---

## 6. 以后开发

- 前端开发者只需在 `frontend/` 目录下开发和运行。
- 后端开发者只需在 `backend/` 目录下开发和运行。

---

### 总结

- 这样结构清晰，协作方便，适合全栈/小团队。
- 只需一次性整理，后续开发体验会更好。

如需**具体的移动命令**或**目录结构自动生成脚本**，请告诉我你的操作系统和具体需求，我可以一步步帮你操作！
