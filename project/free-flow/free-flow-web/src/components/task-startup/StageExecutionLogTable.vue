<script setup>
import { API_PATHS } from '@/utils/api/paths'
import request from '@/utils/request'
import { ref, computed, watch } from 'vue'

const props = defineProps(['stageExecution'])

// const cars = [
//     {
//         name: 'Ford Mustang',
//         horsepower: 450,
//         fuel: 'Gasoline',
//         origin: 'USA',
//         price: 55000,
//     },
//     {
//         name: 'Tesla Model S',
//         horsepower: 670,
//         fuel: 'Electric',
//         origin: 'USA',
//         price: 79999,
//     },
//     {
//         name: 'BMW M3',
//         horsepower: 503,
//         fuel: 'Gasoline',
//         origin: 'Germany',
//         price: 70000,
//     },
//     {
//         name: 'Audi RS6',
//         horsepower: 591,
//         fuel: 'Gasoline',
//         origin: 'Germany',
//         price: 109000,
//     },
//     {
//         name: 'Chevrolet Camaro',
//         horsepower: 650,
//         fuel: 'Gasoline',
//         origin: 'USA',
//         price: 62000,
//     },
//     {
//         name: 'Porsche 911',
//         horsepower: 379,
//         fuel: 'Gasoline',
//         origin: 'Germany',
//         price: 101000,
//     },
//     {
//         name: 'Jaguar F-Type',
//         horsepower: 575,
//         fuel: 'Gasoline',
//         origin: 'UK',
//         price: 61000,
//     },
//     {
//         name: 'Mazda MX-5',
//         horsepower: 181,
//         fuel: 'Gasoline',
//         origin: 'Japan',
//         price: 26000,
//     },
//     {
//         name: 'Nissan GT-R',
//         horsepower: 565,
//         fuel: 'Gasoline',
//         origin: 'Japan',
//         price: 113540,
//     },
//     {
//         name: 'Mercedes-AMG GT',
//         horsepower: 523,
//         fuel: 'Gasoline',
//         origin: 'Germany',
//         price: 115900,
//     },
// ]

// const carsHeaders = ref([
// { title: 'Car Model', key: 'name', align: 'start', headerProps: { class: 'font-weight-bold' } },
// { title: 'Horsepower', key: 'horsepower', align: 'end' },
// { title: 'Fuel Type', key: 'fuel', align: 'start' },
// { title: 'Origin', key: 'origin', align: 'start' },
// { title: 'Price ($)', key: 'price', align: 'end' },
// ])


const headers = ref([
    { title: '时间', key: 'createTime', align: 'start', headerProps: { class: 'font-weight-bold' } },
    { title: '日志内容', key: 'logContent', align: 'start', headerProps: { class: 'font-weight-bold' } },
])


const tablePageInfo = ref({
    total: 0,
    itemsPerLoad: 10, // 每次加载4个item
    searchAfter: null
})

const shownItems = ref([])
const intersector = {
    handler: loadMore,
    options: {
        threshold: 0.9, // 90% 调大一点，如果太小，比如说50%，可能出现结果新增了一些，但是这个响应组件仍然保持展示超过50%，这样就无法保证能够触发下一次的翻页
    }
}

const loading = ref(false)
const showLoadMore = ref(true)
function loadMore(isIntersecting, entries, observer) {
    // 仅仅修改page信息，具体触发fetch操作通过@update:options
    // console.log("aaa",isIntersecting, entries, observer)
    if (isIntersecting) {
        fetchData()
    }
}

async function fetchData() {
    loading.value = true
    showLoadMore.value = true
    if (props.stageExecution == null) {
        loading.value = false
        showLoadMore.value = false
        return
    }
    const respBody = await request.get(API_PATHS.TASK_INFO.STAGE_EXECUTION_LOGS, {
        params: {
            stageExecutionId: props.stageExecution.id,
            pageSize: tablePageInfo.value.itemsPerLoad,
            searchAfter: tablePageInfo.value.searchAfter
        }
    })
    if (respBody.data != null && respBody.data.length != 0) {
        shownItems.value.push(...respBody.data)
        tablePageInfo.value.searchAfter = respBody.data[respBody.data.length - 1].sort
        tablePageInfo.total += tablePageInfo.value.itemsPerLoad
    } else{
        showLoadMore.value = false
    }
    loading.value = false

}

watch(() => props.stageExecution, () => {
    shownItems.value = []
    tablePageInfo.value.total = 10
    tablePageInfo.value.searchAfter = null
    fetchData()
})
</script>

<template>
    <v-data-table-server class="border-thin"  :headers="headers"
        :hide-default-footer="true" :items="shownItems" :disable-sort="true" :items-length="tablePageInfo.total"
        :loading="loading"  density="compact" @update:options="fetchData" striped="even" height="150"
        :fixed-header="true">


        <!-- 一个无限下列器 -->
        <template v-slot:item="{ item }">
            <tr>
                <td>{{ item.createTime }}</td>
                <td>{{ item.logContent }}</td>
            </tr>
        </template>

        <template v-slot:body.append v-if="showLoadMore">
            <!-- 清空背景色https://www.doubao.com/thread/w8ba1a5ea0b54decf -->
            <tr v-intersect.quiet="intersector" :style="{ 'background-image': 'initial' }">
                <td :colspan="headers.length">
                    <v-skeleton-loader max-width="200" type="list-item"
                        :style="{ 'left': '50%', 'transform': 'translate(-50%,0)' }" />
                </td>
            </tr>
        </template>

    </v-data-table-server>
</template>