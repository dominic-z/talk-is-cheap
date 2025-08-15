<script setup>
import { VueFlow, Panel,useVueFlow } from '@vue-flow/core'
import { ref } from 'vue'

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

const nodeId = ref('1');
function updateNode() {
    const instance = useVueFlow()

    // use the `updateNodeData` method to update the data of an node
    instance.updateNodeData(nodeId, { hello: 'mona' })

    // find the node in the state by its id
    const node = instance.findNode(nodeId)
    console.log(node)

    // node.data = {
    //     ...node.data,
    //     hello: 'world',
    // }

    // you can also mutate properties like `selectable` or `draggable`
    // node.selectable = false
    // node.draggable = false

    // or use `updateNode` to update the node directly
    // instance.updateNode(nodeId, { selectable: false, draggable: false })
}

</script>

<template>
  <VueFlow :nodes="nodes">
    <Panel >
      <button type="button" @click="updateNode">updateNode</button>
    </Panel>
  </VueFlow>
</template>

<style lang="css" scoped>
/* import the necessary styles for Vue Flow to work */
@import '@vue-flow/core/dist/style.css';

/* import the default theme, this is optional but generally recommended */
@import '@vue-flow/core/dist/theme-default.css';
</style>