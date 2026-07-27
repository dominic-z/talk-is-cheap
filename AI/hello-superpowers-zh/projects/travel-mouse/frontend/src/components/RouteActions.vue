<template>
  <div class="route-actions">
    <button class="btn-rollback" @click="$emit('rollback')">↩️ 回退</button>
    <button class="btn-save" @click="$emit('save')">💾 暂存当天</button>
  </div>
</template>

<script setup>
defineEmits(['rollback', 'save'])
</script>

<style scoped>
.route-actions { margin-top: 1rem; display: flex; flex-direction: column; gap: 0.5rem; }
.btn-rollback { padding: 0.5rem; background: #f59e0b; color: #fff; border: none; border-radius: 4px; cursor: pointer; }
.btn-save { padding: 0.5rem; background: #10b981; color: #fff; border: none; border-radius: 4px; cursor: pointer; }
</style>
