<script setup>
import { mdiMagnify } from '@mdi/js';


</script>
<template>

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
          <v-expansion-panels>
            <v-expansion-panel :key="1" title="全局参数定义">
              <v-expansion-panel-text>
                java.utils.Map
              </v-expansion-panel-text>
            </v-expansion-panel>
          </v-expansion-panels>
        </v-card>

      </v-list-item>


      <v-list-item class="pa-1">
        <v-card class="ma-1" hover>
          <v-card-title>版本</v-card-title>
          <v-card-subtitle>发布日期</v-card-subtitle>
          <v-expansion-panels>
            <v-expansion-panel :key="1" title="全局参数定义">
              <v-expansion-panel-text>
                java.utils.Map
              </v-expansion-panel-text>
            </v-expansion-panel>
          </v-expansion-panels>
        </v-card>

      </v-list-item>



    </v-navigation-drawer>
</template>