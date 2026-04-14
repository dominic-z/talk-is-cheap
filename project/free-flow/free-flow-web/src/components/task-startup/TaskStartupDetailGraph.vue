<script setup>
import { mdiWindowClose } from '@mdi/js';
import { Background } from '@vue-flow/background';
import { MarkerType, VueFlow } from '@vue-flow/core';
import { ref, computed,onMounted } from 'vue';
import RunnableStageNode from '../flow/node/RunnableStageNode.vue';
import { mdiCheck } from '@mdi/js';
import { mdiCheckCircle } from '@mdi/js';
import StageStartupDetailNav from './StageStartupDetailNav.vue';
import request from '@/utils/request';
import { API_PATHS } from '@/utils/api/paths';

const props = defineProps(['taskStartupId','taskName','taskVersion'])


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
    stageDetailId.value = node.id


}


const draw = ref(false)
let taskDef = null


async function getTaskDefinition(taskName,taskVersion) {
    return await request.post(API_PATHS.TASK_DEFINITION.DETAILS_QUERY,{
        data:{
            query:{
                taskName:taskName,
                taskVersion:taskVersion,
            }
        }
    }).then(respBody=>{
        // console.log(respBody)
        return respBody.data.taskDefinitionDTOs[0]
    })
}



onMounted(()=>{
    taskDef = getTaskDefinition(props.taskName,props.taskVersion)

})

</script>

<template>
    <v-main>

        <v-progress-linear color="primary" indeterminate></v-progress-linear>
        <v-sheet class="mx-auto pa-2 pt-6" color="grey-lighten-4">

            <div :style="{ height: '100vh', width: '100wh' }">
                <VueFlow :nodes="nodes" :edges="edges" @node-click="onNodeClick" fit-view-on-init>

                    <template #node-RunnableStage="runnableStageProp">
                        <RunnableStageNode v-bind="runnableStageProp">
                            <template v-slot:content>
                                <VFadeTransition :leave-absolute="true">
                                    <v-progress-circular v-if="runnableStageProp.data.status == 'running'" class="mr-2"
                                        color="primary" indeterminate>
                                    </v-progress-circular>
                                    <v-icon v-else :icon="mdiCheckCircle" :style="{ 'color': 'green' }" />
                                </VFadeTransition>
                                {{ runnableStageProp.data.label }}
                            </template>

                        </RunnableStageNode>
                    </template>


                    <Background></Background>

                </VueFlow>


            </div>

        </v-sheet>




        <StageStartupDetailNav :id="stageDetailId" :draw="draw" @update:draw="() => draw = !draw"></StageStartupDetailNav>


    </v-main>

</template>