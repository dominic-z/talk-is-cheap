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
<template>
  <div class="add-dest">
    <input v-model="keyword" placeholder="搜索目的地..." @keyup.enter="doSearch" />
    <button @click="doSearch">搜索</button>
    <ul v-if="results.length" class="search-results">
      <li v-for="(r, i) in results" :key="i" @click="$emit('select', r)">
        <strong>{{ r.name }}</strong>
        <span>{{ r.address }}</span>
      </li>
    </ul>
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
.add-dest input { width: 70%; padding: 0.4rem; border: 1px solid #d1d5db; border-radius: 4px; }
.add-dest button { padding: 0.4rem 0.8rem; margin-left: 0.5rem; cursor: pointer; }
.search-results { list-style: none; margin-top: 0.5rem; max-height: 200px; overflow-y: auto; }
.search-results li { padding: 0.4rem; cursor: pointer; border-bottom: 1px solid #f3f4f6; }
.search-results li:hover { background: #eff6ff; }
.search-results span { display: block; font-size: 0.8rem; color: #6b7280; }
</style>
