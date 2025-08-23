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

const { removeEdges } = useVueFlow()

function removeOneEdge() {
    removeEdges('e1->2')
}

function removeMultipleEdges() {
    removeEdges(['e1->3', 'e2->3'])
}

function removeEdgeByRef() {
    edges.value.splice(0, 1);
}
</script>

<template>
    <div :style="{ height: '500px', width: '500px', border: '1px solid' }">

        <VueFlow :nodes="nodes" :edges="edges">
            <Panel>
                <button @click="removeOneEdge">Remove Edge 1</button>
                <button @click="removeMultipleEdges">Remove Edges 2 and 3</button>
                <button @click="removeEdgeByRef">removeEdgeByRef</button>
            </Panel>
        </VueFlow>

    </div>

</template>