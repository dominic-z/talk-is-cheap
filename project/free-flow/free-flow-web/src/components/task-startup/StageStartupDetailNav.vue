<script setup>
import { mdiWindowClose } from '@mdi/js'
import { ref } from 'vue'
import StageExecutionLogTable from './StageExecutionLogTable.vue'
const emitter = defineEmits(['update:draw'])

const props = defineProps(['draw'])
const navWidth = ref(600)


const executions = ref([
    {
        'stageExecutionId': 1,
        'launchTime': '2020-08-01 12:20:10',
    },
    {
        'stageExecutionId': 2,
        'launchTime': '2020-08-02 12:20:10',
    },
    {
        'stageExecutionId': 3,
        'launchTime': '2020-08-03 12:20:10',
    },
])


const params =  ref([
    {
        'key':'a',
        'value':'b',
    }
])


const logs = ref([
    'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.',

    'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.',
    '11'
])
</script>

<template>

    <v-navigation-drawer v-model="props.draw" :location="'right'" :width="navWidth" floating="">
        <v-toolbar density="compact">

            <v-btn :icon="mdiWindowClose" @click="$emit('update:draw')"></v-btn>
            <v-toolbar-title text="阶段信息"></v-toolbar-title>
        </v-toolbar>

        <v-card class="border-thin">
            <v-card-title>基本信息</v-card-title>
            <v-card-text>
                <v-table density="compact" height="100px" striped="even" fixed-header class="border-thin text-break">
                    <!-- 列宽定义区域 -->
                    <colgroup>
                        <col style="width: 25%;"> <!-- 第 1 列宽度 -->
                        <col style="width: 75%;"> <!-- 第 2 列宽度 -->
                    </colgroup>
                    <tbody>
                        <tr>
                            <td>version</td>
                            <td>1.1</td>
                        </tr>
                        <tr>
                            <td>Startup Time</td>
                            <td>2021-07-01</td>
                        </tr>
                        <tr>
                            <td>Retry Times</td>
                            <td>3</td>
                        </tr>
                    </tbody>
                </v-table>

            </v-card-text>

        </v-card>

        <v-card class=" border-thin">
            <v-card-title>启动参数</v-card-title>
            <v-card-text>
                <v-table density="compact" height="200px" striped="even" fixed-header class="border-thin text-break">
                    <!-- 列宽定义区域 -->
                    <colgroup>
                        <col style="width: 25%;"> <!-- 第 1 列宽度 -->
                        <col style="width: 75%;"> <!-- 第 2 列宽度 -->
                    </colgroup>
                    <thead>

                        <tr class="text-h6">
                            <th class="text-left">
                                Parameter
                            </th>
                            <th class="text-left">
                                Value
                            </th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="kv in params" :key="kv.key">
                            <td>{{ kv.key }}</td>
                            <td>{{ kv.value }}</td>
                        </tr>
                    </tbody>
                </v-table>
            </v-card-text>
        </v-card>

        <v-divider></v-divider>

        <v-card class="border-thin" >


            <v-card-title>
                阶段执行信息
            </v-card-title>

            <!-- hide-details会隐藏掉一个小尾巴 -->
            <v-select :items="executions" item-title="stageExecutionId" label="阶段执行实例" :hide-details="true">
                <template v-slot:item="{ props, item }">
                    <v-list-item v-bind="props" :subtitle="item.raw.launchTime"
                        @click="() => console.log(props)"></v-list-item>
                </template>
            </v-select>


            <v-card class="border-thin" :style="{'height':'100%'}">
                <v-card-title>日志</v-card-title>
                <v-card-text>
                    

                    <StageExecutionLogTable></StageExecutionLogTable>
                </v-card-text>
            </v-card>
        </v-card>

    </v-navigation-drawer>

</template>