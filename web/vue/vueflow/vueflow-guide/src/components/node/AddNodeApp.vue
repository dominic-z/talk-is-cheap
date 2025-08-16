<!-- 如何增加节点 -->
<script setup>
import { ref, onMounted } from 'vue'
import { VueFlow, Panel, useVueFlow } from '@vue-flow/core'

const inputValue = ref("")

const nodes = ref([
    {
        id: '1',
        position: { x: 50, y: 50 },
        data: { label: 'Node 1', },
    }
]);

function addNode() {
    const id = Date.now().toString()

    nodes.value.push({
        id,
        position: { x: 150, y: 50 },
        data: { label: `Node ${id}`, },
    })
}

function updateNode() {
    nodes.value[0].data.label = `Node ${inputValue.value}`;
}
// useVueFlow

const { addNodes } = useVueFlow()
function generateRandomNode() {
  return {
    id: Math.random().toString(),
    position: { x: Math.random() * 500, y: Math.random() * 500 },
    label: 'Random Node',
  }
}

function onAddNode() {
    // add a single node to the graph
    addNodes(generateRandomNode())
}

function onAddNodes() {
    // add multiple nodes to the graph
    addNodes(Array.from({ length: 10 }, generateRandomNode))
}
</script>

<template>
    <div>
        <input type="text" v-model="inputValue"></input>
        <button type="button" @click="updateNode">updateNode</button>
    </div>
    <VueFlow :nodes="nodes">
        <Panel>
            <button type="button" @click="addNode">Add a node</button>
            <button type="button" @click="onAddNode">Add a node</button>
            <button type="button" @click="onAddNodes">Add multiple nodes</button>
        </Panel>
    </VueFlow>
</template>