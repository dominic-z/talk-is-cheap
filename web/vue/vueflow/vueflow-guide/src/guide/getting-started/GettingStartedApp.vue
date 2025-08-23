<!-- 一个入门小flow，初步展示vue-flow的功能 -->
<!-- 按照https://vueflow.dev/guide/getting-started.html章节提供的代码实现的，但需要在这个代码基础上做些修改 -->
<!-- 1：需要给vueflow新增一个容器并设定一个长度和宽度 -->
<!-- 2：specialNode里面漏了一个prop -->
<!-- 没有研究太细致，大致看了一下规则，有个初步印象 -->
<script setup>
import { ref } from 'vue'
import { VueFlow } from '@vue-flow/core'

// these components are only shown as examples of how to use a custom node or edge
// you can find many examples of how to create these custom components in the examples page of the docs
import SpecialNode from './SpecialNode.vue'
import SpecialEdge from './SpecialEdge.vue'

// these are our nodes
const nodes = ref([
  // an input node, specified by using `type: 'input'`
  { 
    id: '1',
    type: 'input', 
    position: { x: 250, y: 5 },
    // all nodes can have a data object containing any data you want to pass to the node
    // a label can property can be used for default nodes
    data: { label: 'Node 1' },
  },

  // default node, you can omit `type: 'default'` as it's the fallback type
  { 
    id: '2', 
    position: { x: 100, y: 100 },
    data: { label: 'Node 2' },
  },

  // An output node, specified by using `type: 'output'`
  { 
    id: '3', 
    type: 'output', 
    position: { x: 400, y: 200 },
    data: { label: 'Node 3' },
  },

  // this is a custom node
  // we set it by using a custom type name we choose, in this example `special`
  // the name can be freely chosen, there are no restrictions as long as it's a string
  {
    id: '4',
    type: 'special', // <-- this is the custom node type name
    position: { x: 400, y: 50 },
    data: {
      label: 'Node 4',
      hello: 'world',
    },
  },
])

// these are our edges
const edges = ref([
  // default bezier edge
  // consists of an edge id, source node id and target node id
  { 
    id: 'e1->2',
    source: '1', 
    target: '2',
  },

  // set `animated: true` to create an animated edge path
  { 
    id: 'e2->3',
    source: '2', 
    target: '3', 
    animated: true,
  },

  // a custom edge, specified by using a custom type name
  // we choose `type: 'special'` for this example
  {
    id: 'e3->4',
    type: 'special',
    source: '3',
    target: '4',

    // all edges can have a data object containing any data you want to pass to the edge
    data: {
      hello: 'world',
    }
  },
])
</script>

<template>
  <div :style="{height: '500px',width:'1000px'}">
    <VueFlow :nodes="nodes" :edges="edges">
    <!-- bind your custom node type to a component by using slots, slot names are always `node-<type>` -->
    <template #node-special="specialNodeProps">
      <SpecialNode v-bind="specialNodeProps" />
    </template>

    <!-- bind your custom edge type to a component by using slots, slot names are always `edge-<type>` -->
    <template #edge-special="specialEdgeProps">
      <SpecialEdge v-bind="specialEdgeProps" />
    </template>
  </VueFlow>
  </div>
</template>