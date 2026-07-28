# UI 优化（Element Plus）实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 将前端自定义 CSS 替换为 Element Plus 组件，统一 UI 风格。

**架构：** 全量引入 Element Plus，逐文件替换现有按钮、卡片、表单等组件为对应的 Element Plus 组件，删除冗余自定义样式。

**技术栈：** Vue 3 + Element Plus + @element-plus/icons-vue

---

## 文件结构

| 文件 | 职责 |
|------|------|
| `frontend/src/main.js` | 应用入口，注册 Element Plus |
| `frontend/src/App.vue` | 根组件，顶部导航 |
| `frontend/src/views/HomeView.vue` | 首页，计划列表 |
| `frontend/src/views/FavoritesView.vue` | 收藏管理 |
| `frontend/src/views/PlanCreateView.vue` | 创建计划表单 |
| `frontend/src/views/PlanDetailView.vue` | 计划详情，天列表 |
| `frontend/src/views/DayPlanView.vue` | 核心页面，三栏布局 |
| `frontend/src/components/AddDestination.vue` | 搜索添加目的地 |
| `frontend/src/components/DestinationCard.vue` | 目的地卡片 |
| `frontend/src/components/PendingList.vue` | 待定列表 |
| `frontend/src/components/RoutePlan.vue` | 路线展示 |
| `frontend/src/components/RouteActions.vue` | 路线操作按钮 |

---

### 任务 1：安装 Element Plus 并配置入口

**文件：**
- 修改：`frontend/package.json`
- 修改：`frontend/src/main.js`

- [ ] **步骤 1：安装依赖**

运行：
```bash
cd frontend
npm install element-plus @element-plus/icons-vue
```

- [ ] **步骤 2：修改 main.js**

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

- [ ] **步骤 3：验证编译**

运行：`cd frontend && npm run build`
预期：BUILD SUCCESS

- [ ] **步骤 4：Commit**

```bash
git add frontend/
git commit -m "feat: 引入 Element Plus UI 组件库"
```

---

### 任务 2：更新 App.vue 导航栏

**文件：**
- 修改：`frontend/src/App.vue`

- [ ] **步骤 1：替换导航栏为 el-menu**

```vue
<template>
  <div id="app">
    <el-menu mode="horizontal" :router="true" :default-active="$route.path">
      <el-menu-item index="/">首页</el-menu-item>
      <el-menu-item index="/favorites">收藏</el-menu-item>
      <el-menu-item index="/plan/create">新建计划</el-menu-item>
    </el-menu>
    <main class="app-main">
      <router-view />
    </main>
  </div>
</template>

<style>
* { margin: 0; padding: 0; box-sizing: border-box; }
body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; }
.app-main { height: calc(100vh - 60px); }
</style>
```

- [ ] **步骤 2：验证页面**

运行：`cd frontend && npm run dev`
预期：导航栏显示为 Element Plus 风格

- [ ] **步骤 3：Commit**

```bash
git add frontend/src/App.vue
git commit -m "feat: 导航栏改用 el-menu"
```

---

### 任务 3：更新 HomeView.vue

**文件：**
- 修改：`frontend/src/views/HomeView.vue`

- [ ] **步骤 1：替换为 Element Plus 组件**

```vue
<template>
  <div class="home">
    <h1>我的旅行计划</h1>
    <div class="plan-list" v-if="plans.length">
      <el-card v-for="plan in plans" :key="plan.id" class="plan-card" shadow="hover"
               @click="$router.push(`/plan/${plan.id}`)">
        <template #header>
          <div class="card-header">
            <span>{{ plan.name }}</span>
            <el-button type="danger" size="small" @click.stop="removePlan(plan.id)">删除</el-button>
          </div>
        </template>
        <p>{{ plan.startDate }} ~ {{ plan.endDate }}</p>
      </el-card>
    </div>
    <el-empty v-else description="还没有旅行计划，点击右上角「新建计划」开始吧！" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { getPlans, deletePlan } from '../api/plans'

const plans = ref([])

onMounted(async () => {
  plans.value = await getPlans()
})

const removePlan = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除该计划？', '提示', { type: 'warning' })
    await deletePlan(id)
    plans.value = plans.value.filter(p => p.id !== id)
    ElMessage.success('删除成功')
  } catch {
    // 用户取消
  }
}
</script>

<style scoped>
.home { padding: 2rem; }
.plan-list { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 1rem; margin-top: 1rem; }
.plan-card { cursor: pointer; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
```

