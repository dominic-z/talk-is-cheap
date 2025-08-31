<script setup>

import { mdiWindowClose } from '@mdi/js'
import { ref } from 'vue'



const navWidth = ref(400)
// const props = defineProps(['draw'])
// const emitter = defineEmits(['update:draw'])
const draw = defineModel('draw')




const paramsDefinitionItems = ref([
    { 'paramKey': 'aa', 'paramType': 'string', 'desc': '描述1' },
    { 'paramKey': 'bb', 'paramType': 'Object', 'desc': '描述2' }
])

const paramsDeifinitionHeaders = ref([
    {'title':'参数',key:'paramKey',align:'start',minWidth:'100px',headerProps:{class:'font-weight-bold'}},
    {'title':'参数类型',key:'paramType',align:'start',minWidth:'150px',headerProps:{class:'font-weight-bold'}},
    {'title':'描述',key:'desc',align:'start',minWidth:'300px',headerProps:{class:'font-weight-bold'}},
])
const paramDetailWidth = ref(500)

</script>

<template>

    <v-navigation-drawer v-model="draw" :location="'right'" :width="navWidth" floating="">
        <v-toolbar density="compact">
            <v-btn :icon="mdiWindowClose" @click="draw=!draw"></v-btn>
            <v-toolbar-title text="阶段定义"></v-toolbar-title>
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
                            <td>Release Time</td>
                            <td>2021-07-01</td>
                        </tr>
                        <tr>
                            <td>Retry Times</td>
                            <td>3</td>
                        </tr>
                        <tr>
                            <td>入参类型</td>
                            <td>java.lang.String</td>
                        </tr>
                    </tbody>
                </v-table>

            </v-card-text>

        </v-card>


        <v-card class=" border-thin">
            <v-card-title>入参定义</v-card-title>
            <v-card-text>
                <v-data-table :headers="paramsDeifinitionHeaders" :width="paramDetailWidth" :items="paramsDefinitionItems" item-key="paramKey" hide-default-footer disable-sort class="border-thin"
                ></v-data-table>
            </v-card-text>
        </v-card>




    </v-navigation-drawer>
</template>