<template>
  <div class="pending-list">
    <DestinationCard
      v-for="dest in destinations" :key="dest.id"
      :dest="dest"
      :travel-time="getTravelTime(dest.id)"
      @add-to-route="$emit('add-to-route', dest)"
      @remove="$emit('remove', dest.id)"
    />
    <el-empty v-if="destinations.length === 0" description="暂无待定目的地" :image-size="60" />
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
