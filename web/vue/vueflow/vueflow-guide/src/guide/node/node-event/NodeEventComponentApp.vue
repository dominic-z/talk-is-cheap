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
} = useVueFlow()
  
const nodes = ref([
  {
    id: '1',
    data: { label: 'Node 1' },
    position: { x: 50, y: 50 },
  },
])

function logEvent(name, data) {
  console.log(name, data)
}
  
// ... and so on  
</script>

<template>
  <div :style="{height: '500px',width: '400px',border: '1px solid'}">
    <!-- bind listeners to the event handlers -->
  <VueFlow
    :nodes="nodes"
    @node-drag-start="logEvent('drag start', $event)"
    @node-drag="logEvent('drag', $event)"
    @node-drag-stop="logEvent('drag stop', $event)"
    @node-click="logEvent('click', $event)"
    @node-double-click="logEvent('dblclick', $event)"
    @node-contextmenu="logEvent('contextmenu', $event)"
    @node-mouse-enter="logEvent('mouseenter', $event)"
    @node-mouse-leave="logEvent('mouseleave', $event)"
    @node-mouse-move="logEvent('mousemove', $event)"
  />
  </div>

</template>
