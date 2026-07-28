# UI 优化设计规格 — 引入 Element Plus

## 概述

将 travel-mouse 前端的自定义 CSS 替换为 Element Plus 组件库，统一 UI 风格，提升开发效率和视觉一致性。

## 技术选型

**选定方案：Element Plus**

选择理由：
- 国内最流行的 Vue 3 UI 库，资料最丰富
- 中文文档完善，社区活跃
- 遇到问题容易搜到解决方案
- 饿了么团队出品，长期维护有保障

## 安装与配置

### 安装依赖

```bash
cd frontend
npm install element-plus @element-plus/icons-vue
```

### 配置方式：全量引入

在 `main.js` 中：

```javascript
import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'

const app = createApp(App)

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(router)
app.use(ElementPlus)
app.mount('#app')
```

选择全量引入的理由：
- 本地工具，打包体积不敏感
- 配置简单，无需按需引入插件
- 避免遗漏组件导致的报错

## 组件映射

| 现有元素 | Element Plus 组件 | 说明 |
|----------|------------------|------|
| 顶部导航栏 | `el-menu` (mode="horizontal") | 首页/收藏/新建计划 |
| 计划卡片 | `el-card` + `el-button` | 首页计划列表 |
| 收藏卡片 | `el-card` | 收藏列表 |
| 表单（创建计划） | `el-form` + `el-input` + `el-date-picker` | 日期选择器替代原生 input |
| 天卡片 | `el-card` + `el-tag` | 状态标签（草稿/已完成） |
| 搜索框 | `el-input` + `el-button` | 目的地搜索 |
| 搜索结果列表 | `el-scrollbar` + 自定义列表 | 保持轻量 |
| 目的地卡片 | `el-card` + `el-button` | 加入路线/删除 |
| 路线列表 | `el-timeline` | 更直观的路线展示 |
| 操作按钮 | `el-button` | 回退/暂存 |
| 确认弹窗 | `ElMessageBox.confirm` | 替代原生 confirm |
| 成功提示 | `ElMessage` | 替代原生 alert |
| 空状态 | `el-empty` | 无数据时展示 |

### 保留自定义的部分

- 三栏布局结构（左 22% / 中地图 / 右 22%）— Element Plus 无此布局组件
- 地图容器 — 高德地图自身渲染

## 文件级改动范围

### 需修改的文件（13 个）

| 文件 | 改动内容 |
|------|----------|
| `main.js` | 引入 Element Plus + 图标注册 |
| `App.vue` | 导航栏改用 `el-menu` |
| `HomeView.vue` | `el-card` 卡片 + `el-empty` + `ElMessageBox` |
| `FavoritesView.vue` | `el-card` + `el-tag` + `el-empty` |
| `PlanCreateView.vue` | `el-form` + `el-input` + `el-date-picker` + `ElMessage` |
| `PlanDetailView.vue` | `el-card` + `el-tag`（状态） |
| `DayPlanView.vue` | `ElMessage` 替代 alert |
| `LeftPanel.vue` | 无结构变化，仅样式微调 |
| `AddDestination.vue` | `el-input` + `el-button` + `el-scrollbar` |
| `DestinationCard.vue` | `el-card` + `el-button` |
| `PendingList.vue` | `el-empty` |
| `RoutePlan.vue` | `el-timeline` 展示路线 |
| `RouteActions.vue` | `el-button` |

### 不改动的文件

- `MapContainer.vue` — 纯地图容器
- `api/` — API 调用层
- `composables/` — 地图逻辑
- `router/` — 路由配置

## 设计原则

1. **渐进替换**：保持现有功能不变，仅替换 UI 组件
2. **保留布局**：三栏布局结构不变，仅替换内部组件
3. **统一风格**：所有按钮、卡片、表单使用 Element Plus 统一风格
4. **删除冗余 CSS**：替换后删除对应的自定义样式

## 预计工作量

纯前端改动，约 1-2 小时。
