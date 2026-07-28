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
