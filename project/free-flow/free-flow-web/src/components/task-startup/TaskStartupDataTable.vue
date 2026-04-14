<script setup>

import { ref,onMounted } from 'vue'

import { mdiAccountCowboyHat, mdiDotsVertical } from '@mdi/js'
import { namedRoutes } from '@/router'
import { mdiCheckCircle } from '@mdi/js'
import request from '@/utils/request'
import { API_PATHS } from '@/utils/api/paths'
import { TaskStageStatus } from '@/enums/task'
import { mdiClockTimeEightOutline } from '@mdi/js'
import { mdiMessageAlert } from '@mdi/js'
import { mdiAlertCircle } from '@mdi/js'

const headers = ref([
    { title: 'Task Name', key: 'taskName', align: 'start', headerProps: { class: 'font-weight-bold' } },
    { title: 'Task Version', key: 'taskVersion', align:'center',headerProps: { class: 'font-weight-bold' } },
    { title: 'Progress', key: 'progress', align:'center',headerProps: { class: 'font-weight-bold' } },
    { title: 'Startup Time', key: 'startupTime',align: 'end', headerProps: { class: 'font-weight-bold' } },
    // { title: 'Actions', key: 'actions', align: 'end', headerProps: { class: 'font-weight-bold' } },
])

const loading = ref(true)
const page = ref(1)
const pageSize = ref(100)
const totalCount = ref(0)

const items = ref([
    {
        id: 1,
        taskName: '小猪跑',
        taskVersion: '1',
        progress: 30,
        status: 'running',
        startupTime: '2021-07-01',
    },
    {
        id: 2,
        taskName: '小猪跑',
        taskVersion: '2',
        progress: 70,
        status: 'fail',
        startupTime: '2021-07-01',
    },
    {
        id: 3,
        taskName: '小猪跑',
        taskVersion: '3',
        progress: 100,
        status: 'success',
        startupTime: '2021-07-01',
    }, {
        id: 4,
        taskName: '小猪跑',
        taskVersion: '4',
        progress: 100,
        status: 'success',
        startupTime: '2021-07-01',
    }
])

async function getTaskStartupList(){
    loading.value = true
    await request.get(API_PATHS.TASK_INFO.TASK_STARTUPS,{
        params: {
            page: page.value,
            pageSize: pageSize.value,
        }
    }).then(respBody=>{
        const respData = respBody.data
        totalCount.value = respData.total
        items.value = respData.taskStartupInfoList

    }).catch(e=>{
        console.log(e)
    })
    loading.value = false
}

onMounted(getTaskStartupList)


</script>

<template>

    <VDataTableServer :headers="headers" v-model:page="page" v-model:items-per-page="pageSize" v-model:items-length="totalCount" :loading="loading" @update:options="getTaskStartupList"
        item-value="id" :items="items" :disable-sort="true" hover>


        <template v-slot:item="{ item }">
            <tr class="text-no-wrap cursor-pointer"
                @click="$router.push({ name: namedRoutes.taskStartupDetail.name, params: { 'taskStartupId': item.id }, state: {'data':{'taskName': item.taskName,'taskVerison':item.taskVersion}} })">
                <td>{{ item.taskName }}</td>
                <td class="text-center">{{ item.taskVersion }}</td>
                <td class=" d-flex justify-center align-center">
                    <v-progress-circular v-if="item.status==TaskStageStatus.RUNNING"  :model-value="item.progress" color="primary" class="text-body-2" :size="25">
                        <template v-slot:default> {{ item.progress }} </template>
                    </v-progress-circular>

                    <v-icon v-else-if="item.status==TaskStageStatus.FAILED || item.status==TaskStageStatus.FAILING" :icon="mdiAlertCircle" :style="{color:'red'}" :size="25"/>

                    <v-icon v-else-if="item.status==TaskStageStatus.TIME_OUT" :icon="mdiClockTimeEightOutline" :style="{color:'gray'}" :size="35"/>
                    <v-icon v-else :icon="mdiMessageAlert" :size="25"/>
                </td>
                <td class="text-end">{{ item.startupTime }}</td>
                <!-- <td class="text-end">
                    <v-btn variant="plain" :icon="mdiDotsVertical" @click=""></v-btn>
                </td> -->
            </tr>

        </template>
    </VDataTableServer>
</template>