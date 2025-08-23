<script setup>
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
        await request.get(API_PATHS.cluster.schduler.id)
            .then(respData => {
                const res = respData.data
                console.log(res)
                serverItems.value = [{
                    nodeID: res,
                    nodeType: props.nodeType,
                    launchTime: "2020-08-08"
                }]
            })
        itemLength.value = 10
        loading.value = false;
    }, 500)

}

watch(computed(() => props.nodeType), loadItems, { immediate: true })

function myAlert() {
    alert("aaaa")
}



</script>

<template>


    <v-main style="height: 100vh;">

        <slot name="search"></slot>

        <v-data-table-server v-model:items-per-page="itemsPerPage" v-model:page="page" :headers="headers"
            :items="serverItems" :items-length="itemLength" :loading="loading" @update:page="loadItems"
            :show-current-page="true" :disable-sort="true" hover>


            <template v-slot:item="{ item }">
                <tr class="text-no-wrap">
                    <td><v-icon :icon="`${mdiAccountCowboyHat}`" />{{ item.nodeID }}</td>
                    <td class="text-end">{{ item.nodeType }}</td>
                    <td class="text-end">{{ item.launchTime }}</td>
                    <td class="text-end">
                        <v-btn variant="plain" :icon="mdiDotsVertical" @click="myAlert"></v-btn>
                    </td>
                </tr>

            </template>
        </v-data-table-server>
    </v-main>
</template>
