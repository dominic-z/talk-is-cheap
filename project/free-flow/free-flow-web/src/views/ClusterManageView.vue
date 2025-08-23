<script setup>

// import HomeNav from '@/views/HomeNav.vue';
import GlobalNav from '@/components/nav/GlobalNav.vue';

import ClusterManageMainView from './ClusterManageMainView.vue';

import { mdiMagnify,mdiArrowCollapseVertical } from '@mdi/js'
import { ref } from 'vue'

const expand = ref(false)
const nodeType = ref('Scheduler')
function updateTab(e) {
  // console.log(e)
  // console.log(nodeType.value)
}

</script>

<template>
  <v-app id="inspire">
    <GlobalNav>
      <v-btn :icon="mdiMagnify" @click="expand = !expand">
      </v-btn>
    </GlobalNav>

    <ClusterManageMainView :nodeType="nodeType">

      <template v-slot:search>
        <v-expand-transition>
          <v-card v-if="expand">
            <v-toolbar class="text-black">
              <v-tabs v-model="nodeType" @update:modelValue="updateTab">
                <v-tab value="Scheduler">Scheduler</v-tab>
                <v-tab value="Worker">Worker</v-tab>
              </v-tabs>

              <v-btn :icon="mdiArrowCollapseVertical" class="position-absolute right-0" @click="expand = !expand"></v-btn>

            </v-toolbar>
          </v-card>

        </v-expand-transition>
      </template>


    </ClusterManageMainView>
  </v-app>
</template>
