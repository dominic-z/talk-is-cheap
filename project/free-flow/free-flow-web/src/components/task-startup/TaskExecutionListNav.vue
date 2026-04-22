<script setup>
// 页面左侧的execution导航栏

import { TaskStageStatus } from '@/enums/task';
import { API_PATHS } from '@/utils/api/paths';
import request from '@/utils/request';
import { mdiCloseCircleOutline } from '@mdi/js';
import { mdiCheckCircleOutline } from '@mdi/js';
import { mdiClockTimeEightOutline } from '@mdi/js';
import { mdiPinOutline, mdiPinOffOutline, mdiMenuRight, mdiMenuDown } from '@mdi/js';
import { mdiCheck } from '@mdi/js';
import { mdiFolder } from '@mdi/js';
import { mdiDotsVertical } from '@mdi/js';
import { ref, onMounted } from 'vue';

// const a = mdiPinOutline

const props = defineProps(['taskStartupId','taskExecutionInfoList'])
const activeTaskExecutionId = defineModel('activeTaskExecutionId')

const navProps = ref({
    expandOnHover: true,
    isNavExpanded: false, // nav是否有展开
    rail: true, // 是否开启滑轨，true就是开启滑轨，如果为false就是关闭滑轨，会一直展开
    pinIcon: mdiPinOutline,
    showExecutionWhenNavExpand: false
})

function toggleNav(e) {
    if (e) {
        // 收起nav触发，需要收起所有card，然后更新状态变量
        navProps.value.isNavExpanded = false
        navProps.value.showExecutionWhenNavExpand = false
        for (let taskExecution of props.taskExecutionInfoList) {
            taskExecution.showDetail = false
            taskExecution.pin = false
        }
    } else {
        navProps.value.isNavExpanded = true
        navProps.value.showExecutionWhenNavExpand = false
        setTimeout(() => navProps.value.showExecutionWhenNavExpand = true, 50)
    }
}

function toggleNavPin() {
    // 固定或者解除固定
    navProps.value.rail = !navProps.value.rail
    navProps.value.expandOnHover = !navProps.value.expandOnHover

    if (!navProps.value.rail) {
        navProps.value.pinIcon = mdiPinOffOutline
    } else {
        navProps.value.pinIcon = mdiPinOutline
    }
}

let timer = null
function onMouseEnterTaskExecution(taskExecution) {
    if (timer) {
        clearTimeout(timer)
    }
    timer = setTimeout(() => taskExecution.showDetail = true, 1000)
}

function onMouseLeaveTaskExecution(taskExecution) {
    // https://www.qianwen.com/share/chat/8a28e7524cff470e8f647f9281d79236
    // console.log(e.target)
    if (taskExecution.pin) {
        // pin状态则一直展开
        return
    }
    if (timer) {
        clearTimeout(timer)
    }
    taskExecution.showDetail = false;
}



function toggleTaskExecutionDetailPin(taskExecution) {
    if (timer) {
        clearTimeout(timer)
    }
    // 点击item后锁定展开，再点击一次解除锁定
    if (!('pin' in taskExecution)) {
        taskExecution.pin = true
    } else {
        taskExecution.pin = !taskExecution.pin
    }
    taskExecution.showDetail = true
    activeTaskExecutionId.value = taskExecution.id
}



</script>

<template>
    
    <v-navigation-drawer :expand-on-hover="navProps.expandOnHover" width="270" permanent :rail="navProps.rail"
        @update:rail="toggleNav">
        <v-slide-y-transition :leave-absolute="true">
            <v-toolbar density="compact" v-if="navProps.isNavExpanded">
                <v-toolbar-title text="任务执行实例"></v-toolbar-title>
            </v-toolbar>
        </v-slide-y-transition>
        <v-divider></v-divider>


        <v-list density="compact" nav  v-if="props.taskExecutionInfoList && props.taskExecutionInfoList.length">


            <!-- <v-list-item :key="1" title="My Files" value="myfiles" link>
                <template v-slot:prepend>
                    <v-progress-circular color="red" :model-value="20" :size="25" class="mr-5"></v-progress-circular>
                </template>
                <template>
                    sfdafd
                </template>

            </v-list-item> -->



            <!-- 见vuetify-guide -->
            <v-list-item v-for="(taskExecution,index) in props.taskExecutionInfoList" :key="taskExecution.id"
                @mouseenter="(e) => onMouseEnterTaskExecution(taskExecution)"
                @mouseleave="(e) => onMouseLeaveTaskExecution(taskExecution)"
                @click="(e) => toggleTaskExecutionDetailPin(taskExecution)" link
                :active="taskExecution.id==activeTaskExecutionId"
                >
                <!-- <template v-slot:prepend>
                    <v-progress-circular color="primary" :model-value="20" :size="25"
                            class="mr-5"></v-progress-circular>
                </template> -->

                <template v-slot:default>
                    <div class="v-list-item pa-0">
                        <v-icon v-if="taskExecution.status == TaskStageStatus.FAILED"
                            :icon="mdiCloseCircleOutline" color="red" :size="30"  class="mr-2"></v-icon>

                        <v-progress-circular v-else-if="taskExecution.status == TaskStageStatus.RUNNING" color="primary"
                            indeterminate :size="25" class="ml-1 mr-4"></v-progress-circular>

                        <v-icon v-else-if="taskExecution.status == TaskStageStatus.TIME_OUT" :icon="mdiClockTimeEightOutline" size="30" class="mr-2"></v-icon>
                        <v-icon v-else-if="taskExecution.status == TaskStageStatus.SUCCEEDED" :icon="mdiCheckCircleOutline" color="green" size="30" class="mr-2"></v-icon>
                        <v-icon v-else
                            :icon="mdiCloseCircleOutline" color="red" size="30" class="mr-2"></v-icon>
                        <!-- todo: 这个会跳一下 -->
                        <span v-if="navProps.isNavExpanded" class="ml-3">{{ taskExecution.startTime }}</span>
                        <v-icon v-if="taskExecution.showDetail" :icon="mdiMenuDown"></v-icon>
                        <v-icon v-else :icon="mdiMenuRight"></v-icon>
                    </div>

                    <v-expand-transition>
                        <div v-if="taskExecution.showDetail">
                            <v-card class="mt-2" ref="cardRef">
                                <v-table density="compact">
                                    <tbody>
                                        <tr>
                                            <th class="text-left">
                                                id
                                            </th>
                                            <th class="text-left">
                                                {{ taskExecution.id }}
                                            </th>
                                        </tr>
                                        <tr>
                                            <th class="text-left text-no-wrap">
                                                执行节点
                                            </th>
                                            <th class="text-left text-break">
                                                {{ taskExecution.assignedWorkerAddr }}
                                            </th>
                                        </tr>
                                        <tr>
                                            <th class="text-left">
                                                状态
                                            </th>
                                            <th class="text-left">
                                                {{ taskExecution.status }}
                                            </th>
                                        </tr>
                                        <tr>
                                            <th class="text-left">
                                                完成时间
                                            </th>
                                            <th class="text-left">
                                                {{ taskExecution.completionTime }}
                                            </th>
                                        </tr>
                                    </tbody>
                                </v-table>
                            </v-card>
                        </div>
                    </v-expand-transition>

                </template>
            </v-list-item>


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

.grid-container {
    display: grid;
    /* 第一列 200px，第二列 300px */
    grid-template-columns: 1fr 2fr;
}
</style>