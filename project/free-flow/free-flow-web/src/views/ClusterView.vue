<script setup>
import { API_PATHS } from '@/utils/api/paths';
import request from '@/utils/request';
import { mdiAccountCowboyHat } from '@mdi/js';
import { ref } from 'vue';
import MyIcon from './MyIcon.vue';

const path = mdiAccountCowboyHat

console.log(mdiAccountCowboyHat)
const itemsPerPage = ref(2)
const headers = [
    { title: 'Node ID', key: 'nodeID', align: 'start' },
    { title: 'Node Type', key: 'nodeType', align: 'end' },
    { title: 'Launch Time', key: 'launchTime', align: 'end' },
    { title: 'Action', key: 'action', align: 'end' },
]
const loading = ref(true)
const serverItems = ref([])
const itemLength = ref(0)
const page = ref(1)

async function loadItems() {
    await request.get(API_PATHS.cluster.schduler.id)
        .then(respData => {
            const res = respData.data
            console.log(res)
            serverItems.value = [{
                nodeID: res,
                nodeType: "Scheduler",
                launchTime: "2020-08-08"
            }]
        })
    loading.value = false;
}

function myAlert() {
    alert("aaaa")
}

</script>

<template>
<MyIcon></MyIcon>
<!-- <v-icon icon="mdi-account-cowboy-hat" /> -->
    <v-app>
        <v-main style="height: 100vh;">
            <v-data-table-server v-model:items-per-page="itemsPerPage" :page="page" :headers="headers"
                :items="serverItems" :items-length="itemLength" :loading="loading" item-value="name"
                @update:options="loadItems" :show-current-page="true" :disable-sort="true" hover>
                <template v-slot:item="{ item }">
                    <tr class="text-no-wrap">
                        <td><v-icon :icon="`${mdiAccountCowboyHat}`" />{{ item.nodeID }}</td>
                        <td class="text-end">{{ item.nodeType }}</td>
                        <td class="text-end">{{ item.launchTime }}</td>
                        <td class="text-end">
                            <v-btn variant="plain" icon="mdi-dots-vertical"  @click="myAlert"></v-btn>
                        </td>
                    </tr>

                </template>
            </v-data-table-server>
        </v-main>
    </v-app>
</template>