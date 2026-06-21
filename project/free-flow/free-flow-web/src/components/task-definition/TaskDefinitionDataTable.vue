<script setup>

import { ref } from 'vue'

import { mdiAccountCowboyHat, mdiDotsVertical } from '@mdi/js'
import { namedRoutes } from '@/router'
import request from '@/utils/request'
import { API_PATHS } from '@/utils/api/paths'

const headers = ref([
    { title: 'Task Name', key: 'taskName', align: 'start', headerProps: { class: 'font-weight-bold' } },
    { title: 'Task Version', key: 'taskVersion', align: 'end', headerProps: { class: 'font-weight-bold' } },
    { title: 'Release Time', key: 'releaseTime', align: 'end', headerProps: { class: 'font-weight-bold' } },
    // { title: 'Actions', key: 'actions', align: 'end', headerProps: { class: 'font-weight-bold' } },
])


const page = ref(1)
const pageSize = ref(4)
const totalCount = ref(4)
const loading = ref(true)

const items = ref([
    {
        id: 1,
        name: '小猪跑',
        version: '1',
        releaseTime: '2021-07-01',
    },
    {
        id: 2,
        name: '小猪跑',
        version: '2',
        releaseTime: '2021-07-01',
    },
    {
        id: 3,
        taskName: '小猪跑',
        version: '3',
        releaseTime: '2021-07-01',
    }, {
        id: 4,
        name: '小猪跑',
        version: '4',
        releaseTime: '2021-07-01',
    }
])

async function loadItems() {
    loading.value = true
    await request.get(API_PATHS.TASK_DEFINITION.DETAILS_QUERY, {
        params: {
            page: page.value,
            pageSize: pageSize.value,
        }
    })
        .then(respBody => {
            const respData = respBody.data
            totalCount.value = respData.total
            items.value = respData.tasks.map(t => {
                return {
                    id: t.id,
                    name: t.name,
                    version: t.version,
                    releaseTime: t.releaseTime
                }
            })
        })
        .catch(e => {
            console.error("请求任务列表发生异常", e)
        })
    loading.value = false
}

const pageSizeOptions = [
  {value: 10, title: '10'},
  {value: 25, title: '25'},
  {value: 50, title: '50'},
  {value: 100, title: '100'},
//   {value: -1, title: '$vuetify.dataFooter.itemsPerPageAll'}
]

loadItems()

</script>

<template>

    <VDataTableServer :headers="headers" v-model:page="page" v-model:items-per-page="pageSize" :items-length="totalCount" :loading="loading" @update:options="loadItems" :items-per-page-options="pageSizeOptions"
        item-value="id" :items="items" :disable-sort="true" hover>


        <template v-slot:item="{ item }">
            <tr class="text-no-wrap cursor-pointer" :id="item.id"
                @click="$router.push({ name: namedRoutes.taskDefinitionDetail.name, params: { 'taskId': item.id } })">
                <td>{{ item.name }}</td>
                <td class="text-end">{{ item.version }}</td>
                <td class="text-end">{{ item.releaseTime }}</td>
                <!-- <td class="text-end">
                    <v-btn variant="plain" :icon="mdiDotsVertical" @click=""></v-btn>
                </td> -->
            </tr>

        </template>
    </VDataTableServer>
</template>