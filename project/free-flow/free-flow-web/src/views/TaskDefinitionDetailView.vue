<script setup>
import TaskDefinitionDetailGraph from '@/components/task-definition-detail/TaskDefinitionDetailGraph.vue';
import { mdiMagnify } from '@mdi/js';
import { mdiArrowLeft } from '@mdi/js';

//

const props = defineProps(['taskId'])
</script>

<template>
  <v-app id="taskDefinitionDetail">

    <v-app-bar color="grey-lighten-4 border-thin" height="72" flat>
      <v-btn :icon="mdiArrowLeft" class=" mr-1"></v-btn>
      <div class="text-h6">taskDefinitionName</div>

    </v-app-bar>


    <v-navigation-drawer>
      <div class="d-flex mt-2 ml-2 mr-2 mb-0">
        <v-text-field label="taskVersion"></v-text-field>
        <v-btn :icon="mdiMagnify" variant="plain"></v-btn>
      </div>

      <v-divider></v-divider>
      <v-list-item class="pa-1">
        <v-card class="ma-1" hover>
          <v-card-title>版本</v-card-title>
          <v-card-subtitle>发布日期</v-card-subtitle>
        </v-card>

      </v-list-item>


      <v-list-item class="pa-1">
        <v-card class="ma-1" hover>
          <v-card-title>版本</v-card-title>
          <v-card-subtitle>发布日期</v-card-subtitle>
        </v-card>

      </v-list-item>

    </v-navigation-drawer>

    <TaskDefinitionDetailGraph></TaskDefinitionDetailGraph>
  </v-app>
</template>
