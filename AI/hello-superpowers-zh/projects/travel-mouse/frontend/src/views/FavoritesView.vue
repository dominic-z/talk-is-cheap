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
<template>
  <div class="favorites">
    <h1>我的收藏</h1>
    <div class="fav-list">
      <div v-for="fav in favorites" :key="fav.id" class="fav-card">
        <h3>{{ fav.name }}</h3>
        <p>{{ fav.address }}</p>
        <p v-if="fav.category" class="tag">{{ fav.category }}</p>
        <button @click="remove(fav.id)">删除</button>
      </div>
      <p v-if="favorites.length === 0" class="empty">还没有收藏，在地图页面中点击「收藏」按钮添加。</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getFavorites, deleteFavorite } from '../api/favorites'

const favorites = ref([])

onMounted(async () => {
  favorites.value = await getFavorites()
})

const remove = async (id) => {
  await deleteFavorite(id)
  favorites.value = favorites.value.filter(f => f.id !== id)
}
</script>

<style scoped>
.favorites { padding: 2rem; }
.fav-list { display: grid; grid-template-columns: repeat(auto-fill, minmax(250px, 1fr)); gap: 1rem; margin-top: 1rem; }
.fav-card { border: 1px solid #e5e7eb; border-radius: 8px; padding: 1rem; }
.fav-card button { margin-top: 0.5rem; padding: 4px 12px; background: #ef4444; color: #fff; border: none; border-radius: 4px; cursor: pointer; }
.tag { color: #2563eb; font-size: 0.85rem; }
.empty { color: #9ca3af; }
</style>
