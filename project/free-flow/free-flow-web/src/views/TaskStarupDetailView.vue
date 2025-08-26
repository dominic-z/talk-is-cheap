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



                    <!-- 这是个啥玩意？解释一下，我的诉求是希望能够 -->
                    <!-- 1. 在nav的hover之前展示icon，之后展开nav，展开后额外展示title -->
                    <!-- 2. hover某一个item的时候，在这个item中展开一个card，显示这个item的详细信息 -->
                    <!-- 参考文档中的原始代码，https://vuetifyjs.com/zh-Hans/components/navigation-drawers/#section-60ac505c65f662695c55 -->
                    <!-- 使用的是v-list-item的title属性实现标题， prepend-icon实现hover之前展示icon-->
                    <!-- 但是，在v-list-item中首先会渲染一个display为grid的块作为根，其类为v-list-item，下面有prepend和v-list-item__content，这两个块会并排存在 -->
                    <!-- 那么，如果我简单的在v-list-item中添加一个card话，这个card会被放在v-list-item__content -->
                    <!-- 最终样式就会变成 -->
                    <!--      title -->
                    <!-- icon card  -->
                    <!--      card  -->

                    <!-- 但我希望的样式是，也就是说我希望card能另起一行，不要和title挤在同一列里 -->
                    <!-- icon title  -->
                    <!-- c  a  r  d  -->
                    <!-- c  a  r  d  -->
                    <!-- 于是决定重写v-list-item的插槽自己实现，然后将默认插槽的div与v-card分开，div存储icon和title -->
                    <!-- 但是者带来了一个新问题，hover nav之前，title会被展示在nav中而不会隐藏掉，为了解决这个问题，直接在div中加上了v-list-item这个类，让div具备grid属性，从而固定div中的位置，反复尝试的结果，因为我也不太记得grid定位的用法了，所以直接抄vuetify内置的class -->
                    <v-list-item v-if="showTaskStartupDetail" key="2-1"
                        @mouseleave="showTaskStartupDetail = !showTaskStartupDetail" link>
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
                    <v-list-item key="2-2" v-else @mouseenter="showTaskStartupDetail = !showTaskStartupDetail">
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
