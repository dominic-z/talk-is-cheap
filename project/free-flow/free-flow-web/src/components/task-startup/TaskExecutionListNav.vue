<script setup>
// 页面左侧的execution导航栏

import { mdiPinOutline, mdiPinOffOutline } from '@mdi/js';
import { mdiCheck } from '@mdi/js';
import { mdiFolder } from '@mdi/js';
import { mdiDotsVertical } from '@mdi/js';
import { ref } from 'vue';

// const a = mdiPinOutline

const navProps = ref({
    expandOnHover: true,
    isNavExpanded: false, // nav是否有展开
    rail: true, // 是否开启滑轨，true就是开启滑轨，如果为false就是关闭滑轨，会一直展开
    pinIcon: mdiPinOutline,
})

const taskExecutionProps = ref({
    inTransition: false, // 是否在动画中
    show: false, // 是否展示
    active: false, // 选中
    pin: false, //是否固定
})


function toggleNav(e) {
    if (e) {
        // 收起nav触发，需要收起所有card，然后更新状态变量
        taskExecutionProps.value.show = false
        navProps.value.isNavExpanded = false
        taskExecutionProps.value.pin = false
    } else {
        navProps.value.isNavExpanded = true
    }
}

function toggleNavPin() {
    // 固定或者解除固定
    navProps.value.rail = !navProps.value.rail
    navProps.value.expandOnHover = !navProps.value.expandOnHover

    if (!navProps.value.rail) {
        console.log('pin')
        navProps.value.pinIcon = mdiPinOffOutline
    } else {
        navProps.value.pinIcon = mdiPinOutline
    }
}

function toggleTaskExecutionDetail(e) {
    // 展开或者收起一个taskExecutionCard
    if (taskExecutionProps.value.inTransition) {
        // 说明当前正在一个动画中，禁止同一个card同时执行多个动画
        return
    }

    if (taskExecutionProps.value.pin) {
        return
    }

    taskExecutionProps.value.inTransition = true
    if (taskExecutionProps.value.show) {
        taskExecutionProps.value.show = false;
    } else {
        setTimeout(() => taskExecutionProps.value.show = true, 100)
    }

}



function onTransitionEnd() {
    taskExecutionProps.value.inTransition = false
}

function toggleTaskExecutionDetailPin(e) {
    // 点击item后锁定展开，再点击一次
    if (taskExecutionProps.value.pin) {
        taskExecutionProps.value.pin = false
        taskExecutionProps.value.active = false
    } else {
        taskExecutionProps.value.active = true
        taskExecutionProps.value.show = true
        taskExecutionProps.value.pin = true
    }

}
</script>

<template>

    <v-navigation-drawer :expand-on-hover="navProps.expandOnHover" permanent :rail="navProps.rail"
        @update:rail="toggleNav">

        <v-slide-y-transition :leave-absolute="true" >
            <v-toolbar density="compact" v-if="navProps.isNavExpanded">
                <v-toolbar-title text="任务执行实例"></v-toolbar-title>
            </v-toolbar>
        </v-slide-y-transition>
        <v-divider></v-divider>


        <v-list density="compact" nav>
            <v-slide-y-transition :leave-absolute="true" :group="true" :hide-on-leave="true">

                <v-list-item :key="1" title="My Files" value="myfiles" link>
                    <template v-slot:prepend>
                        <v-progress-circular color="red" :model-value="20" :size="25"
                            class="mr-5"></v-progress-circular>
                    </template>
                    <template>
                        sfdafd
                    </template>

                </v-list-item>



                <!-- 见vuetify-guide -->
                <v-list-item v-if="taskExecutionProps.show" key="2-1" @transitionend="onTransitionEnd" value="detail"
                    @mouseleave="toggleTaskExecutionDetail" @click="toggleTaskExecutionDetailPin"
                    :active="taskExecutionProps.active" link>
                    <template v-slot:default>
                        <div class="v-list-item pa-0">
                            <v-progress-circular color="primary" :model-value="20" :size="25"
                                class="mr-5"></v-progress-circular>
                            <span class="overflow-hidden">fsdfs</span>
                        </div>

                        <v-card class="mt-2" ref="cardRef">
                            <v-table density="compact">

                                <tbody>
                                    <tr>
                                        <th class="text-left">
                                            启动时间
                                        </th>
                                        <th class="text-left">
                                            xxx
                                        </th>
                                    </tr>
                                    <tr>
                                        <th class="text-left">
                                            来源类型
                                        </th>
                                        <th class="text-left">
                                            API
                                        </th>
                                    </tr>
                                    <tr>
                                        <th class="text-left">
                                            来源ID
                                        </th>
                                        <th class="text-left">
                                            aaa
                                        </th>
                                    </tr>
                                </tbody>
                            </v-table>
                        </v-card>
                    </template>
                </v-list-item>
                <v-list-item key="2-2" v-else value="abstract" @mouseenter="toggleTaskExecutionDetail"
                    @transitionend="onTransitionEnd" :active="taskExecutionProps.active" link>
                    <div class="v-list-item pa-0">
                        <v-progress-circular color="primary" :model-value="20" :size="25"
                            class="mr-5"></v-progress-circular>
                        <span class="overflow-hidden">fsdfs</span>
                    </div>

                </v-list-item>




                <v-list-item :prepend-icon="mdiCheck" title="Starred" value="starred" :key="5"></v-list-item>
            </v-slide-y-transition>

        </v-list>


        <div v-if="navProps.isNavExpanded" class="position-absolute pin-btn">
            <v-btn :icon="navProps.pinIcon" @click="toggleNavPin"></v-btn>
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