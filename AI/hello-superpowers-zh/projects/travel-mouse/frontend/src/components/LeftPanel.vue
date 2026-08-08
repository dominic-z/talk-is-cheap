<template>
  <div class="left-panel">
    <h3>待定目的地</h3>
    <AddDestination @select="$emit('add-dest', $event)" />
    <PendingList
      :destinations="destinations"
      :travel-times="travelTimes"
      @add-to-route="$emit('add-to-route', $event)"
      @remove="$emit('remove-dest', $event)"
    />
  </div>
</template>

<script setup>
import AddDestination from './AddDestination.vue'
import PendingList from './PendingList.vue'

defineProps(['dayId', 'destinations', 'travelTimes'])
defineEmits(['add-to-route', 'remove-dest', 'add-dest'])
</script>

<style scoped>
.left-panel { padding: 1rem; }
</style>
