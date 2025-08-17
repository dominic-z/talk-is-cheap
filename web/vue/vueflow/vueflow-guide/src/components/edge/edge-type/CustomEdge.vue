<script setup>
import { BezierEdge } from '@vue-flow/core';

// props were passed from the slot using `v-bind="customEdgeProps"`
const props = defineProps(['sourceX', 'sourceY', 'targetX', 'targetY', 'sourcePosition', 'targetPosition','label']);

console.log(props)

</script>


<template>
  <BezierEdge
      :source-x="props.sourceX"
      :source-y="props.sourceY"
      :target-x="props.targetX"
      :target-y="props.targetY"
      :source-position="props.sourcePosition"
      :target-position="props.targetPosition"
      :label="props.label"
  />
</template>