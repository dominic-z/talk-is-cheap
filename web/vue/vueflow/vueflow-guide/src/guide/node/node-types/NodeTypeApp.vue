<!-- 节点的各种内置类型和自定义类型 -->
<script setup>

import { ref,markRaw } from 'vue'
import { Position, VueFlow } from '@vue-flow/core'
import CustomNode from './CustomNode.vue';

const nodeTypes = {
  custom: markRaw(CustomNode),
}

const nodes = ref([
  {
    id: '1',
    type: 'default', // You can omit this as it's the fallback type
    targetPosition: Position.Top, // or Bottom, Left, Right,
    sourcePosition: Position.Bottom, // or Top, Left, Right,
    position: { x: 0, y: 100 },
    data: { label: 'Default Node' },
  },
  {
    id: '2',
    type: 'input',
    sourcePosition: Position.Bottom, // or Top, Left, Right,
    position: { x: 0, y: 200 },
    data: { label: 'Input Node' },
  },
  {
    id: '3',
    type: 'output',
    targetPosition: Position.Top, // or Bottom, Left, Right,
    position: { x: 0, y: 300 },
    data: { label: 'Output Node' },
  },
  {
    id: '4',
    data: { label: 'Node 4' },
    // this will create the node-type `custom`
    type: 'custom',
    class: 'vue-flow__node-custom',
    position: { x: 150, y: 50 },
  },
  {
    id: '5',
    data: { label: 'Node 5' },
    // this will create the node-type `special`
    type: 'special',
    position: { x: 350, y: 50 },
  }
])

</script>

<template>

  <div :style="{ width: '100vw', height: '100vh' }">

    <!-- 通过nodeType指定某个type要用的节点组件，与下面的template是相同的 -->
    <!-- <VueFlow :nodes=nodes :nodeTypes="nodeTypes"> -->
    <VueFlow :nodes=nodes>
      <!-- 含义是，一个type的custom的node的组件是CustomNode，并且其参数会被叫做customNodeProps，其实就是id='4'的那个obj -->
      <template #node-custom="customNodeProps">
        <CustomNode v-bind="customNodeProps" />
      </template>

    </VueFlow>
  </div>
</template>

<!-- 同样不能加scoped -->
<style>

/* 这个会影响所有type是custom的node */
.vue-flow__node-custom {
    background: #9CA8B3;
    color: #fff;
    padding: 10px;
}
</style>