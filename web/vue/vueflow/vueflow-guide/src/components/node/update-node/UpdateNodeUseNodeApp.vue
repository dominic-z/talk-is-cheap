<script setup>

// 通过useVueFlow来修改节点
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

<style lang="css">
/* import the necessary styles for Vue Flow to work */
@import '@vue-flow/core/dist/style.css';

/* import the default theme, this is optional but generally recommended */
@import '@vue-flow/core/dist/theme-default.css';
</style>