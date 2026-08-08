<template>
  <div class="plan-create">
    <h1>新建旅行计划</h1>
    <el-form :model="form" label-width="100px" class="create-form">
      <el-form-item label="计划名称">
        <el-input v-model="form.name" placeholder="如：杭州三日游" />
      </el-form-item>
      <el-form-item label="起始日期">
        <el-date-picker v-model="form.startDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" />
      </el-form-item>
      <el-form-item label="结束日期">
        <el-date-picker v-model="form.endDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="submit">创建</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createPlan } from '../api/plans'

const router = useRouter()
const form = reactive({ name: '', startDate: '', endDate: '' })

const submit = async () => {
  if (!form.name || !form.startDate || !form.endDate) {
    ElMessage.warning('请填写完整信息')
    return
  }
  const plan = await createPlan(form)
  ElMessage.success('创建成功')
  router.push(`/plan/${plan.id}`)
}
</script>

<style scoped>
.plan-create { padding: 2rem; max-width: 500px; }
.create-form { margin-top: 1rem; }
</style>
