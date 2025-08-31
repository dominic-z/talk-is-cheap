<script setup>
import TaskExecutionListNav from '@/components/task-startup/TaskExecutionListNav.vue';
import TaskStartupDetailGraph from '@/components/task-startup/TaskStartupDetailGraph.vue';
import Loader from '@/components/utils/Loader.vue';
import { mdiArrowLeft } from '@mdi/js';
import { mdiPinOutline } from '@mdi/js';
import { mdiFolder } from '@mdi/js';
import { mdiDotsVertical } from '@mdi/js';
import { ref } from 'vue';

const props = defineProps(['startupId'])

const retryCount = ref([3])

const loading = ref(true)
setTimeout(()=>loading.value = false,1000)

</script>

<template>

    <v-app>
        <Loader :overlay="loading"></Loader>

        <v-app-bar color="primary" density="compact" flat class="border-thin">

            <template v-slot:prepend>
                <v-btn :icon="mdiArrowLeft" @click="$router.go(-1)"></v-btn>
            </template>

            <v-app-bar-title>任务启动名称</v-app-bar-title>

        </v-app-bar>

        <TaskExecutionListNav></TaskExecutionListNav>


        <TaskStartupDetailGraph></TaskStartupDetailGraph>


    </v-app>

</template>
