<template>
  <div class="right-panel">
    <h3>路线规划</h3>
    <RoutePlan :route-list="routeList" :segments="segments" />
    <RouteActions @rollback="$emit('rollback')" @save="$emit('save-day')" />
  </div>
</template>

<script setup>
import RoutePlan from './RoutePlan.vue'
import RouteActions from './RouteActions.vue'

defineProps(['routeList', 'segments'])
defineEmits(['rollback', 'save-day'])
</script>

<style scoped>
.right-panel { padding: 1rem; }
</style>
