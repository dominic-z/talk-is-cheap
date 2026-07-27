<template>
  <div class="plan-create">
    <h1>新建旅行计划</h1>
    <form @submit.prevent="submit">
      <label>计划名称
        <input v-model="form.name" placeholder="如：杭州三日游" required />
      </label>
      <label>起始日期
        <input type="date" v-model="form.startDate" required />
      </label>
      <label>结束日期
        <input type="date" v-model="form.endDate" required />
      </label>
      <button type="submit">创建</button>
    </form>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { createPlan } from '../api/plans'

const router = useRouter()
const form = reactive({ name: '', startDate: '', endDate: '' })

const submit = async () => {
  const plan = await createPlan(form)
  router.push(`/plan/${plan.id}`)
}
</script>

<style scoped>
.plan-create { padding: 2rem; max-width: 400px; }
form { display: flex; flex-direction: column; gap: 1rem; margin-top: 1rem; }
label { display: flex; flex-direction: column; gap: 0.25rem; font-weight: 500; }
input { padding: 0.5rem; border: 1px solid #d1d5db; border-radius: 4px; }
button { padding: 0.5rem 1rem; background: #2563eb; color: #fff; border: none; border-radius: 4px; cursor: pointer; }
</style>
