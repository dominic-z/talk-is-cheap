<script setup>
import TaskStartupDetailGraph from '@/components/task-startup/TaskStartupDetailGraph.vue';
import { mdiArrowLeft } from '@mdi/js';
import { mdiFolder } from '@mdi/js';
import { mdiDotsVertical } from '@mdi/js';
import { ref } from 'vue';

const props = defineProps(['startupId'])

const retryCount = ref([3])
const showTaskStartupDetail = ref(false)
const rail = ref(true)

function updateRail(e) {
    if (e) {
        // 收起nav
        showTaskStartupDetail.value = false
    }
}

function updateShowTaskStartupDetail(e) {
    console.log(e)
    if (showTaskStartupDetail.value) {
        showTaskStartupDetail.value = false;
    } else {
        setTimeout(() => showTaskStartupDetail.value = true, 100)
    }
}

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


        <v-navigation-drawer permanent @update:rail="updateRail">
        <!-- <v-navigation-drawer expand-on-hover permanent rail @update:rail="updateRail"> -->
            <v-divider></v-divider>


            <v-list density="compact" nav>
                <v-slide-y-transition :leave-absolute="true" :group="true" :hide-on-leave="true">

                    <v-list-item :key="1" title="My Files" value="myfiles" link>
                        <template v-slot:prepend>
                            <v-progress-circular color="primary" :model-value="20" :size="25"
                                class="mr-5"></v-progress-circular>
                        </template>
                        <template>
                            sfdafd
                        </template>

                    </v-list-item>



                    <!-- 见vuetify-guide -->
                    <v-list-item v-if="showTaskStartupDetail" key="2-1"  @mouseleave="updateShowTaskStartupDetail" link>
                        <template v-slot:default>
                            <div class="v-list-item pa-0">
                                <v-progress-circular color="primary" :model-value="20" :size="25"
                                    class="mr-5"></v-progress-circular>
                                <span class="overflow-hidden">fsdfs</span>
                            </div>

                            <v-card class="mt-2">
                                <div>123213</div>
                                <div>123213</div>
                                <div>123213</div>
                                <div>123213</div>
                            </v-card>
                        </template>

                    </v-list-item>
                    <v-list-item key="2-2" v-else @mouseenter="updateShowTaskStartupDetail">
                        <div class="v-list-item pa-0">
                            <v-progress-circular color="primary" :model-value="20" :size="25"
                                class="mr-5"></v-progress-circular>
                            <span class="overflow-hidden">fsdfs</span>
                        </div>

                    </v-list-item>




                    <v-list-item :prepend-icon="mdiFolder" title="Starred" value="starred" :key="5"></v-list-item>
                </v-slide-y-transition>

            </v-list>
        </v-navigation-drawer>

        <TaskStartupDetailGraph></TaskStartupDetailGraph>


    </v-app>

</template>
