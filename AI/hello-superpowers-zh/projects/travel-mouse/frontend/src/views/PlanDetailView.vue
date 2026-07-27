<template>
  <div class="plan-detail" v-if="plan">
    <h1>{{ plan.name }}</h1>
    <p>{{ plan.startDate }} ~ {{ plan.endDate }}</p>
    <div class="day-list">
      <div v-for="day in dailyPlans" :key="day.id" class="day-card"
           @click="$router.push(`/plan/${plan.id}/day/${day.id}`)">
        <h3>第 {{ day.sortOrder }} 天</h3>
        <p>{{ day.planDate }}</p>
        <span :class="['status', day.status]">{{ day.status === 'done' ? '已完成' : '草稿' }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getPlan } from '../api/plans'

const route = useRoute()
const plan = ref(null)
const dailyPlans = ref([])

onMounted(async () => {
  const result = await getPlan(route.params.id)
  plan.value = result.plan
  dailyPlans.value = result.dailyPlans
})
</script>

<style scoped>
.plan-detail { padding: 2rem; }
.day-list { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 1rem; margin-top: 1rem; }
.day-card { border: 1px solid #e5e7eb; border-radius: 8px; padding: 1rem; cursor: pointer; }
.day-card:hover { border-color: #2563eb; }
.status { font-size: 0.8rem; padding: 2px 8px; border-radius: 4px; }
.status.done { background: #d1fae5; color: #065f46; }
.status.draft { background: #fef3c7; color: #92400e; }
</style>
