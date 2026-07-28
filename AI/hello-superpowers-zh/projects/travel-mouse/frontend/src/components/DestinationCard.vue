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
<template>
  <div class="dest-card">
    <div class="dest-info">
      <strong>{{ dest.name }}</strong>
      <span class="addr">{{ dest.address }}</span>
      <span v-if="travelTime" class="time">🚗 {{ travelTime }}</span>
    </div>
    <div class="dest-actions">
      <button class="btn-add" @click="$emit('add-to-route')">加入路线</button>
      <button class="btn-del" @click="$emit('remove')">删除</button>
    </div>
  </div>
</template>

<script setup>
defineProps(['dest', 'travelTime'])
defineEmits(['add-to-route', 'remove'])
</script>

<style scoped>
.dest-card { padding: 0.6rem; border: 1px solid #e5e7eb; border-radius: 6px; margin-bottom: 0.5rem; }
.dest-info { display: flex; flex-direction: column; gap: 2px; }
.addr { font-size: 0.8rem; color: #6b7280; }
.time { font-size: 0.8rem; color: #059669; }
.dest-actions { margin-top: 0.4rem; display: flex; gap: 0.5rem; }
.btn-add { font-size: 0.8rem; padding: 2px 8px; background: #2563eb; color: #fff; border: none; border-radius: 4px; cursor: pointer; }
.btn-del { font-size: 0.8rem; padding: 2px 8px; background: #ef4444; color: #fff; border: none; border-radius: 4px; cursor: pointer; }
</style>