- [ ] **步骤 2：Commit**

```bash
git add frontend/src/views/HomeView.vue
git commit -m "feat: 首页改用 Element Plus 组件"
```

---

### 任务 4：更新 FavoritesView.vue

**文件：**
- 修改：`frontend/src/views/FavoritesView.vue`

- [ ] **步骤 1：替换为 Element Plus 组件**

```vue
<template>
  <div class="favorites">
    <h1>我的收藏</h1>
    <div class="fav-list" v-if="favorites.length">
      <el-card v-for="fav in favorites" :key="fav.id" class="fav-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span>{{ fav.name }}</span>
            <el-button type="danger" size="small" @click="remove(fav.id)">删除</el-button>
          </div>
        </template>
        <p>{{ fav.address }}</p>
        <el-tag v-if="fav.category" size="small" type="info">{{ fav.category }}</el-tag>
      </el-card>
    </div>
    <el-empty v-else description="还没有收藏，在地图页面中点击「收藏」按钮添加。" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getFavorites, deleteFavorite } from '../api/favorites'

const favorites = ref([])

onMounted(async () => {
  favorites.value = await getFavorites()
})

const remove = async (id) => {
  await deleteFavorite(id)
  favorites.value = favorites.value.filter(f => f.id !== id)
  ElMessage.success('删除成功')
}
</script>

<style scoped>
.favorites { padding: 2rem; }
.fav-list { display: grid; grid-template-columns: repeat(auto-fill, minmax(250px, 1fr)); gap: 1rem; margin-top: 1rem; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
```

- [ ] **步骤 2：Commit**

```bash
git add frontend/src/views/FavoritesView.vue
git commit -m "feat: 收藏页改用 Element Plus 组件"
```

---

### 任务 5：更新 PlanCreateView.vue

**文件：**
- 修改：`frontend/src/views/PlanCreateView.vue`

- [ ] **步骤 1：替换为 Element Plus 表单**

```vue
<template>
  <div class="plan-create">
    <h1>新建旅行计划</h1>
    <el-form :model="form" label-width="100px" class="create-form">
      <el-form-item label="计划名称">
        <el-input v-model="form.name" placeholder="如：杭州三日游" />
      </el-form-item>
      <el-form-item label="起始日期">
        <el-date-picker v-model="form.startDate" type="date" placeholder="选择日期"
                        value-format="YYYY-MM-DD" />
      </el-form-item>
      <el-form-item label="结束日期">
        <el-date-picker v-model="form.endDate" type="date" placeholder="选择日期"
                        value-format="YYYY-MM-DD" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="submit">创建</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createPlan } from '../api/plans'

const router = useRouter()
const form = reactive({ name: '', startDate: '', endDate: '' })

const submit = async () => {
  if (!form.name || !form.startDate || !form.endDate) {
    ElMessage.warning('请填写完整信息')
    return
  }
  const plan = await createPlan(form)
  ElMessage.success('创建成功')
  router.push(`/plan/${plan.id}`)
}
</script>

<style scoped>
.plan-create { padding: 2rem; max-width: 500px; }
.create-form { margin-top: 1rem; }
</style>
```

- [ ] **步骤 2：Commit**

```bash
git add frontend/src/views/PlanCreateView.vue
git commit -m "feat: 创建计划页改用 el-form"
```

---

### 任务 6：更新 PlanDetailView.vue

**文件：**
- 修改：`frontend/src/views/PlanDetailView.vue`

- [ ] **步骤 1：替换为 Element Plus 组件**

