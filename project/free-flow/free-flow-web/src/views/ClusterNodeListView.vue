<script setup>

// import HomeNav from '@/views/HomeNav.vue';

import ClusterManageNodeDataTable from '../components/cluster-manage/ClusterManageNodeDataTable.vue';

import { useClusterManageStore } from '@/stores/clusterManageStore';
import { mdiArrowCollapseVertical } from '@mdi/js';
import { ref } from 'vue';
import {NodeType} from '@/enums/node'


const clusterManageStore = useClusterManageStore()

const nodeType = ref(NodeType.WORKER)
function updateTab(e) {
  console.log(nodeType.value)
}


</script>

<template>

  <v-main style="height: 100vh;">

    <v-expand-transition>
      <v-card v-if="clusterManageStore.expand">
        <v-toolbar class="text-black">
          <v-tabs v-model="nodeType" @update:modelValue="updateTab">
            <v-tab :value="NodeType.SCHEDULER">Scheduler</v-tab>
            <v-tab :value="NodeType.WORKER">Worker</v-tab>
          </v-tabs>

          <v-btn :icon="mdiArrowCollapseVertical" class="position-absolute right-0"
            @click="clusterManageStore.changeExpand"></v-btn>

        </v-toolbar>
      </v-card>

    </v-expand-transition>

    <ClusterManageNodeDataTable :nodeType="nodeType">
    </ClusterManageNodeDataTable>

  </v-main>

</template>
