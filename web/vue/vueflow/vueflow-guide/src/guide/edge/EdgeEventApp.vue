<script setup>
import { ref } from 'vue'  
import { VueFlow, useVueFlow } from '@vue-flow/core'

// useVueFlow provides access to the event handlers
const { 
  onEdgeClick,
  onEdgeDoubleClick,
  onEdgeContextMenu,
  onEdgeMouseEnter,
  onEdgeMouseLeave,
  onEdgeMouseMove,
  onEdgeUpdateStart,
  onEdgeUpdate,
  onEdgeUpdateEnd,
} = useVueFlow()
  
const nodes = ref([
  {
    id: '1',
    position: { x: 50, y: 50 },
    data: { label: 'Node 1', },
  },
  {
    id: '2',
    position: { x: 50, y: 250 },
    data: { label: 'Node 2', },
  },
])

const edges = ref([
  {
    id: 'e1->2',
    source: '1',
    target: '2',
  },
])
  
// bind listeners to the event handlers
onEdgeClick((event, edge) => {
  console.log('edge clicked', edge)
})

onEdgeDoubleClick((event, edge) => {
  console.log('edge double clicked', edge)
})

onEdgeContextMenu((event, edge) => {
  console.log('edge context menu', edge)
})
  

function logEvent(eventName, data) {
  console.log(eventName, data)
}
// ... and so on  
</script>

<template>
  <VueFlow :nodes="nodes" :edges="edges" @edge-mouse-enter="logEvent('edge enter', $event)" />
</template>