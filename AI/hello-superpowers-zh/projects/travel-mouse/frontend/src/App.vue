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
<template>
  <div id="app">
    <nav class="app-nav">
      <router-link to="/">首页</router-link>
      <router-link to="/favorites">收藏</router-link>
      <router-link to="/plan/create">新建计划</router-link>
    </nav>
    <main class="app-main">
      <router-view />
    </main>
  </div>
</template>

<style>
* { margin: 0; padding: 0; box-sizing: border-box; }
body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; }
.app-nav {
  display: flex; gap: 1rem; padding: 0.75rem 1.5rem;
  background: #fff; border-bottom: 1px solid #e5e7eb;
}
.app-nav a { text-decoration: none; color: #374151; font-weight: 500; }
.app-nav a.router-link-active { color: #2563eb; }
.app-main { height: calc(100vh - 49px); }
</style>
