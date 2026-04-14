<template>
    <div style="padding: 20px;">
      <h2>📄 列表页 (发送数据)</h2>
      <p>点击下方按钮跳转到详情页，并传递一些数据（这些数据不会出现在 URL 中）。</p>
      <button @click="goToDetail">跳转到详情页并传递 State</button>
    </div>
  </template>
  
  <script setup>
  import { useRouter } from 'vue-router'
  
  const router = useRouter()
  
  const goToDetail = () => {
    // 要传递的复杂数据对象
    const payload = {
      userId: Math.random(),
      userInfo: { name: 'Alice', age: 25, tags: ['frontend', 'vue'] },
      searchQuery: { category: 'tech', dateRange: [1672531200000, 1675209600000] },
      // 一些不想暴露在 URL 上的临时 token
      tempToken: 'temp_token_xyz_abc'
    }
  
    // 使用 router.push 的 state 属性传递数据
    router.push({
      name: 'Detail', // 使用命名路由
      state: payload  // 这里的数据不会出现在 URL 中
    })
  }
  </script>