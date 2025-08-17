<script setup>
import { Panel, VueFlow, useVueFlow } from '@vue-flow/core'
import { ref } from 'vue'
const initialNodes = ref([
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
    }
])

const edges = ref([]);

const { addEdges } = useVueFlow()


function addEdgeByUseVueFlow() {
    addEdges([
        {
            id: 'e1->2',
            source: '1',
            target: '2',
            label: 'e1->2'

            // if a node has multiple handles of the same type,
            // you should specify which handle to use by id
            // sourceHandle: null,
            // targetHandle: null,
        }
    ])
}

function addEdgeByRef() {
    edges.value.push({
        id: 'e2->3',
        source: '2',
        target: '3',

        // if a node has multiple handles of the same type,
        // you should specify which handle to use by id
        // sourceHandle: null,
        // targetHandle: null,
    })
}

</script>

<template>
    <div :style="{ height: '500px', width: '500px', border: '1px solid' }">

        <VueFlow :nodes="initialNodes" :edges="edges">
            <Panel>
                <button @click="addEdgeByUseVueFlow">addEdgeByUseVueFlow</button>
                <button @click="addEdgeByRef">addEdgeByRef</button>
            </Panel>

        </VueFlow>
    </div>
</template>