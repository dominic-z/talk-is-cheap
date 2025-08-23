<script setup>

// import HomeNav from '@/views/HomeNav.vue';

import ClusterManageMainView from '../components/cluster-manage/ClusterManageMain.vue';

import { useClusterManageStore } from '@/stores/clusterManageStore';
import { mdiArrowCollapseVertical } from '@mdi/js';
import { ref } from 'vue';


const clusterManageStore = useClusterManageStore()

const nodeType = ref('Scheduler')
function updateTab(e) {
  console.log(nodeType.value)
}


</script>

<template>

    <ClusterManageMainView :nodeType="nodeType">

      <!-- 其实好像没必要用slot的，但是懒得改了 -->
      <template v-slot:search>
        <v-expand-transition>
          <v-card v-if="clusterManageStore.expand">
            <v-toolbar class="text-black">
              <v-tabs v-model="nodeType" @update:modelValue="updateTab">
                <v-tab value="Scheduler">Scheduler</v-tab>
                <v-tab value="Worker">Worker</v-tab>
              </v-tabs>

              <v-btn :icon="mdiArrowCollapseVertical" class="position-absolute right-0" @click="clusterManageStore.changeExpand"></v-btn>

            </v-toolbar>
          </v-card>

        </v-expand-transition>
      </template>


    </ClusterManageMainView>
</template>
