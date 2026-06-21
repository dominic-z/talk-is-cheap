<script setup>
import { ref, onMounted,markRaw } from 'vue'
import { VueFlow, useVueFlow, Panel } from '@vue-flow/core'
import CustomEdge from './CustomEdge.vue'


// 这种方式也可以
// const edgeTypes = {
//   custom: markRaw(CustomEdge),
// }

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
        position: { x: 250, y: 400 },
        data: { label: 'Node 4', },
    },
    {
        id: '5',
        position: { x: 450, y: 250 },
        data: { label: 'Node 5', },
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
        type: 'step',
        source: '1',
        target: '3',
    },
    {
        id: 'e2->3',
        type: 'smoothstep',
        source: '2',
        target: '3',
    },
    {
        id: 'e2->4',
        type: 'straight',
        source: '2',
        target: '4',
    },
    {
        id: 'e3->4',
        type: 'custom',
        source: '3',
        target: '4',
        label: 'my'
    }
])


</script>

<template>

    <div :style="{ height: '500px', width: '700px', border: '1px solid' }">

        <VueFlow :nodes="nodes" :edges="edges">
            <template #edge-custom="customEdgeProps">
                <CustomEdge v-bind="customEdgeProps" />
            </template>
        </VueFlow>

    </div>

</template>