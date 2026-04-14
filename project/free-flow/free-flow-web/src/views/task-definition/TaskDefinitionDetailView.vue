<script setup>
import TaskDefinitionDetailGraph from '@/components/task-definition-detail/TaskDefinitionDetailGraph.vue';
import TaskDefinitionVersionListNav from '@/components/task-definition-detail/TaskDefinitionVersionListNav.vue';
import Loader from '@/components/utils/Loader.vue';
import { mdiMagnify } from '@mdi/js';
import { mdiArrowLeft } from '@mdi/js';
import {ref} from 'vue'
//

const props = defineProps(['taskId'])


const loading = ref(true)
setTimeout(()=>loading.value = false,1000)



</script>

<template>
  <v-app id="taskDefinitionDetail">
    <Loader :overlay="loading"></Loader>

    <v-app-bar color="grey-lighten-4" class="border-thin" flat density="compact">
      <v-btn :icon="mdiArrowLeft" class=" mr-1" @click="$router.go(-1)"></v-btn>
      <div class="text-h6">taskDefinitionName</div>
    </v-app-bar>


    <TaskDefinitionVersionListNav></TaskDefinitionVersionListNav>

    <TaskDefinitionDetailGraph></TaskDefinitionDetailGraph>
  </v-app>
</template>
