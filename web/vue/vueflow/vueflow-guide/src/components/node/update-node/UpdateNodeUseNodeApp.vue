<script setup>

// 通过useVueNode来修改节点，useXX是vue中会产生副作用的函数，useVueNode就是获取这个节点，并对节点进行一些处理
import CustomNode from '@/components/node/update-node/CustomNode.vue';
import { VueFlow, Panel, useVueFlow } from '@vue-flow/core'
import { ref } from 'vue'

const nodes = ref([
  {
    id: '1',
    position: { x: 50, y: 50 },
    data: { label: 'Node 1' },
    type: 'custom',
  },
  {
    id: '2',
    position: { x: 250, y: 50 },
    data: { label: 'Node 2' },
    type: 'custom',
  }
])

</script>

<template>
  <div :style="{ height: '100vh', width: '100vw' }">
    <VueFlow :nodes="nodes">
      <template #node-custom="nodes">
        <CustomNode v-bind="nodes" />
      </template>
    </VueFlow>
  </div>
</template>