```vue
<template>
  <div class="plan-detail" v-if="plan">
    <h1>{{ plan.name }}</h1>
    <p>{{ plan.startDate }} ~ {{ plan.endDate }}</p>
    <div class="day-list">
      <el-card v-for="day in dailyPlans" :key="day.id" class="day-card" shadow="hover"
               @click="$router.push(`/plan/${plan.id}/day/${day.id}`)">
        <h3>第 {{ day.sortOrder }} 天</h3>
        <p>{{ day.planDate }}</p>
        <el-tag :type="day.status === 'done' ? 'success' : 'warning'" size="small">
          {{ day.status === 'done' ? '已完成' : '草稿' }}
        </el-tag>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getPlan } from '../api/plans'

const route = useRoute()
const plan = ref(null)
const dailyPlans = ref([])

onMounted(async () => {
  const result = await getPlan(route.params.id)
  plan.value = result.plan
  dailyPlans.value = result.dailyPlans
})
</script>

<style scoped>
.plan-detail { padding: 2rem; }
.day-list { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 1rem; margin-top: 1rem; }
.day-card { cursor: pointer; }
</style>
```

- [ ] **步骤 2：Commit**

```bash
git add frontend/src/views/PlanDetailView.vue
git commit -m "feat: 计划详情页改用 Element Plus 组件"
```

---

### 任务 7：更新 DayPlanView.vue

**文件：**
- 修改：`frontend/src/views/DayPlanView.vue`

- [ ] **步骤 1：替换 alert 为 ElMessage**

在 `<script setup>` 中：
```javascript
import { ElMessage } from 'element-plus'

// 在 saveDay 函数中，将 alert('当天计划已保存！') 替换为：
ElMessage.success('当天计划已保存！')
```

- [ ] **步骤 2：Commit**

```bash
git add frontend/src/views/DayPlanView.vue
git commit -m "feat: DayPlanView 使用 ElMessage 替代 alert"
```

---

### 任务 8：更新左侧面板组件

**文件：**
- 修改：`frontend/src/components/AddDestination.vue`
- 修改：`frontend/src/components/DestinationCard.vue`
- 修改：`frontend/src/components/PendingList.vue`

- [ ] **步骤 1：更新 AddDestination.vue**

```vue
<template>
  <div class="add-dest">
    <el-input v-model="keyword" placeholder="搜索目的地..." @keyup.enter="doSearch" clearable>
      <template #append>
        <el-button @click="doSearch">搜索</el-button>
      </template>
    </el-input>
    <el-scrollbar v-if="results.length" max-height="200px" class="search-results">
      <div v-for="(r, i) in results" :key="i" class="result-item" @click="$emit('select', r)">
        <strong>{{ r.name }}</strong>
        <span>{{ r.address }}</span>
      </div>
    </el-scrollbar>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { usePoiSearch } from '../composables/usePoiSearch'

defineEmits(['select'])

const keyword = ref('')
const results = ref([])
let AMap = null

const doSearch = async () => {
  if (!keyword.value.trim()) return
  if (!AMap) {
    window._AMapSecurityConfig = { securityJsCode: import.meta.env.VITE_AMAP_SECURITY_CODE }
    const loader = await import('@amap/amap-jsapi-loader')
    AMap = await loader.default.load({
      key: import.meta.env.VITE_AMAP_KEY,
      version: '2.0',
      plugins: ['AMap.PlaceSearch']
    })
  }
  const { search } = usePoiSearch(AMap)
  results.value = await search(keyword.value)
}
</script>

<style scoped>
.add-dest { margin-bottom: 1rem; }
.search-results { margin-top: 0.5rem; }
.result-item { padding: 0.5rem; cursor: pointer; border-bottom: 1px solid #f0f0f0; }
.result-item:hover { background: #f5f7fa; }
.result-item span { display: block; font-size: 0.8rem; color: #909399; }
</style>
```

- [ ] **步骤 2：更新 DestinationCard.vue**

