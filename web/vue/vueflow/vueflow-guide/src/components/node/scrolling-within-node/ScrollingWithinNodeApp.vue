<!-- 滚轮不会触发缩放，而是会进行滚动 -->
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
  }
])

</script>

<template>

  <div :style="{ width: '100vw', height: '100vh' }" >

    <VueFlow :nodes=nodes :no-wheel-class-name="my-nowheel">
      <!-- 含义是，一个type的custom的node的组件是CustomNode，并且其参数会被叫做customNodeProps，其实就是id='4'的那个obj -->
      <template #node-custom="customNodeProps">
        <CustomNode v-bind="customNodeProps" />
      </template>

    </VueFlow>
  </div>
</template>

<style>
.my-nowheel{
  background-color: orange;
  height: 100px;
  width: 200px;
  /* scroll */

  overflow-y: auto;
}
</style>