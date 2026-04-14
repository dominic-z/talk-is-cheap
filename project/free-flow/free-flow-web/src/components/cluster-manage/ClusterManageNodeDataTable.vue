<script setup>
import { NodeType } from '@/enums/node';
import { API_PATHS } from '@/utils/api/paths';
import request from '@/utils/request';
import { mdiAccountCowboyHat, mdiDotsVertical } from '@mdi/js';
import { ref, watchEffect, watch, computed } from 'vue';


const props = defineProps(['nodeType'])

const headers = [
    { title: 'Node ID', key: 'nodeID', align: 'start', headerProps: { class: 'font-weight-bold' } },
    { title: 'Node Type', key: 'nodeType', align: 'end', headerProps: { class: 'font-weight-bold' } },
    { title: 'Launch Time', key: 'launchTime', align: 'end', headerProps: { class: 'font-weight-bold' } },
    { title: 'Action', key: 'action', align: 'end', headerProps: { class: 'font-weight-bold' } },
]
const loading = ref(true)
const serverItems = ref([])
const itemLength = ref(0)
const page = ref(1)
const itemsPerPage = ref(2)



async function loadItems() {

    loading.value = true
    serverItems.value = []
    // 模拟个读取
    setTimeout(async () => {
        await request.post(API_PATHS.cluster.nodes, {
            data: {
                nodeType: props.nodeType,
                pageSize: itemsPerPage.value,
                page: page.value
            }
        })
            .then(resp => {
                const respData = resp.data
                console.log(respData)
                itemLength.value = respData.total

                serverItems.value = respData.nodes.map(n => {
                    return {
                        nodeID: n.nodeAddress,
                        nodeType: n.nodeType,
                        launchTime: n.launchTime,
                        nodeStatus: n.nodeStatus,
                        isLeader: n.isLeader
                    }

                })
                console.log(serverItems.value)
                loading.value = false;
            })
            .catch(e => {
                console.error(e)
                loading.value = false
            })
    }, 500)

}

watch(() => props.nodeType, loadItems, { immediate: true })

function myAlert() {
    alert("aaaa")
}

const pageSizeOptions = [
  {value: 10, title: '10'},
  {value: 25, title: '25'},
  {value: 50, title: '50'},
  {value: 100, title: '100'},
//   {value: -1, title: '$vuetify.dataFooter.itemsPerPageAll'}
]


</script>

<template>
    <v-data-table-server v-model:items-per-page="itemsPerPage" v-model:page="page" :headers="headers"
        :items="serverItems" :items-length="itemLength" :loading="loading" @update:options="loadItems"
        :show-current-page="true" :disable-sort="true" hover item-value="nodeID"
        :items-per-page-options="pageSizeOptions"
        >
        <template v-slot:item="{ item }">
            <tr class="text-no-wrap">
                <td><v-icon v-if="item.isLeader" :icon="`${mdiAccountCowboyHat}`" />{{ item.nodeID }}</td>
                <td class="text-end">{{ item.nodeType==NodeType.WORKER?'Worker':'Scheduler' }}</td>
                <td class="text-end">{{ item.launchTime }}</td>
                <td class="text-end">
                    <v-btn variant="plain" :icon="mdiDotsVertical" @click="myAlert"></v-btn>
                </td>
            </tr>
        </template>
    </v-data-table-server>
</template>
