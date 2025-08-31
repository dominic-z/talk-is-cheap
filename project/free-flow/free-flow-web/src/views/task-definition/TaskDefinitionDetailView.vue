<script setup>
import TaskDefinitionDetailGraph from '@/components/task-definition-detail/TaskDefinitionDetailGraph.vue';
import TaskDefinitionVersionListNav from '@/components/task-definition-detail/TaskDefinitionVersionListNav.vue';
import { mdiMagnify } from '@mdi/js';
import { mdiArrowLeft } from '@mdi/js';

//

const props = defineProps(['taskId'])
</script>

<template>
  <v-app id="taskDefinitionDetail">

    <v-app-bar color="grey-lighten-4" class="border-thin" flat density="compact">
      <v-btn :icon="mdiArrowLeft" class=" mr-1" @click="$router.go(-1)"></v-btn>
      <div class="text-h6">taskDefinitionName</div>
    </v-app-bar>


    <TaskDefinitionVersionListNav></TaskDefinitionVersionListNav>

    <TaskDefinitionDetailGraph></TaskDefinitionDetailGraph>
  </v-app>
</template>
