<template>
  <div class="plan-detail" v-if="plan">
    <h1>{{ plan.name }}</h1>
    <p>{{ plan.startDate }} ~ {{ plan.endDate }}</p>
    <div class="day-list">
      <el-card v-for="day in dailyPlans" :key="day.id" class="day-card" shadow="hover"
               @click="$router.push(`/plan/${plan.id}/day/${day.id}`)">
        <h3>第 {{ day.sortOrder }} 天</h3>
        <p>{{ day.planDate }}</p>
        <el-tag :type="day.status === 'done' ? 'success' : 'warning'" size="small">
          {{ day.status === 'done' ? '已完成' : '草稿' }}
        </el-tag>
      </el-card>
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
.day-card { cursor: pointer; }
</style>
