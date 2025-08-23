
<script setup>
import { ref } from 'vue'  
import { VueFlow, useVueFlow } from '@vue-flow/core'
// 如何在一个组件中创建多个vue-flow，摸索出来的，发现
// 1. vueflow需要一个id字段，用来区分不同的vueflow
// 2. useVueFlow也需要一个id参数用来确定哪个flow
// useVueFlow provides access to the event handlers
const { 
  onNodeDragStart, 
  onNodeDrag,
  onNodeDragStop, 
  onNodeClick, 
  onNodeDoubleClick, 
  onNodeContextMenu, 
  onNodeMouseEnter, 
  onNodeMouseLeave, 
  onNodeMouseMove 
} = useVueFlow(1)
  
const nodes1 = ref([
  {
    id: '1',
    data: { label: 'Node 1' },
    position: { x: 50, y: 50 },
  },
])

const nodes2 = ref([
  {
    id: '2',
    data: { label: 'Node 1' },
    position: { x: 50, y: 50 },
  },
])
  
// bind listeners to the event handlers
onNodeDragStart((event) => {
  console.log('Node drag started', event)
})

onNodeDrag((event) => {
  console.log('Node dragged', event)
})

onNodeDragStop((event) => {
  console.log('Node drag stopped', event)
})


function logEvent(name, data) {
  console.log(name, data)
}
  
// ... and so on  
</script>

<template>
  <div :style="{height: '500px',width: '400px',border: '1px solid'}">
    <!-- 摸索出来的，我发现如果不用id区分的话，vueflow会使用相同的规则渲染，两个flow完全一样 -->
    <VueFlow :id="1" :nodes="nodes1" />
  </div>

  <div :style="{height: '500px',width: '400px',border: '1px solid'}">
    <VueFlow :id="2" :nodes="nodes2" @node-click="logEvent('click', $event)"/>
  </div>
</template>
