<script setup>

import { ref } from 'vue'

import { mdiAccountCowboyHat, mdiDotsVertical } from '@mdi/js'
import { namedRoutes } from '@/router'
import { mdiCheckCircle } from '@mdi/js'

const headers = ref([
    { title: 'Task Name', key: 'taskName', align: 'start', headerProps: { class: 'font-weight-bold' } },
    { title: 'Task Version', key: 'taskVersion', align:'center',headerProps: { class: 'font-weight-bold' } },
    { title: 'Progress', key: 'progress', align:'center',headerProps: { class: 'font-weight-bold' } },
    { title: 'Startup Time', key: 'startupTime',align: 'end', headerProps: { class: 'font-weight-bold' } },
    // { title: 'Actions', key: 'actions', align: 'end', headerProps: { class: 'font-weight-bold' } },
])


const page = ref(1)
const pageSize = ref(4)
const totalCount = ref(4)

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

function getProgressColor(status){
    if(status=='running'){
        return 'primary'
    }
    return 'red'
}

</script>

<template>

    <VDataTableServer :headers="headers" :page="page" :items-per-page="pageSize" :items-length="totalCount"
        item-value="id" :items="items" :disable-sort="true" hover>


        <template v-slot:item="{ item }">
            <tr class="text-no-wrap cursor-pointer"
                @click="$router.push({ name: namedRoutes.taskStartupDetail.name, params: { 'startupId': item.id } })">
                <td>{{ item.taskName }}</td>
                <td class="text-center">{{ item.taskVersion }}</td>
                <td class=" d-flex justify-center align-center">
                    <v-icon v-if="item.status=='success'" :icon="mdiCheckCircle" :style="{color:'green'}" :size="35"/>
                    <v-progress-circular v-else :model-value="item.progress" :color="getProgressColor(item.status)" class="text-body-2">
                        <template v-slot:default> {{ item.progress }} </template>
                    </v-progress-circular>
                </td>
                <td class="text-end">{{ item.startupTime }}</td>
                <!-- <td class="text-end">
                    <v-btn variant="plain" :icon="mdiDotsVertical" @click=""></v-btn>
                </td> -->
            </tr>

        </template>
    </VDataTableServer>
</template>