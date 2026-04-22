<script setup>
import { mdiWindowClose } from '@mdi/js';
import { Background } from '@vue-flow/background';
import { MarkerType, useVueFlow, VueFlow } from '@vue-flow/core';
import { ref, computed, onMounted, watch, nextTick } from 'vue';
import RunnableStageNode from '../flow/node/RunnableStageNode.vue';
import { mdiCheck } from '@mdi/js';
import { mdiCheckCircle } from '@mdi/js';
import StageStartupDetailNav from './StageStartupDetailNav.vue';
import request from '@/utils/request';
import { API_PATHS } from '@/utils/api/paths';
import { TaskStageStatus } from '@/enums/task';
import { useLayout } from '@/utils/useLayout';
import { mdiCloseCircle } from '@mdi/js';
import { mdiTimerSand } from '@mdi/js';
import { mdiClockAlert } from '@mdi/js';

const props = defineProps(['taskStartupId', 'taskName', 'taskVersion', 'taskExecutionId'])

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


const currentStageStartup = ref(null)
// Node click event handler
function onNodeClick({ event, node }) {
    // console.log('Node clicked:', node, event);
    if (!draw.value) {
        draw.value = !draw.value;
    }
    currentStageStartup.value = node.data.stageStartup
}


const draw = ref(false)

async function getTaskDefinition(taskName, taskVersion) {
    return await request.post(API_PATHS.TASK_DEFINITION.DETAILS_QUERY, {
        data: {
            queries: [
                {
                    taskName: taskName,
                    taskVersion: taskVersion,
                }
            ]
        }
    }).then(respBody => {
        // console.log(respBody)

        return respBody.data.taskDefinitionDTOs[0]
    })
}

async function getStageStartups(taskExecutionId) {
    return await request.get(API_PATHS.TASK_INFO.STAGE_STARTUPS, {
        params: {
            taskExecutionId: taskExecutionId
        }
    }).then(respBody => {
        return respBody.data
    })
}


onMounted(() => {
})

const stageStartups = ref(null)
let taskDef = null
let stageIdStageDefMap = new Map()


const { layout } = useLayout()

const { fitView } = useVueFlow()
// 当taskExecution变更，重新请求
watch(() => props.taskExecutionId, async () => {
    if (taskDef == null) {
        taskDef = await getTaskDefinition(props.taskName, props.taskVersion)
        Object.entries(taskDef.stageDefinitionMap).forEach(([stageName, stageDef]) => {
            stageIdStageDefMap.set(stageDef.id, stageDef)
        });
    }

    if (props.taskExecutionId != null) {
        stageStartups.value = await getStageStartups(props.taskExecutionId)

        let newNodes = []
        //     {
        //     id: '3',
        //     position: { x: 350, y: 200 },
        //     type: "RunnableStage",

        //     data: { label: 'Node 3', status: 'running' },
        // }

        let newEdges = []

        //     {
        //     id: 'e1->3',
        //     source: '1',
        //     target: '3',
        //     animated: true,
        //     markerEnd: {
        //         type: MarkerType.Arrow,
        //         color: 'gray',
        //         strokeWidth: 1.5,
        //     },
        // }
        stageStartups.value.forEach((stageStartup) => {
            const stageDef = stageIdStageDefMap.get(stageStartup.stageId)
            if (stageDef == null) {
                return
            }
            const node = {
                id: stageStartup.stageId + '',
                type: "RunnableStage",
                position: { x: 350, y: 200 },
                data: { label: stageDef.name, status: stageStartup.status, stageStartup:stageStartup },
            }
            newNodes.push(node)
            // console.log(taskDef.pointInGraph)
            const pointInStageNames = taskDef.pointInGraph[stageDef.name]
            pointInStageNames?.forEach(pointInStageName => {
                if (!Object.hasOwn(taskDef.stageDefinitionMap, pointInStageName)) {
                    return
                }
                const pointInStageDef = taskDef.stageDefinitionMap[pointInStageName]
                const edge = {
                    id: 'e' + pointInStageDef.id + '->' + stageDef.id,
                    source: pointInStageDef.id + '',
                    target: stageDef.id + '',
                    animated: TaskStageStatus.RUNNING == stageStartup.status,
                    markerEnd: {
                        type: MarkerType.Arrow,
                        color: 'gray',
                        strokeWidth: 1.5,
                    }
                }
                newEdges.push(edge)
            })
        })


        nodes.value = newNodes
        edges.value = newEdges


        // 确保node第一次更新完成，vue-flow的主图渲染完毕，然后更新layout，因为layout方法里面有一些需要读取当前图的方法，所以需要等待图渲染完毕
        await nextTick()
        const nodesAfterLayout = layout(nodes.value, edges.value, 'TB')
        nodes.value = nodesAfterLayout
        // console.log()
        await nextTick()
        fitView()
    }


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
                                    <v-progress-circular v-if="runnableStageProp.data.status == TaskStageStatus.RUNNING"
                                        class="mr-2" color="primary" indeterminate>
                                    </v-progress-circular>
                                    <v-icon v-else-if="runnableStageProp.data.status == TaskStageStatus.SUCCEEDED"
                                        :icon="mdiCheckCircle" :style="{ 'color': 'green' }" />
                                    <v-icon v-else-if="runnableStageProp.data.status == TaskStageStatus.FAILED"
                                        :icon="mdiCloseCircle" :style="{ 'color': 'red' }" />
                                    <v-icon v-else-if="runnableStageProp.data.status == TaskStageStatus.TIME_OUT"
                                        :icon="mdiClockAlert" :style="{ 'color': 'gray' }" />
                                    <v-icon v-else-if="runnableStageProp.data.status == TaskStageStatus.PENDING"
                                        :icon="mdiTimerSand" :style="{ 'color': 'gray' }" />
                                </VFadeTransition>
                                {{ runnableStageProp.data.label }}
                            </template>

                        </RunnableStageNode>
                    </template>


                    <Background></Background>

                </VueFlow>


            </div>

        </v-sheet>



        <!-- 可以用defineModel替代 -->
        <StageStartupDetailNav :stageStartup="currentStageStartup" :draw="draw" @update:draw="() => draw = !draw">
        </StageStartupDetailNav>


    </v-main>

</template>