<template>
  <div class="route-plan">
    <div v-for="(dest, i) in routeList" :key="dest.id" class="route-item">
      <div class="route-dest">{{ i + 1 }}️⃣ {{ dest.name }}</div>
      <div v-if="i < routeList.length - 1" class="route-seg">
        ↓ 驾车 {{ getSegmentTime(i) }}
      </div>
    </div>
    <p v-if="routeList.length === 0" class="empty">路线为空，从左侧添加目的地</p>
  </div>
</template>

<script setup>
const props = defineProps(['routeList', 'segments'])

const getSegmentTime = (index) => {
  const seg = props.segments?.[index]
  return seg ? `${seg.durationMinutes}分钟` : '...'
}
</script>

<style scoped>
.route-item { margin-bottom: 0.25rem; }
.route-dest { padding: 0.4rem; background: #f9fafb; border-radius: 4px; }
.route-seg { text-align: center; font-size: 0.8rem; color: #6b7280; padding: 0.2rem; }
.empty { color: #9ca3af; font-size: 0.9rem; }
</style>
