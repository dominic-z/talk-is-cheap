<script setup>
import { nextTick, reactive, ref } from 'vue'

const obj = ref({
    nested: { count: 0 },
    arr: ['foo', 'bar']
})


async function mutateDeeply() {
    // 以下都会按照期望工作
    obj.value.nested.count++

    await nextTick()
    obj.value.arr.push('baz')
}

const state = reactive({ countReactive: 0 })
const CC = reactive(0)

// 可以看出reactive和ref的输出对象的类型是不同的，看起来reactive只是原对象的代理（感觉有点像java的动态代理）
// 而ref本身直接生成了一个RefImp对象，
// 由于xxx（见guide）限制，我们建议使用 ref() 作为声明响应式状态的主要 API。
console.log(obj)
console.log(state)

function log(someLog){
    console.log('log: ',someLog)
}


let refC = ref(0)
log(refC)

</script>


<template>

    <div>{{ obj.nested }}</div>
    <div>{{ obj.arr }}</div>
    <button @click="mutateDeeply">
        check
    </button>

    <button @click="state.countReactive++">
        countReactive: {{ state.countReactive }}
    </button>


    <button @click="refC+=1">
        add {{ refC }}
    </button>


</template>