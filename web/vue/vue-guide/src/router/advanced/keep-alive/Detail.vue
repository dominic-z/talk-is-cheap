<!-- views/Detail.vue -->
<template>
    <div class="detail-container">
      <h2>用户详情</h2>
      <p>用户 ID: {{ $route.params.id }}</p>
      <button @click="$router.back()">返回列表</button>
    </div>
  </template>
  
  <script setup>
  // 详情页不需要特殊逻辑
  </script>