<template>
  <div class="route-plan">
    <el-timeline v-if="routeList.length">
      <el-timeline-item
        v-for="(dest, i) in routeList" :key="dest.id"
        :timestamp="i < routeList.length - 1 ? `驾车 ${getSegmentTime(i)}` : ''"
        placement="top"
      >
        {{ dest.name }}
      </el-timeline-item>
    </el-timeline>
    <el-empty v-else description="路线为空，从左侧添加目的地" :image-size="60" />
  </div>
</template>

<script setup>
const props = defineProps(['routeList', 'segments'])

const getSegmentTime = (index) => {
  const seg = props.segments?.[index]
  return seg ? `${seg.durationMinutes}分钟` : '...'
}
</script>
