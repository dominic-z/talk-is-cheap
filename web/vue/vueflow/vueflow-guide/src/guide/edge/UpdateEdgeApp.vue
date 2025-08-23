<script setup>
import { ref, onMounted } from 'vue'
import { VueFlow, useVueFlow, Panel } from '@vue-flow/core'

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
    {
        id: '3',
        position: { x: 250, y: 50 },
        data: { label: 'Node 3', },
    },
    {
        id: '4',
        position: { x: 250, y: 250 },
        data: { label: 'Node 4', },
    },
])

const edges = ref([
    {
        id: 'e1->2',
        source: '1',
        target: '2',
    },
    {
        id: 'e1->3',
        source: '1',
        target: '3',
    },
    {
        id: 'e2->3',
        source: '2',
        target: '3',
    },
    {
        id: 'e2->4',
        source: '2',
        target: '4',
    },
])

const instance = useVueFlow()

function updateEdgeByUseFlow() {
    instance.updateEdgeData('e1->2', { hello: 'world' })
}

function updateEdgeByFind() {
    const edge = instance.findEdge('e1->3')
    edge.label = 'e1->3'
    edge.data.hello = 'world'
    // you can also mutate properties like `selectable` or `animated`
    edge.selectable = !edge.selectable
    edge.animated = !edge.animated

    
}


function updateEdgeByRef(){
    const edge = edges.value.find(e=>e.id=='e2->4')
    edge.data = {hello: 'world'}
    edges.value[3].label = 'e2-4'
    edge.selectable = !edge.selectable
    edge.animated = !edge.animated
    console.log(edges.value)
}

function showEdges(){
    console.log(instance.getEdges)
}

</script>

<template>

    <div :style="{ height: '500px', width: '500px', border: '1px solid' }">

        <!-- 与更新updateNode同理，需要v-model -->
        <VueFlow :nodes="nodes" v-model:edges="edges">
            <Panel>
                <button @click="updateEdgeByUseFlow">updateEdgeByUseFlow</button>
                <button @click="updateEdgeByFind">updateEdgeByFind</button>
                <button @click="updateEdgeByRef">updateEdgeByRef</button>
                <button @click="showEdges">showEdges</button>
            </Panel>
        </VueFlow>

    </div>

</template>