<script setup>
import { ref, nextTick } from 'vue'
import { VueFlow, Panel } from '@vue-flow/core'

const nodes = ref([
  {
    id: '1',
    position: { x: 50, y: 50 },
    data: {
      label: 'Node 1',
      hello: 'world',
    }
  },
])

async function onSomeEvent(nodeId) {
  // filter的响应会失效，需要写成传统的for循环
  // const node = nodes.value.find((node) => node.id === nodeId)

  // node.data = {
  //   // ...nodes.value[0].data,
  //   hello: 'world',
  //   label: 'hello'
  // }

  // // you can also mutate properties like `selectable` or `draggable`
  // node.selectable = false
  // node.draggable = false

  // 需要这么写
  for (let node of nodes.value) {
    if (node.id === nodeId) {
      // node.data = {
      //   hello: 'world',
      //   label: 'hello11'
      // }
      node.data.label = 'ass'
      // node.selectable = false
      // node.draggable = false
      console.log(node)

    }
  }
}
</script>

<template>
  <div :style="{ height: '100vh', width: '100vw' }">
    <VueFlow :nodes="nodes">
      <Panel>
        <button type="button" @click="onSomeEvent('1')">Update Node 1</button>
      </Panel>
    </VueFlow>
  </div>

</template>