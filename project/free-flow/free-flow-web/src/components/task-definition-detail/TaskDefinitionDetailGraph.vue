<script setup>
import { VueFlow } from '@vue-flow/core';
import { ref } from 'vue';
import { MarkerType, } from '@vue-flow/core'

const nodes = ref([
    {
        id: '1',
        position: { x: 200, y: 100 },
        data: { label: 'Node 1' },
    },
    {
        id: '2',
        position: { x: 50, y: 200 },
        data: { label: 'myNode 2' },
    },
    {
        id: '3',
        position: { x: 350, y: 200 },
        data: { label: 'Node 3' },
    }
])

const edges = ref([
    {
        id: 'e1->2',
        source: '1',
        target: '2',
        markerEnd: {
            type: MarkerType.ArrowClosed,
            color: '#ff0072',
            width: 20,
            height: 30,
        },
    },

    {
        id: 'e1->3',
        source: '1',
        target: '3',
        animated: true,
    }
])
</script>

<template>
    <v-main>
        <v-sheet class="mx-auto pa-2 pt-6" color="grey-lighten-4">
            <div :style="{ height: '100vh', width: '100wh' }">
                <VueFlow :nodes="nodes" :edges="edges"></VueFlow>
            </div>

        </v-sheet>
    </v-main>

</template>