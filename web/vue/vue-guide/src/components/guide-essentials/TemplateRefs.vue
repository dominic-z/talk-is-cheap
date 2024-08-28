<script setup>
import { ref, onMounted } from 'vue'

// 声明一个 ref 来存放该元素的引用
// 必须和模板里的 ref 同名
// 用来作为指向dom元素的对象

const input = ref(null)

onMounted(() => {
    console.log(input.value)
    input.value.focus()
})

let elInput = ref(null)
</script>

<template>
    <input id="id1" ref="input" @change="(v) => {
        // 这里这个input变量值得就是这个input的dom元素了，还可以使用dom的api
        console.log(input)
        console.log(input.getAttribute('id'))
    }" />


    <input :ref="(el) => {

        console.log('执行绑定ref')
        elInput = el
        /* 将 el 赋值给一个数据属性或 ref 变量 */
    }" @input="(e) => { console.log(e); elInput.setAttribute('id', e.data) }">
</template>
