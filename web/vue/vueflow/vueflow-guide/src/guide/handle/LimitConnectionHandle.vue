<script lang="ts" setup>
import { Position, Handle, type HandleConnectableFunc } from '@vue-flow/core'  
  
const handleConnectable: HandleConnectableFunc = (node, connectedEdges) => {
  // only allow connections if the node has less than 3 connections
  console.log('node,',node)
  console.log('connectedEdges,',connectedEdges)
  return connectedEdges.length < 2
}
</script>

<template>
  <Handle type="source" :position="Position.Bottom" :connectable="handleConnectable" />
</template>