```vue
<template>
  <el-card class="dest-card" shadow="never">
    <div class="dest-info">
      <strong>{{ dest.name }}</strong>
      <span class="addr">{{ dest.address }}</span>
      <el-tag v-if="travelTime" type="success" size="small">🚗 {{ travelTime }}</el-tag>
    </div>
    <div class="dest-actions">
      <el-button type="primary" size="small" @click="$emit('add-to-route')">加入路线</el-button>
      <el-button type="danger" size="small" @click="$emit('remove')">删除</el-button>
    </div>
  </el-card>
</template>

<script setup>
defineProps(['dest', 'travelTime'])
defineEmits(['add-to-route', 'remove'])
</script>

<style scoped>
.dest-card { margin-bottom: 0.5rem; }
.dest-info { display: flex; flex-direction: column; gap: 4px; }
.addr { font-size: 0.8rem; color: #909399; }
.dest-actions { margin-top: 0.5rem; display: flex; gap: 0.5rem; }
</style>
```

- [ ] **步骤 3：更新 PendingList.vue**

```vue
<template>
  <div class="pending-list">
    <DestinationCard
      v-for="dest in destinations" :key="dest.id"
      :dest="dest"
      :travel-time="getTravelTime(dest.id)"
      @add-to-route="$emit('add-to-route', dest)"
      @remove="$emit('remove', dest.id)"
    />
    <el-empty v-if="destinations.length === 0" description="暂无待定目的地" :image-size="60" />
  </div>
</template>

<script setup>
import DestinationCard from './DestinationCard.vue'

const props = defineProps(['destinations', 'travelTimes'])
defineEmits(['add-to-route', 'remove'])

const getTravelTime = (destId) => {
  const t = props.travelTimes?.find(tt => tt.destId === destId)
  return t?.durationMinutes != null ? `${t.durationMinutes}分钟` : null
}
</script>
```

- [ ] **步骤 4：Commit**

```bash
git add frontend/src/components/AddDestination.vue frontend/src/components/DestinationCard.vue frontend/src/components/PendingList.vue
git commit -m "feat: 左侧面板组件改用 Element Plus"
```

---

### 任务 9：更新右侧面板组件

**文件：**
- 修改：`frontend/src/components/RoutePlan.vue`
- 修改：`frontend/src/components/RouteActions.vue`

- [ ] **步骤 1：更新 RoutePlan.vue 使用 el-timeline**

```vue
<template>
  <div class="route-plan">
    <el-timeline v-if="routeList.length">
      <el-timeline-item
        v-for="(dest, i) in routeList" :key="dest.id"
        :timestamp="i < routeList.length - 1 ? `驾车 ${getSegmentTime(i)}` : ''"
        placement="top"
      >
        {{ dest.name }}
      </el-timeline-item>
    </el-timeline>
    <el-empty v-else description="路线为空，从左侧添加目的地" :image-size="60" />
  </div>
</template>

<script setup>
const props = defineProps(['routeList', 'segments'])

const getSegmentTime = (index) => {
  const seg = props.segments?.[index]
  return seg ? `${seg.durationMinutes}分钟` : '...'
}
</script>
```

- [ ] **步骤 2：更新 RouteActions.vue**

```vue
<template>
  <div class="route-actions">
    <el-button type="warning" @click="$emit('rollback')">
      <el-icon><RefreshLeft /></el-icon> 回退
    </el-button>
    <el-button type="success" @click="$emit('save')">
      <el-icon><Check /></el-icon> 暂存当天
    </el-button>
  </div>
</template>

<script setup>
import { RefreshLeft, Check } from '@element-plus/icons-vue'
defineEmits(['rollback', 'save'])
</script>

<style scoped>
.route-actions { margin-top: 1rem; display: flex; flex-direction: column; gap: 0.5rem; }
</style>
```

- [ ] **步骤 3：Commit**

```bash
git add frontend/src/components/RoutePlan.vue frontend/src/components/RouteActions.vue
git commit -m "feat: 右侧面板组件改用 Element Plus"
```

---

### 任务 10：验证构建

- [ ] **步骤 1：运行构建**

运行：`cd frontend && npm run build`
预期：BUILD SUCCESS，无错误

- [ ] **步骤 2：启动开发服务器验证**

运行：`cd frontend && npm run dev`
预期：所有页面正常渲染，Element Plus 组件显示正确

- [ ] **步骤 3：最终 Commit**

```bash
git add -A
git commit -m "feat: UI 优化完成，全面使用 Element Plus"
```
