<script setup>
import { mdiWindowClose } from '@mdi/js';
import { Background } from '@vue-flow/background';
import { MarkerType, VueFlow } from '@vue-flow/core';
import { ref, computed } from 'vue';
import RunnableStageNode from '../flow/node/RunnableStageNode.vue';
import { mdiCheck } from '@mdi/js';
import { mdiCheckCircle } from '@mdi/js';

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
        data: { label: 'myNode 2', hasMenu: true },
    },
    {
        id: '3',
        position: { x: 350, y: 200 },
        type: "RunnableStage",

        data: { label: 'Node 3', status: 'running' },
    }
])

const edges = ref([
    {
        id: 'e1->2',
        source: '1',
        target: '2',
        markerEnd: {
            type: MarkerType.Arrow,
            color: 'gray',
            strokeWidth: 1.5,
        },
    },

    {
        id: 'e1->3',
        source: '1',
        target: '3',
        animated: true,
        markerEnd: {
            type: MarkerType.Arrow,
            color: 'gray',
            strokeWidth: 1.5,
        },
    }
])


// Node click event handler
function onNodeClick({ event, node }) {
    console.log('Node clicked:', node, event);
    if (!draw.value) {
        draw.value = !draw.value;
    }
    nodes.value[2].data.status = ''


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
                        <RunnableStageNode v-bind="runnableStageProps">
                            <template v-slot:content>


                                <VFadeTransition leave-absolute="true">
                                    <v-progress-circular v-if="runnableStageProps.data.status == 'running'" class="mr-2"
                                        color="primary" indeterminate>
                                    </v-progress-circular>
                                    <v-icon v-else :icon="mdiCheckCircle" :style="{ 'color': 'green' }" />

                                </VFadeTransition>
                                {{ runnableStageProps.data.label }}
                                

                                
                            </template>
                        </RunnableStageNode>
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