<script setup>
import { mdiWindowClose } from '@mdi/js';
import { Background } from '@vue-flow/background';
import { MarkerType, VueFlow } from '@vue-flow/core';
import { ref } from 'vue';
import RunnableStageNode from '../flow/node/RunnableStageNode.vue';

const nodes = ref([
    {
        id: '1',
        position: { x: 200, y: 100 },
        type: "RunnableStage",
        data: { label: 'Node 1' },
    },
    {
        id: '2',
        position: { x: 50, y: 200 },
        type: "RunnableStage",
        data: { label: 'myNode 2' },
    },
    {
        id: '3',
        position: { x: 350, y: 200 },

        type: "RunnableStage",

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
            color: 'black',
            strokeWidth: 3,
        },
    },

    {
        id: 'e1->3',
        source: '1',
        target: '3',
        animated: true,
        label:'11',
        markerEnd: {
            type: MarkerType.Arrow,
            color: 'gray',
            strokeWidth: 3,
        },
    }
])

// Node click event handler
function onNodeClick({ event, node }) {
    console.log('Node clicked:', node, event);
    if (!draw.value) {
        draw.value = !draw.value;
    }

}

const navWidth = ref(300)

const draw = ref(false)

</script>

<template>
    <v-main>
        <v-sheet class="mx-auto pa-2 pt-6" color="grey-lighten-4">
            <div :style="{ height: '100vh', width: '100wh' }">
                <VueFlow :nodes="nodes" :edges="edges" @node-click="onNodeClick" fit-view-on-init>

                    <template #node-RunnableStage="runnableStageProps">
                        <RunnableStageNode v-bind="runnableStageProps" />
                    </template>


                    <Background></Background>

                </VueFlow>


            </div>

        </v-sheet>


        <v-navigation-drawer v-model="draw" :location="'right'" :width="navWidth" floating="">
            <v-toolbar density="compact">

                <v-btn :icon="mdiWindowClose" @click="draw = !draw"></v-btn>
            </v-toolbar>
            <div class="d-flex mt-2 ml-2 mr-2 ga-2">
                <v-text-field label="taskVersion"></v-text-field>
                <!-- <v-btn :icon="mdiMagnify" variant="plain"></v-btn> -->
            </div>
            <div class="d-flex mt-2 ml-2 mr-2 ga-2">
                <v-text-field label="taskVersion"></v-text-field>
                <!-- <v-btn :icon="mdiMagnify" variant="plain"></v-btn> -->
            </div>
            <div class="d-flex mt-2 ml-2 mr-2 ga-2">
                <v-text-field label="taskVersion"></v-text-field>
                <!-- <v-btn :icon="mdiMagnify" variant="plain"></v-btn> -->
            </div>
            <div class="d-flex mt-2 ml-2 mr-2 ga-2">
                <v-text-field label="taskVersion"></v-text-field>
                <!-- <v-btn :icon="mdiMagnify" variant="plain"></v-btn> -->
            </div>
            <div class="d-flex mt-2 ml-2 mr-2 ga-2">
                <v-text-field label="taskVersion"></v-text-field>
                <!-- <v-btn :icon="mdiMagnify" variant="plain"></v-btn> -->
            </div>
        </v-navigation-drawer>


    </v-main>

</template>