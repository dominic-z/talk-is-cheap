<script setup>
import TaskStartupDetailGraph from '@/components/task-startup/TaskStartupDetailGraph.vue';
import { mdiFolder } from '@mdi/js';
import { mdiDotsVertical } from '@mdi/js';
import { ref } from 'vue';

const props = defineProps(['startupId'])

const retryCount = ref([3])
const showTaskStartupDetail = ref(false)
const rail = ref(true)

</script>

<template>

    <v-app>
        <v-app-bar color="primary" density="compact">
            <template v-slot:prepend>
                <v-app-bar-nav-icon></v-app-bar-nav-icon>
            </template>

            <v-app-bar-title>Photos</v-app-bar-title>

            <template v-slot:append>
                <v-btn :icon="mdiDotsVertical"></v-btn>
            </template>
        </v-app-bar>


        <v-navigation-drawer expand-on-hover permanent rail @update:rail="(e) => rail = e">
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



                    <v-list-item v-if="showTaskStartupDetail" key="2-1" title="1212"
                        @mouseleave="showTaskStartupDetail = !showTaskStartupDetail" link>
                        <template v-slot:prepend>
                            <v-progress-circular color="primary" :model-value="20" :size="25"
                                class="mr-5"></v-progress-circular>
                        </template>

                        

                        <template v-slot:default>
                            <!-- <v-progress-circular color="primary" :model-value="20" :size="25"
                                class="mr-5"></v-progress-circular> -->
                            <span>fsdfs</span>
                            <div :style="{ 'grid-column': '1 / -1' }">
                                fsd

                                <v-card >
                                    <div>123213</div>
                                    <div>123213</div>
                                    <div>123213</div>
                                    <div>123213</div>
                                </v-card>
                            </div>

                        </template>

                    </v-list-item>
                    <v-list-item key="2-2" v-else @mouseenter="showTaskStartupDetail = !showTaskStartupDetail"
                        title="123">
                        <!-- <template v-slot:prepend>
                            <v-progress-circular color="primary" :model-value="20" :size="25"
                                class="mr-5"></v-progress-circular>
                        </template> -->

                        <v-progress-circular color="primary" :model-value="20" :size="25"
                                class="mr-5"></v-progress-circular>
                                <div class="overflow-hidden">fsdfs</div>
                    </v-list-item>




                    <v-list-item :prepend-icon="mdiFolder" title="Starred" value="starred" :key="5"></v-list-item>
                </v-slide-y-transition>

            </v-list>
        </v-navigation-drawer>

        <TaskStartupDetailGraph></TaskStartupDetailGraph>


    </v-app>

</template>
