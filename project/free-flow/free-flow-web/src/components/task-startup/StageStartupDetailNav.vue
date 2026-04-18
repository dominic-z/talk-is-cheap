<script setup>
import { mdiWindowClose } from '@mdi/js'
import { ref } from 'vue'
import StageExecutionLogTable from './StageExecutionLogTable.vue'
const emitter = defineEmits(['update:draw'])

const props = defineProps(['draw', 'id'])
const navWidth = ref(600)
// console.log(props)

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


const params = ref([
    {
        'key': 'a',
        'value': 'b',
    }
])

const paramsJson = ref({
    'key1': 'aa',
    'list1': [1, 2, 3],
    'deepObj': {
        'key2': '1',
        'list2': [3, 4, 5]
    }
})


const items = ['Foo', 'Bar', 'Fizz', 'Buzz']

</script>

<template>

    <v-navigation-drawer v-model="props.draw" :location="'right'" :width="navWidth" floating="">
        <v-toolbar density="compact">

            <v-btn :icon="mdiWindowClose" @click="$emit('update:draw')"></v-btn>
            <!-- <v-toolbar-title text="阶段信息"></v-toolbar-title> -->
            <v-select :items="items" density="compact" label="Compact" :hide-details="true"
            :tile="true" :style="{'border-style':'none'}">
            </v-select>

        </v-toolbar>

        <v-card class="border-thin">
            <v-card-title>基本信息 {{ props.id }}</v-card-title>

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
            <v-card-title>全局参数快照</v-card-title>
            <v-card-text>
                <json-viewer :value="paramsJson" :expand-depth=5 copyable boxed sort></json-viewer>
            </v-card-text>
        </v-card>


        <v-card class=" border-thin">
            <v-card-title>当前阶段启动参数</v-card-title>
            <v-card-text>
                <json-viewer :value="paramsJson" :expand-depth=5 copyable boxed sort></json-viewer>
            </v-card-text>
        </v-card>

        <v-divider></v-divider>

        <v-card class="border-thin">


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


            <v-card class="border-thin" :style="{ 'height': '100%' }">
                <v-card-title>日志</v-card-title>
                <v-card-text>


                    <StageExecutionLogTable></StageExecutionLogTable>
                </v-card-text>
            </v-card>
        </v-card>

    </v-navigation-drawer>

</template>