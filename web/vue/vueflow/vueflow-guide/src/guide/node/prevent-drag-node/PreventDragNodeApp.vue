<!-- 点击会触发写入 -->
<script setup>

import { ref } from 'vue'
import { Position, VueFlow } from '@vue-flow/core'
import CustomNode from './CustomNode.vue';

const nodes = ref([
  {
    id: '1',
    data: { label: 'Node 1' },
    // this will create the node-type `custom`
    type: 'custom',
    position: { x: 150, y: 50 },
  },
  {
    id: '2',
    data: { label: 'Node 2' },
    position: { x: 150, y: 250 },
  }
])

</script>

<template>

  <div :style="{ width: '100vw', height: '100vh' }">

    <VueFlow :nodes=nodes >
      <template #node-custom="customNodeProps">
        <CustomNode v-bind="customNodeProps" />
      </template>

    </VueFlow>
  </div>
</template>
