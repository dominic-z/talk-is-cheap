<script setup>
// https://techblog.geekyants.com/enhancing-vuetify-data-table-performance-with-infinite-scroll

//


import { ref, computed } from 'vue'

// This is the number of rows that should be shown in at any given moment in the data table

// Generate random items
function generateRandomItems() {
    const items = [];

    for (let i = 0; i < 1000; i++) {
        const item = {
            name: generateRandomString(),
            calories: getRandomNumber(100, 500),
            fat: getRandomFloat(0, 20),
            carbs: getRandomNumber(0, 50),
            protein: getRandomFloat(0, 10),
            iron: getRandomNumber(0, 5),
            calcium: getRandomNumber(0, 15),
            magnesium: getRandomNumber(0, 15),
            potassium: getRandomNumber(0, 15),
        };
        items.push(item);
    }

    return items;
}

// Helper functions to generate random data
function generateRandomString() {
    const characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    let result = "";

    for (let i = 0; i < 10; i++) {
        result += characters.charAt(Math.floor(Math.random() * characters.length));
    }

    return result;
}

function getRandomNumber(min, max) {
    return Math.floor(Math.random() * (max - min + 1) + min);
}

function getRandomFloat(min, max) {
    return (Math.random() * (max - min) + min).toFixed(1);
}

// 每次加载20个item
const itemsPerLoad = 20
const dataTable = ref(null)
const itemsPerPage = ref(itemsPerLoad)
const itemStartIndex = ref(0)
const itemEndIndex = ref(itemsPerLoad)
const loading = ref(false)
const headers = ref([
    { title: "Dessert (100g serving)", align: "start", sortable: false, key: "name" },
    { title: "Calories", key: "calories" },
    { title: "Fat (g)", key: "fat" },
    { title: "Carbs (g)", key: "carbs" },
    { title: "Protein (g)", key: "protein" },
    { title: "Iron (%)", key: "iron" },
    { title: "Calcium (%)", key: "calcium" },
    { title: "Magnesium (%)", key: "magnesium" },
    { title: "Potassium (%)", key: "potassium" },
])
const desserts = generateRandomItems() // Generate random items
const itemsToDisplay = computed(() => desserts.slice(itemStartIndex.value, itemEndIndex.value))
console.log(itemsToDisplay.value)

function scrollToRow(index) {
    const table = dataTable.value.$el.querySelector("table");
    const row = table.rows[index];
    console.log(table.rows)
    console.log(index)
    console.log(row)
    if (row) {
        row.scrollIntoView(true);
    }
}

function loadMore(isIntersecting, entries, observer,) {
    console.log('loadmore')
    //only change the indexes when isIntersecting is true i.e the component is visible in viewport
    if (isIntersecting) {
        // 如果为true说明出现了
        loading.value = true

        // 模拟加载
        setTimeout(() => {
            const indexesLeft = desserts.length - itemEndIndex.value;
            if (indexesLeft < itemsPerLoad) {
                itemEndIndex.value = itemsToDisplay.value.length;
                itemsPerPage.value = itemsToDisplay.value.length;
            } else {
                itemEndIndex.value += itemsPerLoad
                itemsPerPage.value += itemsPerLoad
            }
            loading.value = false
            console.log(loading.value)
            // these values need to be updated according to your use case and desired value for itemsPerPage
            // 加载完成后向上滚动5个单位，运行到这里的时候table并未完成更新，因此还需要将itemEndIndex还原回原来的状态
            scrollToRow(itemEndIndex.value - itemsPerLoad - 5);
        }, 2000)

    }
}

const intersector = {
    handler: loadMore,
    options: {
        threshold: 0.5, // 50%
    }
}

</script>

<!-- Vue template -->
<template>

    <v-data-table ref="dataTable" :headers="headers" :items="itemsToDisplay" :items-per-page="itemsPerPage"
        class="elevation-1" disable-pagination hide-default-footer height="400" :loading="loading">


        <template v-slot:body.append>
            <!-- 加了quiet就不会在元素创建的时候触发handler函数，也就是说创建不会被认为是一次响应 -->
            <!-- Will not invoke the handler function if the element is visible when the IntersectionObserver is created. -->
            <tr v-intersect.quiet="intersector">
                <td :colspan="headers.length">
                    <v-skeleton-loader max-width="300" type="list-item"
                        :style="{ 'left': '50%', 'transform': 'translate(-50%,0)' }" />

                </td>
            </tr>
        </template>
    </v-data-table>
</template>