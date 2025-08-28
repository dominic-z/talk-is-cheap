<script setup>
// 页面左侧的execution导航栏

import { mdiPinOutline } from '@mdi/js';
import { mdiFolder } from '@mdi/js';
import { mdiDotsVertical } from '@mdi/js';
import { ref } from 'vue';


const showTaskStartupDetail = ref(false)
const rail = ref(true)
const expandOnHover = ref(true)
const expand = ref(false)
const cardRef = ref(null)


function updateRail(e) {
    if (e) {
        // 收起nav
        showTaskStartupDetail.value = false
        expand.value = false
    } else {
        expand.value = true
    }
}

function updateShowTaskStartupDetail(e) {
    console.log(e)
    if (showTaskStartupDetail.value) {
        // 也可以通过模板引用获取一个组件的真实位置信息，并将其与鼠标位置进行对比
        // const rect = cardRef.value.getBoundingClientRect();
        console.log(e)
        console.log(cardRef.value.position)

        
        showTaskStartupDetail.value = false;
    } else {
        setTimeout(() => showTaskStartupDetail.value = true, 100)
    }
}

function pinNav() {
    rail.value = !rail.value
    expandOnHover.value = !expandOnHover.value
}
</script>

<template>

    <v-navigation-drawer :expand-on-hover="expandOnHover" permanent :rail="rail" @update:rail="updateRail">
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
                <v-list-item v-if="showTaskStartupDetail" key="2-1" @mouseleave="updateShowTaskStartupDetail" link>
                    <template v-slot:default>
                        <div class="v-list-item pa-0">
                            <v-progress-circular color="primary" :model-value="20" :size="25"
                                class="mr-5"></v-progress-circular>
                            <span class="overflow-hidden">fsdfs</span>
                        </div>

                        <v-card class="mt-2" ref="cardRef">
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


        <div v-if="expand" class="position-absolute pin-btn">
            <v-btn :icon="mdiPinOutline" @click="pinNav"></v-btn>
        </div>


    </v-navigation-drawer>
</template>



<style>
.pin-btn {
    /* 贴紧右边框 */
    right: 0;
    /* 垂直方向居中（top:50% 配合 translateY(-50%) 抵消自身高度的一半） */
    top: 50%;
    transform: translateY(-50%);
    /* 调整按钮与右边框的距离（可选） */
    transform: translate(50%, -50%);
    /* 若想让按钮一半在div外，可添加此句 */

}
</style>