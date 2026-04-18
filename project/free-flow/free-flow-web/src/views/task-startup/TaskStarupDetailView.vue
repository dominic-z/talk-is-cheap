<script setup>
import TaskExecutionListNav from '@/components/task-startup/TaskExecutionListNav.vue';
import TaskStartupDetailGraph from '@/components/task-startup/TaskStartupDetailGraph.vue';
import Loader from '@/components/utils/Loader.vue';
import { API_PATHS } from '@/utils/api/paths';
import request from '@/utils/request';
import { mdiArrowLeft } from '@mdi/js';
import { mdiPinOutline } from '@mdi/js';
import { mdiFolder } from '@mdi/js';
import { mdiDotsVertical } from '@mdi/js';
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useRoute } from 'vue-router';

const props = defineProps(['taskStartupId'])
const route = useRoute()
const router = useRouter()

let data = router?.options?.history?.state?.data
const taskName = data.taskName
const taskVersion = data.taskVersion


const retryCount = ref([3])

const loading = ref(true)
setTimeout(() => loading.value = false, 1000)


// 如果列表数据是通过异步请求（如 API 调用）获取的，那么在组件初始渲染时，数据可能还是空的（例如 undefined 或空数组 []）。当数据返回并赋值后，如果子组件没有正确处理，就可能出现无法遍历的情况。
const taskExecutionInfoList = ref([])
const currentTaskExecution = ref(null)
function getTaskExecutions(taskStartupId) {
    request.get(API_PATHS.TASK_INFO.TASK_EXECUTIONS, {
        params: {
            taskStartupId: taskStartupId
        }
    }).then(respBody => {
        taskExecutionInfoList.value = respBody.data
        if (taskExecutionInfoList.value && taskExecutionInfoList.value.length) {
            currentTaskExecution.value = taskExecutionInfoList.value[0]
        }
    })
}

onMounted(() => {
    getTaskExecutions(props.taskStartupId)
})


</script>

<template>

    <v-app>
        <Loader :overlay="loading"></Loader>

        <v-app-bar color="primary" density="compact" flat class="border-thin">

            <template v-slot:prepend>
                <v-btn :icon="mdiArrowLeft" @click="$router.go(-1)"></v-btn>
            </template>

            <v-app-bar-title>任务启动名称</v-app-bar-title>

        </v-app-bar>

        <TaskExecutionListNav :taskStartupId="props.taskStartupId" :taskExecutionInfoList="taskExecutionInfoList">
        </TaskExecutionListNav>


        <TaskStartupDetailGraph :taskStartupId="props.taskStartupId" :taskName="taskName" :taskVersion="taskVersion"
            :taskExecution="currentTaskExecution"></TaskStartupDetailGraph>


    </v-app>

</template>
