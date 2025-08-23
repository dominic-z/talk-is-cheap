<script setup>
import { Panel, VueFlow, useVueFlow,applyChanges } from '@vue-flow/core'

import { ref } from 'vue'


const nodes = ref([
    {
        id: '1',
        position: { x: 0, y: 50 },
        data: { label: 'Node 1', },
    }
])

const edges = ref([])

let nodeId = 1


const { onNodesChange, onEdgesChange, addNodes, addEdges, applyNodeChanges, applyEdgeChanges } = useVueFlow()

function onAddNodes() {
    nodeId += 1

    let x = (100 * nodeId) % 800;
    let y = Math.floor(nodeId / 4) * 200 + 100
    addNodes([{
        id: '' + nodeId,
        position: { x: x, y: y },
        data: { label: 'Node ' + nodeId },
    }])
}

function onAddEdges() {

    addEdges([{
        id: 'e1->' + nodeId,
        source: '1',
        target: '' + nodeId,
        label: 'e1->' + nodeId
    }])
}

// onNodesChange((changes) => {
//     // changes are arrays of type `NodeChange`
//     console.log("onNodesChange", changes)

// })

onEdgesChange((changes) => {
    // changes are arrays of type `EdgeChange`
    console.log("onEdgesChange", changes)
    applyEdgeChanges(changes)
})

const myOnNodesChange = (changes) => {
    // changes are arrays of type `NodeChange` or `EdgeChange`
    console.log("myOnNodesChange", changes)

    // 如果:apply-default="false"，则需要手动进行applyChanges
    // 文档中也提到了，下个大版本不会默认apply change了，也就说下个大版本需要手动applyChange
    // This API is subject to change in the next major release where changes will not be applied automatically anymore.
    // apply changes manually
    //  deprecated，需要改成applyChanges
    applyNodeChanges(changes)
}



</script>

<template>

    <div :style="{ height: '900px', width: '900px', border: '1px solid' }">
        <!-- :apply-default="false"的设置将到导致更新节点不会自动更新，需要applyChanges触发 -->
        <VueFlow :nodes="nodes" :edges="edges" @nodes-change="myOnNodesChange" :apply-default="false">
            <Panel>
                <button @click="onAddNodes">onAddNodes</button>
                <button @click="onAddEdges">onAddEdges</button>
            </Panel>
        </VueFlow>
    </div>
</template>