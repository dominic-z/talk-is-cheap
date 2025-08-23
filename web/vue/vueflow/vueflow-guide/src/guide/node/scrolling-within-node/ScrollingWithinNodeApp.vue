<!-- 滚轮不会触发缩放，而是会进行滚动，但是我没看出来noWheel这个class有啥用，我没加这个class只加了个滚动的样式也没发现什么问题 -->
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

  <div :style="{ width: '100vw', height: '100vh' }" >

    <VueFlow :nodes=nodes >
      <template #node-custom="customNodeProps">
        <CustomNode v-bind="customNodeProps" />
      </template>

    </VueFlow>
  </div>
</template>

