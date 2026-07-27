<template>
  <div class="home">
    <h1>我的旅行计划</h1>
    <div class="plan-list">
      <div v-for="plan in plans" :key="plan.id" class="plan-card" @click="$router.push(`/plan/${plan.id}`)">
        <h3>{{ plan.name }}</h3>
        <p>{{ plan.startDate }} ~ {{ plan.endDate }}</p>
        <button @click.stop="removePlan(plan.id)">删除</button>
      </div>
      <p v-if="plans.length === 0" class="empty">还没有旅行计划，点击右上角「新建计划」开始吧！</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getPlans, deletePlan } from '../api/plans'

const plans = ref([])

onMounted(async () => {
  plans.value = await getPlans()
})

const removePlan = async (id) => {
  if (confirm('确定删除该计划？')) {
    await deletePlan(id)
    plans.value = plans.value.filter(p => p.id !== id)
  }
}
</script>

<style scoped>
.home { padding: 2rem; }
.plan-list { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 1rem; margin-top: 1rem; }
.plan-card { border: 1px solid #e5e7eb; border-radius: 8px; padding: 1rem; cursor: pointer; }
.plan-card:hover { border-color: #2563eb; }
.plan-card button { margin-top: 0.5rem; padding: 4px 12px; background: #ef4444; color: #fff; border: none; border-radius: 4px; cursor: pointer; }
.empty { color: #9ca3af; }
</style>
