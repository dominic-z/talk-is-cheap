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
