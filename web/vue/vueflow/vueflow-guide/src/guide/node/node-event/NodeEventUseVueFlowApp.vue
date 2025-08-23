<!-- 节点的事件处理 -->
<script setup>
import { ref } from 'vue'  
import { VueFlow, useVueFlow } from '@vue-flow/core'

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

  
// ... and so on  
</script>

<template>
  <div :style="{height: '500px',width: '400px',border: '1px solid'}">
    <VueFlow :nodes="nodes1" />
  </div>

</template>
