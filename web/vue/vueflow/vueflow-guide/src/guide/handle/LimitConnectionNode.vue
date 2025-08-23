<script setup>
// 自定义节点的handel
import { Position, Handle } from '@vue-flow/core'
// import LimitConnectionHandle from './LimitConnectionHandle.vue'

const props = defineProps(['data'])

// 这个东西的作用：不能通过鼠标点击一个节点的source+拖拽的方式让一个edge从一个source链接到这个handle节点
// 具体的体现就是：拖拽的时候，如果一个节点可以建立链接，那么会有一个磁铁吸引的效果；而如果handleConnectable返回的是false，那么就不会有磁铁吸引的效果。并且鼠标移动到这个handle的时候，如果handleConnectable=false，也无法从这个handle里拖拽出一个edge
// 但是，如果通过js的形式创建了一个edge，比如使用useVueFlow的instance创建一个edge，那还是能够创建的
const handleConnectable = (node, connectedEdges) => {
  // only allow connections if the node has less than 3 connections
  console.log('node,',node)
  console.log('connectedEdges,',connectedEdges)
  return connectedEdges.length < 2
}
</script>

<template>
  <div :style="{ border: '1px solid', padding: '10px' }">
    <Handle type="target" :position="Position.Top" />
    <div>{{ props.data.label }}</div>
    <Handle type="source" :position="Position.Bottom" :connectable="handleConnectable" />
  </div>
</template>