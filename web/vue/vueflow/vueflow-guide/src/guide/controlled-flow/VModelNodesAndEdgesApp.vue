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
    console.log("nodes",nodes.value)
}

function onAddEdges() {

    addEdges([{
        id: 'e1->' + nodeId,
        source: '1',
        target: '' + nodeId,
        label: 'e1->' + nodeId
    }])
    console.log("edges",edges.value)    
}


onEdgesChange((changes) => {
    console.log("onEdgesChange", changes)
    
    
})

const myOnNodesChange = (changes) => {
    console.log("myOnNodesChange", changes)
}



</script>

<template>

    <div :style="{ height: '900px', width: '900px', border: '1px solid' }">
        <!-- nodes使用了v-model，因此节点变更会同步修改我们自己定义的nodes变量，结果上来看，nodes列表确实有增长，而edges只使用了v-bind，因此新增edge并不会对edges列表内容有影响 -->
        <VueFlow v-model:nodes="nodes" :edges="edges" @nodes-change="myOnNodesChange">
            <Panel>
                <button @click="onAddNodes">onAddNodes</button>
                <button @click="onAddEdges">onAddEdges</button>
            </Panel>
        </VueFlow>
    </div>
</template>