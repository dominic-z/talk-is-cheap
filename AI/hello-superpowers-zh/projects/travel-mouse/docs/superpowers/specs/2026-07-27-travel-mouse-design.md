# Travel Mouse — 旅行计划工具设计规格

## 概述

Travel Mouse 是一个运行在本机的旅行计划构建与生成工具，基于高德地图 API 实现目的地搜索、路线规划和交通时间计算，帮助用户高效制定多日旅行计划。

## 技术选型

| 层级 | 技术 | 说明 |
|------|------|------|
| 前端 | Vue 3 + Vite | 组件化开发，交互密集场景 |
| 地图 | 高德 JS API 2.0 | 前端直调，POI 搜索 + 路线规划 + 距离计算 |
| 后端 | Spring Boot + Maven | 纯数据 CRUD，不涉及地图计算 |
| 数据库 | MySQL | 本地已安装运行 |
| 项目结构 | 单体仓库 | `frontend/` + `backend/` 两个子目录 |

## 整体架构

```
┌─────────────────────────────────────────────────────┐
│                    浏览器 (Vue 3)                     │
│  ┌──────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ 待定列表  │  │  高德地图     │  │  路线规划     │  │
│  │  面板    │  │  JS API 2.0  │  │   面板       │  │
│  └──────────┘  └──────────────┘  └──────────────┘  │
│        POI搜索 / 路线计算 / 距离查询（前端直调高德）     │
└─────────────────────┬───────────────────────────────┘
                      │ REST API (JSON)
┌─────────────────────▼───────────────────────────────┐
│              Spring Boot 后端 (端口 8080)             │
│  ┌─────────┐ ┌──────────┐ ┌─────────┐ ┌────────┐  │
│  │收藏管理  │ │旅行计划   │ │单天计划  │ │图片存储 │  │
│  └─────────┘ └──────────┘ └─────────┘ └────────┘  │
│              Service 层 → Repository 层              │
└─────────────────────┬───────────────────────────────┘
                      │ JDBC
┌─────────────────────▼───────────────────────────────┐
│                 MySQL (travel_mouse)                  │
└─────────────────────────────────────────────────────┘
```

**职责划分：**
- 前端：所有地图交互（POI 搜索、路线规划、距离/时间计算）、UI 状态管理
- 后端：纯数据 CRUD（收藏、旅行计划、单天计划、目的地、备注图片上传存储）
- 高德 API Key 仅配置在前端 `.env` 文件，后端不接触地图 API
- 图片存储在后端本地文件系统 `backend/uploads/`，数据库只存路径

## 数据模型

### favorites（收藏）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK AUTO_INCREMENT | 主键 |
| name | VARCHAR(200) | 地点名称 |
| address | VARCHAR(500) | 地址 |
| longitude | DECIMAL(10,7) | 经度 |
| latitude | DECIMAL(10,7) | 纬度 |
| category | VARCHAR(50) | 分类（景点/酒店/交通等） |
| note | TEXT | 备注 |
| created_at | DATETIME | 创建时间 |

### travel_plans（旅行计划）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK AUTO_INCREMENT | 主键 |
| name | VARCHAR(200) | 计划名称 |
| start_date | DATE | 起始日期 |
| end_date | DATE | 结束日期 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

### daily_plans（单天计划）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK AUTO_INCREMENT | 主键 |
| travel_plan_id | BIGINT FK | 关联旅行计划 |
| plan_date | DATE | 计划日期 |
| sort_order | INT | 第几天 |
| status | VARCHAR(20) | 状态：draft / done |

### destinations（目的地）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK AUTO_INCREMENT | 主键 |
| daily_plan_id | BIGINT FK | 关联单天计划 |
| name | VARCHAR(200) | 地点名称 |
| address | VARCHAR(500) | 地址 |
| longitude | DECIMAL(10,7) | 经度 |
| latitude | DECIMAL(10,7) | 纬度 |
| category | VARCHAR(50) | 分类 |
| note_text | TEXT | 备注文字 |
| arrive_time | TIME | 到达时间 |
| duration_minutes | INT | 游玩时长（分钟） |
| leave_time | TIME | 离开时间 |
| sort_order | INT | 在待定列表中的顺序 |
| in_route | BOOLEAN | 是否已加入路线 |
| route_order | INT | 在路线中的顺序（未加入为 NULL） |

### destination_images（目的地图片）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK AUTO_INCREMENT | 主键 |
| destination_id | BIGINT FK | 关联目的地 |
| file_path | VARCHAR(500) | 文件存储路径 |
| created_at | DATETIME | 上传时间 |

### route_segments（路线段）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK AUTO_INCREMENT | 主键 |
| daily_plan_id | BIGINT FK | 关联单天计划 |
| from_dest_id | BIGINT FK | 起点目的地 |
| to_dest_id | BIGINT FK | 终点目的地 |
| transport_mode | VARCHAR(20) | 交通方式（driving/walking/transit） |
| duration_minutes | INT | 耗时（分钟） |
| distance_meters | INT | 距离（米） |
| route_order | INT | 在路线中的顺序 |

## 前端页面结构

### 路由

| 路由 | 页面 | 说明 |
|------|------|------|
| `/` | HomeView | 旅行计划列表 + 收藏入口 |
| `/favorites` | FavoritesView | 收藏地址增删改查 |
| `/plan/create` | PlanCreateView | 创建旅行计划（名称+起止日期） |
| `/plan/:id` | PlanDetailView | 计划详情，按天展示 |
| `/plan/:id/day/:dayId` | DayPlanView | 核心页面：单天计划制定 |

