<script setup>
import { mdiWindowClose } from '@mdi/js'
import { ref, watch,computed } from 'vue'
import StageExecutionLogTable from './StageExecutionLogTable.vue'
import request from '@/utils/request'
import { API_PATHS } from '@/utils/api/paths'
const emitter = defineEmits(['update:draw'])

const props = defineProps(['draw', 'stageStartup'])
const navWidth = ref(600)

const executions = ref([
    {
        'stageExecutionId': 1,
        'launchTime': '2020-08-01 12:20:10',
    },
    {
        'stageExecutionId': 2,
        'launchTime': '2020-08-02 12:20:10',
    },
    {
        'stageExecutionId': 3,
        'launchTime': '2020-08-03 12:20:10',
    },
])


const params = ref([
    {
        'key': 'a',
        'value': 'b',
    }
])

const paramsJson = ref({
    'key1': 'aa',
    'list1': [1, 2, 3],
    'deepObj': {
        'key2': '1',
        'list2': [3, 4, 5]
    }
})

async function getStageExecutions(stageStartupId) {
    return
}


const items = ['Foo', 'Bar', 'Fizz', 'Buzz']

const stageExecutions = ref([])
const stageParams = ref(null)
const selectedStageExecutionId = ref(null)
const selectedStageExecution = computed(()=>{
    if(selectedStageExecutionId.value==null || stageExecutions.value==null || stageExecutions.value.length==0){
        return null
    }
    for(let stageExecution of stageExecutions.value){
        if(stageExecution.id == selectedStageExecutionId.value){
            return stageExecution
        }
    }
    return null
})
watch(() => props.stageStartup, async () => {
    selectedStageExecutionId.value = null
    if (props.stageStartup == null) {
        return
    }
    stageExecutions.value = await request.get(API_PATHS.TASK_INFO.STAGE_EXECUTIONS, {
        params: {
            stageStartupId: props.stageStartup.id
        }
    })
        .then(respBody => respBody.data)
        .catch(e => {
            console.debug(e)
            return null
        })

    stageParams.value = await request.get(API_PATHS.TASK_INFO.STAGE_STARTUP_PARAMS, {
        params: {
            stageStartupIds: [props.stageStartup.id]
        }
    })
        .then(respBody => {
            if (respBody.data!=null && respBody.data.length == 0) {
                return null
            } else {
                const param = respBody.data[0]
                return {
                    "input":param.encodedInput.length==0?'':JSON.parse(param.encodedInput),
                    "sharedContextAtStartup":param.encodedSharedContextAtStartup.length==0?'':JSON.parse(param.encodedSharedContextAtStartup),
                    "sharedContextAtCompletion":param.encodedSharedContextAtCompletion.length==0?'':JSON.parse(param.encodedSharedContextAtCompletion),
                }
            }
        })
        .catch(e => {
            console.debug(e)
            return null
        })

})


// function selectStageExecution(stageExecution){
//     console.log(stageExecution)
//     currentStageExecution.value = stageExecution
// }

</script>

<template>

    <v-navigation-drawer v-model="props.draw" :location="'right'" :width="navWidth" floating="">
        <v-toolbar density="compact">

            <v-btn :icon="mdiWindowClose" @click="$emit('update:draw')"></v-btn>
            <!-- <v-toolbar-title text="阶段信息"></v-toolbar-title> -->
            <!-- <v-select :items="items" density="compact" label="Compact" :hide-details="true" :tile="true"
                :style="{ 'border-style': 'none' }">
            </v-select> -->

        </v-toolbar>

        <v-card class="border-thin">
            <v-card-title>阶段启动信息</v-card-title>

            <v-card-text>
                <v-table density="compact" striped="even" fixed-header class="border-thin text-break">
                    <!-- 列宽定义区域 -->
                    <colgroup>
                        <col style="width: 25%;"> <!-- 第 1 列宽度 -->
                        <col style="width: 75%;"> <!-- 第 2 列宽度 -->
                    </colgroup>
                    <tbody>
                        <tr>
                            <td>ID</td>
                            <td> {{ props.stageStartup?.id }}</td>
                        </tr>
                        <tr>
                            <td>完成时间</td>
                            <td>{{ props.stageStartup?.completionTime }}</td>
                        </tr>
                        <tr>
                            <td>失败次数</td>
                            <td>{{ props.stageStartup?.failCount }}</td>
                        </tr>
                    </tbody>
                </v-table>

            </v-card-text>

        </v-card>


        <v-card class=" border-thin">
            <v-card-title>全局参数快照</v-card-title>
            <v-card-text>
                <json-viewer v-if="stageParams" :value="stageParams?.sharedContextAtStartup" :expand-depth=5 copyable boxed sort></json-viewer>
            </v-card-text>
        </v-card>


        <v-card class=" border-thin">
            <v-card-title>当前阶段启动参数</v-card-title>
            <v-card-text>
                <json-viewer v-if="stageParams" :value="stageParams?.input" :expand-depth=5 copyable boxed sort></json-viewer>
            </v-card-text>
        </v-card>

        <v-divider></v-divider>

        <v-card class="border-thin">


            <v-card-title>
                阶段执行信息
            </v-card-title>

            <!-- hide-details会隐藏掉一个小尾巴 -->
            <v-select :items="stageExecutions" item-title="id" label="阶段执行实例" :hide-details="true" v-model="selectedStageExecutionId">
                <template v-slot:item="{ props, item }">
                    <v-list-item v-bind="props" :subtitle="item.raw.startTime"
                        ></v-list-item>
                </template>
            </v-select>
            <!-- @click="()=>selectStageExecution(item.raw)" -->


            <v-card class="border-thin" :style="{ 'height': '100%' }">
                <v-card-title>日志</v-card-title>
                <v-card-text>


                    <StageExecutionLogTable :stageExecution="selectedStageExecution"></StageExecutionLogTable>
                </v-card-text>
            </v-card>
        </v-card>

    </v-navigation-drawer>

</template>