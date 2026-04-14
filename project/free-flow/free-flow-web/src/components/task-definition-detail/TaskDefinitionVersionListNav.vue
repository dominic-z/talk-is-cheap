<script setup>
import { mdiMagnify } from '@mdi/js';
import { ref } from 'vue'

const items = ref([
  {
    taskVersion: "版本1",
    releaseTime: "2025年1月1日",
    sharedContextClass: "java.utils.Map"
  },
  {
    taskVersion: "版本2",
    releaseTime: "2026年1月1日",
    sharedContextClass: "java.utils.Map"
  },
])



</script>
<template>

  <v-navigation-drawer>
    <div class="d-flex mt-2 ml-2 mr-2 mb-0">
      <v-text-field label="taskVersion"></v-text-field>
      <v-btn :icon="mdiMagnify" variant="plain"></v-btn>
    </div>

    <v-divider></v-divider>
    <v-list lines="one">
      <v-list-item class="pa-1" v-for="item in items">
        <v-card class="ma-1 cursor-auto" hover>
          <v-card-title>{{ item.taskVersion }}</v-card-title>
          <v-card-subtitle>{{ item.releaseTime }}</v-card-subtitle>
          <v-expansion-panels class="mt-3">
            <v-expansion-panel :key=item.taskVersion title="全局参数定义">
              <v-expansion-panel-text>
                {{ item.sharedContextClass }}
              </v-expansion-panel-text>
            </v-expansion-panel>
          </v-expansion-panels>
        </v-card>

      </v-list-item>
    </v-list>
  </v-navigation-drawer>
</template>