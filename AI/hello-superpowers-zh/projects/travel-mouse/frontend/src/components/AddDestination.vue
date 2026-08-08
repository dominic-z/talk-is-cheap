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
