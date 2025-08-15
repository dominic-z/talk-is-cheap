<script setup>

// 通过useVueFlow来修改节点
import { VueFlow, Panel, useVueFlow } from '@vue-flow/core'
import { ref } from 'vue'

const nodes = ref([
  {
    id: '1',
    position: { x: 50, y: 50 },
    data: { label: 'Node 1' },
  },
  {
    id: '2',
    position: { x: 250, y: 50 },
    data: { label: 'Node 2' },
  }
])

const nodeId = '1';
const instance = useVueFlow() // 这个对象一定要在setup中，不能在function中，因为只有这样才能将这个instance注册到当前的上下文中，才能获取当前的flow的对象
function updateNode() {
  // 简单的ref修改也可以
  // nodes.value[0].data.label="world"

  // use the `updateNodeData` method to update the data of an node
  instance.updateNodeData(nodeId, { hello: 'mona',label:"world" })

  // find the node in the state by its id
  const node = instance.findNode('1')

  
  // you can also mutate properties like `selectable` or `draggable`
  node.selectable = false // 不可以被选中
  node.draggable = false // 不可以被拖拽

  // or use `updateNode` to update the node directly
  // instance.updateNode(nodeId, { selectable: false, draggable: false })
}

</script>

<template>
  <div :style="{ height: '500px', width: '1000px' }">
    <VueFlow :nodes="nodes">
      <Panel>
        <button type="button" @click="updateNode">updateNode</button>
      </Panel>
    </VueFlow>
  </div>
</template>

<style lang="css" scoped>
/* import the necessary styles for Vue Flow to work */
@import '@vue-flow/core/dist/style.css';

/* import the default theme, this is optional but generally recommended */
@import '@vue-flow/core/dist/theme-default.css';
</style>