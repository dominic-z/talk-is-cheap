<script setup>
import { ref,computed } from 'vue'

const cars = [
    {
        name: 'Ford Mustang',
        horsepower: 450,
        fuel: 'Gasoline',
        origin: 'USA',
        price: 55000,
    },
    {
        name: 'Tesla Model S',
        horsepower: 670,
        fuel: 'Electric',
        origin: 'USA',
        price: 79999,
    },
    {
        name: 'BMW M3',
        horsepower: 503,
        fuel: 'Gasoline',
        origin: 'Germany',
        price: 70000,
    },
    {
        name: 'Audi RS6',
        horsepower: 591,
        fuel: 'Gasoline',
        origin: 'Germany',
        price: 109000,
    },
    {
        name: 'Chevrolet Camaro',
        horsepower: 650,
        fuel: 'Gasoline',
        origin: 'USA',
        price: 62000,
    },
    {
        name: 'Porsche 911',
        horsepower: 379,
        fuel: 'Gasoline',
        origin: 'Germany',
        price: 101000,
    },
    {
        name: 'Jaguar F-Type',
        horsepower: 575,
        fuel: 'Gasoline',
        origin: 'UK',
        price: 61000,
    },
    {
        name: 'Mazda MX-5',
        horsepower: 181,
        fuel: 'Gasoline',
        origin: 'Japan',
        price: 26000,
    },
    {
        name: 'Nissan GT-R',
        horsepower: 565,
        fuel: 'Gasoline',
        origin: 'Japan',
        price: 113540,
    },
    {
        name: 'Mercedes-AMG GT',
        horsepower: 523,
        fuel: 'Gasoline',
        origin: 'Germany',
        price: 115900,
    },
]

const headers = ref([
    { title: 'Car Model', key: 'name', align: 'start', headerProps: { class: 'font-weight-bold' } },
    { title: 'Horsepower', key: 'horsepower', align: 'end' },
    { title: 'Fuel Type', key: 'fuel', align: 'start' },
    { title: 'Origin', key: 'origin', align: 'start' },
    { title: 'Price ($)', key: 'price', align: 'end' },
])

const loading = ref(true)
const showLoadMore = ref(true)

const tablePageInfo = ref({
    total: 0,
    itemsPerPage: 4,
    itemsPerLoad: 4, // 每次加载4个item
})

const shownItems = ref([])
const intersector = {
    handler: loadMore,
    options: {
        threshold: 0.5, // 50%
    }
}


function loadMore(isIntersecting, entries, observer) {
    // 仅仅修改page信息，具体触发fetch操作通过@update:options
    console.log("loadmore")
    if (isIntersecting) {
        tablePageInfo.value.total = cars.length
        tablePageInfo.value.itemsPerPage += tablePageInfo.value.itemsPerLoad
    }
}

function fetchData(e) {
    // console.log(e)
    loading.value = true
    setTimeout(() => {
        shownItems.value = cars.slice(0, tablePageInfo.value.itemsPerPage)
        loading.value = false
        if(tablePageInfo.value.itemsPerPage>cars.length){
            showLoadMore.value = false
        }
    }, 1000)
}
</script>

<template>
    <v-data-table-server class="border-thin" v-model:items-per-page="tablePageInfo.itemsPerPage" :headers="headers"
        :hide-default-footer="true" :items="shownItems" :disable-sort="true" :items-length="tablePageInfo.total"
        :loading="loading" item-value="name" density="compact" @update:options="fetchData" striped="even" height="150" :fixed-header="true">


        <!-- 一个无限下列器 -->
        <template v-slot:item="{ item }">
            <tr>
                <td>{{ item.name }}</td>
                <td>{{ item.horsepower }}</td>
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