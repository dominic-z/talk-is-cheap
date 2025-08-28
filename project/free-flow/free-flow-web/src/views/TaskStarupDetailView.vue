<script setup>
import TaskExecutionListNav from '@/components/task-startup/TaskExecutionListNav.vue';
import TaskStartupDetailGraph from '@/components/task-startup/TaskStartupDetailGraph.vue';
import { mdiArrowLeft } from '@mdi/js';
import { mdiPinOutline } from '@mdi/js';
import { mdiFolder } from '@mdi/js';
import { mdiDotsVertical } from '@mdi/js';
import { ref } from 'vue';

const props = defineProps(['startupId'])

const retryCount = ref([3])



</script>

<template>

    <v-app>
        <v-app-bar color="primary" density="compact">
            <template v-slot:prepend>
                <v-btn :icon="mdiArrowLeft"></v-btn>
            </template>

            <v-app-bar-title>Photos</v-app-bar-title>

            <template v-slot:append>
                <v-btn :icon="mdiDotsVertical"></v-btn>
            </template>
        </v-app-bar>

        <TaskExecutionListNav></TaskExecutionListNav>
 

        <TaskStartupDetailGraph></TaskStartupDetailGraph>


    </v-app>

</template>