### 核心页面布局（DayPlanView）

地图居中 + 左右面板：
- 左侧：待定目的地列表（搜索添加、备注编辑、时间标记）
- 中间：高德地图（标记点、路线绘制、交互）
- 右侧：路线规划面板（有序路线段、交通时间、回退/暂存操作）

### 组件树

```
DayPlanView.vue
├── LeftPanel.vue
│   ├── PendingList.vue
│   │   ├── DestinationCard.vue
│   │   └── AddDestination.vue
│   └── NoteEditor.vue
├── MapContainer.vue
│   ├── AmapInstance（高德地图实例）
│   ├── Markers（目的地标记点）
│   └── RoutePolyline（路线绘制）
└── RightPanel.vue
    ├── RoutePlan.vue
    │   └── RouteSegment.vue
    └── RouteActions.vue
```

### 核心交互流程

1. 用户通过搜索（高德 POI）或地图点选添加目的地 → 进入待定列表
2. 用户点击待定列表中某个目的地的「加入路线」→ 成为当前所在地，地图自动展示到所有未加入目的地的交通时间
3. 用户选择下一个目的地 → 路线追加，地图绘制路线段
4. 点击「回退」→ 移除最后一个路线节点，上一个成为所在地
5. 全部规划完成 → 点击「暂存」→ 保存当天，进入下一天

### 地图逻辑封装（composables）

- `useAmap.js`：地图初始化、标记点管理
- `usePoiSearch.js`：POI 关键词搜索
- `useRoutePlanning.js`：路线规划计算（驾车/步行/公交）

## 后端 REST API

### 统一响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": { }
}
```

### 收藏 `/api/favorites`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/favorites` | 获取收藏列表 |
| POST | `/api/favorites` | 添加收藏 |
| PUT | `/api/favorites/{id}` | 修改收藏 |
| DELETE | `/api/favorites/{id}` | 删除收藏 |

### 旅行计划 `/api/plans`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/plans` | 获取所有旅行计划 |
| POST | `/api/plans` | 创建计划（自动创建 daily_plans） |
| GET | `/api/plans/{id}` | 获取计划详情（含所有天） |
| PUT | `/api/plans/{id}` | 修改计划 |
| DELETE | `/api/plans/{id}` | 删除计划（级联） |

### 单天计划 `/api/plans/{planId}/days/{dayId}`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `.../days/{dayId}` | 获取某天详情 |
| PUT | `.../days/{dayId}/save` | 暂存当天计划 |

### 目的地 `/api/days/{dayId}/destinations`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `.../destinations` | 获取当天所有目的地 |
| POST | `.../destinations` | 添加目的地 |
| PUT | `.../destinations/{id}` | 修改目的地 |
| DELETE | `.../destinations/{id}` | 删除目的地 |
| POST | `.../destinations/{id}/images` | 上传图片 |
| DELETE | `.../destinations/{id}/images/{imgId}` | 删除图片 |

### 路线 `/api/days/{dayId}/route`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `.../route` | 获取当天路线 |
| PUT | `.../route` | 整体更新路线（前端计算后批量提交） |

## 项目目录结构

```
travel-mouse/
├── frontend/
│   ├── index.html
│   ├── package.json
│   ├── vite.config.js
│   ├── .env                       ← VITE_AMAP_KEY / VITE_AMAP_SECURITY_CODE
│   └── src/
│       ├── main.js
│       ├── App.vue
│       ├── router/index.js
│       ├── api/                   ← 后端 API 调用
│       ├── composables/           ← 高德地图逻辑
│       ├── views/                 ← 页面组件
│       └── components/            ← 通用组件
├── backend/
│   ├── pom.xml
│   ├── src/main/java/com/travelmouse/
│   │   ├── TravelMouseApplication.java
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── entity/
│   │   ├── dto/
│   │   └── config/
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── db/schema.sql
│   └── uploads/
└── docs/
```

## 开发运行

- 后端：`cd backend && mvn spring-boot:run`（端口 8080）
- 前端：`cd frontend && npm run dev`（端口 5173，代理 `/api` → 8080）
- 数据库：预先创建 `travel_mouse` 数据库，执行 `backend/src/main/resources/db/schema.sql`

## 高德地图配置

1. 前往 https://lbs.amap.com/ 注册并创建应用
2. 获取 Web端(JS API) 的 Key 和安全密钥
3. 配置到 `frontend/.env`：
   ```
   VITE_AMAP_KEY=你的Key
   VITE_AMAP_SECURITY_CODE=你的安全密钥
   ```

## 错误处理

| 层级 | 策略 |
|------|------|
| 前端 - 地图 API | 调用失败 toast 提示，不阻断操作 |
| 前端 - 后端通信 | axios 拦截器统一处理网络错误/4xx/5xx |
| 后端 - 全局异常 | `@RestControllerAdvice` 统一 `{code, message, data}` |
| 后端 - 业务校验 | 404（不存在）/ 400（参数非法） |
| 文件上传 | 限 5MB，仅 jpg/png/webp |

## 测试策略

| 范围 | 方式 |
|------|------|
| 后端 Service 层 | JUnit 5 单元测试 |
| 后端 Controller 层 | MockMvc 集成测试 |
| 前端 | 手动验证（本地工具，暂不引入前端测试框架） |

## 明确不做（YAGNI）

- 用户认证（本地单用户）
- 多设备同步
- AI 智能路线推荐
- 导出 PDF / 分享功能
- 前端自动化测试
