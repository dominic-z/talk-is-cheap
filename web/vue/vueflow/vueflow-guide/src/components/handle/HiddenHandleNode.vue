<script setup>
// 这个节点的handle是不可见，但是可用的
import { Position, Handle } from '@vue-flow/core'

const props = defineProps(['data'])
</script>

<template>
  <div :style="{ border: '1px solid', padding: '10px' }">
    <div>{{ props.data.label }}</div>
    <Handle type="source" :position="Position.Bottom" />
    <Handle id="hidden" type="target" :position="Position.Top" style="opacity: 0" />
  </div>
</template>