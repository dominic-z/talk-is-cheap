<template>
  <div class="route-actions">
    <el-button type="warning" @click="$emit('rollback')">
      <el-icon><RefreshLeft /></el-icon> 回退
    </el-button>
    <el-button type="success" @click="$emit('save')">
      <el-icon><Check /></el-icon> 暂存当天
    </el-button>
  </div>
</template>

<script setup>
import { RefreshLeft, Check } from '@element-plus/icons-vue'
defineEmits(['rollback', 'save'])
</script>

<style scoped>
.route-actions { margin-top: 1rem; display: flex; flex-direction: column; gap: 0.5rem; }
</style>
