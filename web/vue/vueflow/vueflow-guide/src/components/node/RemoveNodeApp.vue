<script setup>
import { ref } from 'vue'
import { VueFlow, Panel,useVueFlow } from '@vue-flow/core'

const nodes = ref([
  {
    id: '1',
    position: { x: 50, y: 50 },
    data: { label: 'Node 1' },
  },
  {
    id: '2',
    position: { x: 150, y: 50 },
    data: { label: 'Node 2' },
  }
])

function removeNode(id) {
  nodes.value = nodes.value.filter((node) => node.id !== id)
}


const { removeNodes } = useVueFlow()

function removeOneNode() {
  removeNodes('1')
}

function removeMultipleNodes() {
  removeNodes(['1', '2'])
}
</script>

<template>
  <VueFlow :nodes="nodes">
    <Panel >
      <button type="button" @click="removeNode('1')">Remove Node 1</button>
      <button type="button" @click="removeNode('2')">Remove Node 2</button>
    </Panel>
    <Panel :style="{left:'500px'}">
      <button type="button" @click="removeOneNode">removeNodes Remove Node 1</button>
      <button type="button" @click="removeMultipleNodes">removeNodes Remove Node 1 and 2</button>
    </Panel>
  </VueFlow>
</template>

<style lang="css" scoped>
/* import the necessary styles for Vue Flow to work */
@import '@vue-flow/core/dist/style.css';

/* import the default theme, this is optional but generally recommended */
@import '@vue-flow/core/dist/theme-default.css';
</style>