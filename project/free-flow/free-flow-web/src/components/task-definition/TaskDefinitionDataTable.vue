<script setup>

import { ref } from 'vue'

import {mdiAccountCowboyHat,mdiDotsVertical} from '@mdi/js'

const headers = ref([
    { title: 'Task Name', key: 'taskName', align: 'start', headerProps: { class: 'font-weight-bold' } },
    { title: 'Task Version', key: 'taskVersion', align: 'end', headerProps: { class: 'font-weight-bold' } },
    { title: 'Release Time', key: 'releaseTime', align: 'end', headerProps: { class: 'font-weight-bold' } },
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
        releaseTime: '2021-07-01',
    },
    {
        id: 2,
        taskName: '小猪跑',
        taskVersion: '2',
        releaseTime: '2021-07-01',
    },
    {
        id: 3,
        taskName: '小猪跑',
        taskVersion: '3',
        releaseTime: '2021-07-01',
    }, {
        id: 4,
        taskName: '小猪跑',
        taskVersion: '4',
        releaseTime: '2021-07-01',
    }
])

</script>

<template>

    <VDataTableServer :headers="headers" :page="page" :items-per-page="pageSize" :items-length="totalCount"
        item-value="id" :items="items" :disable-sort="true" hover>


        <template v-slot:item="{ item }">
            <tr class="text-no-wrap cursor-pointer">
                <td><v-icon :icon="`${mdiAccountCowboyHat}`" />{{ item.taskName }}</td>
                <td class="text-end">{{ item.taskVersion }}</td>
                <td class="text-end">{{ item.releaseTime }}</td>
                <!-- <td class="text-end">
                    <v-btn variant="plain" :icon="mdiDotsVertical" @click=""></v-btn>
                </td> -->
            </tr>

        </template>
    </VDataTableServer>
</template>