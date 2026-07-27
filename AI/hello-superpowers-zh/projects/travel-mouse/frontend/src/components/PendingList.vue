<template>
  <div class="pending-list">
    <DestinationCard
      v-for="dest in destinations" :key="dest.id"
      :dest="dest"
      :travel-time="getTravelTime(dest.id)"
      @add-to-route="$emit('add-to-route', dest)"
      @remove="$emit('remove', dest.id)"
    />
    <p v-if="destinations.length === 0" class="empty">暂无待定目的地</p>
  </div>
</template>

<script setup>
import DestinationCard from './DestinationCard.vue'

const props = defineProps(['destinations', 'travelTimes'])
defineEmits(['add-to-route', 'remove'])

const getTravelTime = (destId) => {
  const t = props.travelTimes?.find(tt => tt.destId === destId)
  return t?.durationMinutes != null ? `${t.durationMinutes}分钟` : null
}
</script>

<style scoped>
.empty { color: #9ca3af; font-size: 0.9rem; }
</style>
