<!-- views/List.vue -->
<template>
    <div class="list-container">
      <h2>用户列表 (Keep-Alive 演示)</h2>
      
      <!-- 筛选条件 -->
      <div class="filter">
        <input v-model="keyword" placeholder="搜索用户..." />
        <button @click="handleSearch">搜索</button>
      </div>
  
      <!-- 列表内容 -->
      <div class="list-items">
        <div 
          v-for="item in list" 
          :key="item.id" 
          class="item"
          @click="goDetail(item)"
        >
          {{ item.name }} - {{ item.role }}
        </div>
      </div>
  
      <!-- 分页 -->
      <div class="pagination">
        <button :disabled="page === 1" @click="changePage(page - 1)">上一页</button>
        <span>第 {{ page }} 页</span>
        <button @click="changePage(page + 1)">下一页</button>
      </div>
    </div>
  </template>
  
  <script setup>
  import { ref, onMounted, onActivated } from 'vue'
  import { useRouter, useRoute } from 'vue-router'
  
  const router = useRouter()
  const route = useRoute()
  
  // 状态定义
  const list = ref([])
  const page = ref(1)
  const keyword = ref('')
  const loading = ref(false)
  
  // 模拟数据请求
  const fetchData = async () => {
    loading.value = true
    console.log('🚀 正在请求接口...') // 用于观察控制台
    
    // 模拟网络延迟
    await new Promise(resolve => setTimeout(resolve, 500))
    
    // 模拟数据
    list.value = Array.from({ length: 10 }).map((_, i) => ({
      id: (page.value - 1) * 10 + i,
      name: `用户 ${(page.value - 1) * 10 + i + 1}`,
      role: keyword.value ? `搜索:${keyword.value}` : '普通用户'
    }))
    
    loading.value = false
  }
  
  // 1. 页面首次加载
  onMounted(() => {
    console.log('🟢 onMounted: 组件首次挂载')
    fetchData()
  })
  
  // 2. 组件被激活（从详情页返回时触发）
  onActivated(() => {
    console.log('🟡 onActivated: 组件从缓存中恢复')
    // 注意：这里不需要重新 fetchData()，因为 keep-alive 会保留数据状态
    // 如果需要检测数据是否过期，可以在这里加逻辑
  })
  
  // 业务逻辑方法
  const goDetail = (item) => {
    router.push(`/detail/${item.id}`)
  }
  
  const changePage = (newPage) => {
    page.value = newPage
    fetchData()
  }
  
  const handleSearch = () => {
    page.value = 1
    fetchData()
  }
  </script>
  
  <style scoped>
  .list-container { padding: 20px; max-width: 600px; margin: 0 auto; }
  .item { padding: 10px; border-bottom: 1px solid #eee; cursor: pointer; }
  .item:hover { background: #f5f5f5; }
  .filter { margin-bottom: 20px; }
  .pagination { margin-top: 20px; display: flex; gap: 10px; align-items: center; }
  </style>