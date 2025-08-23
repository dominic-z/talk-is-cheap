<script setup>
import { Position, Handle } from '@vue-flow/core'

// props were passed from the slot using `v-bind="customNodeProps"`
const props = defineProps(['data'])
console.log(props)
</script>

<template>
  <div>
    <Handle type="target" :position="Position.Top" />
    <div>{{ props.data.label }}</div>
    <Handle type="source" :position="Position.Bottom" />
  </div>
</template>