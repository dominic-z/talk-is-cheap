<script setup>
import { ref, reactive, computed } from 'vue';
import ListComponent from './ListComponent.vue';
const parentMessage = ref('Parent')
const items = ref([{ message: 'Foo' }, { message: 'Bar' }])
const myObject = reactive({
    title: 'How to do lists in Vue',
    author: 'Jane Doe',
    publishedAt: '2016-04-10'
})

const numbers = ref([1, 2, 3, 4, 5])

const evenNumbers = computed(() => {
    return [...numbers.value].reverse().filter((n) => n % 2 === 0)
})


let bound = 11
const sets = ref([
    [1, 2, 3, 4, 5],
    [6, 7, 8, 9, 10]
])
function addNewArrToSets(){
    sets.value.push([...Array(5).keys()].map(i => i + bound))
    bound += 5
}


function even(numbers) {
    return numbers.filter((number) => number % 2 === 0)
}

const todos = [
    {
        name: 'a',
        isComplete: true
    }, {
        name: 'b',
        isComplete: false
    }
]

const orderedList = ref(
    [
        {
            id: 1,
            name: 'name1'
        },
        {
            id: 2,
            name: 'name2'
        }
    ]
)

function updateOrderedList() {
    const old1 = orderedList.value[0]
    const old2 = orderedList.value[1]
    orderedList.value = [
        old2,
        old1
    ];
    console.log(orderedList.value)
}

</script>

<template>

    <li v-for="item in items">
        {{ item.message }}
    </li>
    <hr />
    <li v-for="(item, index) in items">
        {{ parentMessage }} - {{ index }} - {{ item.message }}
    </li>
    <hr />
    <li v-for="{ message } in items">
        {{ message }}
    </li>
    <hr />
    <!-- 有 index 索引时 -->
    <li v-for="({ message }, index) in items">
        {{ message }} {{ index }}
    </li>

    <hr />
    <li v-for="(value, key) in myObject">
        {{ key }}: {{ value }}
    </li>

    <hr />
    <li v-for="n in evenNumbers">{{ n }}</li>
    <ul v-for="numbers in sets">
        <li v-for="n in even(numbers)">{{ n }}</li>
    </ul>
    <button @click="addNewArrToSets">addNewArrToSets</button>

    <hr />
    <!-- 输出的结果是多个li -->
    <template v-for="todo in todos">
        <li v-if="!todo.isComplete">
            {{ todo.name }}
        </li>
    </template>

    <hr />
    <!-- 通过key来标识每个item -->
     <!-- v-for挂在了最外层的div上，那么就会创建多个outer的div -->
    <div name="outer" v-for="item in orderedList" :key="item.id">
        <!-- 内容 -->
        <div>{{ item }}</div>
    </div>
    <button @click="updateOrderedList">updateOrderedList</button>

    <hr />
    <ListComponent v-for="(item, index) in items" :item="item" :index="index" :key="item.id"></ListComponent>

</template>