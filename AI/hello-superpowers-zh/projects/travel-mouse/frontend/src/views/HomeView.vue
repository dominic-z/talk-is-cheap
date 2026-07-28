<template>
  <div class="home">
    <h1>我的旅行计划</h1>
    <div class="plan-list" v-if="plans.length">
      <el-card v-for="plan in plans" :key="plan.id" class="plan-card" shadow="hover"
               @click="$router.push(`/plan/${plan.id}`)">
        <template #header>
          <div class="card-header">
            <span>{{ plan.name }}</span>
            <el-button type="danger" size="small" @click.stop="removePlan(plan.id)">删除</el-button>
          </div>
        </template>
        <p>{{ plan.startDate }} ~ {{ plan.endDate }}</p>
      </el-card>
    </div>
    <el-empty v-else description="还没有旅行计划，点击右上角「新建计划」开始吧！" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { getPlans, deletePlan } from '../api/plans'

const plans = ref([])

onMounted(async () => {
  plans.value = await getPlans()
})

const removePlan = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除该计划？', '提示', { type: 'warning' })
    await deletePlan(id)
    plans.value = plans.value.filter(p => p.id !== id)
    ElMessage.success('删除成功')
  } catch {
    // 用户取消
  }
}
</script>

<style scoped>
.home { padding: 2rem; }
.plan-list { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 1rem; margin-top: 1rem; }
.plan-card { cursor: pointer; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
