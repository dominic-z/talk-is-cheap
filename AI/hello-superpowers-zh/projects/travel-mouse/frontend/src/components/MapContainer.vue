<template>
  <div id="amap-container" class="map-container"></div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useAmap } from '../composables/useAmap'

const { initMap } = useAmap()
const emit = defineEmits(['map-ready'])

onMounted(async () => {
  const { map, AMap } = await initMap('amap-container')
  emit('map-ready', { map, AMap })
})
</script>

<style scoped>
.map-container { width: 100%; height: 100%; }
</style>
