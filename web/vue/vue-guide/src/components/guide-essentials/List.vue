<script setup>
import { ref, reactive, computed } from 'vue';
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

const sets = ref([
    [1, 2, 3, 4, 5],
    [6, 7, 8, 9, 10]
])

function even(numbers) {
    return numbers.filter((number) => number % 2 === 0)
}

</script>

<template>

    <li v-for="item in items">
        {{ item.message }}
    </li>

    <li v-for="(item, index) in items">
        {{ parentMessage }} - {{ index }} - {{ item.message }}
    </li>

    <li v-for="{ message } in items">
        {{ message }}
    </li>

    <!-- 有 index 索引时 -->
    <li v-for="({ message }, index) in items">
        {{ message }} {{ index }}
    </li>

    <li v-for="(value, key) in myObject">
        {{ key }}: {{ value }}
    </li>

    <li v-for="n in evenNumbers">{{ n }}</li>
    <ul v-for="numbers in sets">
        <li v-for="n in even(numbers)">{{ n }}</li>
    </ul>

    
</template>