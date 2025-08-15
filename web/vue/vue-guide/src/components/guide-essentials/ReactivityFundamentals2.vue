<script setup>
import { nextTick, reactive, ref } from 'vue'

const obj = ref({
    nested: { count: 0 },
    arr: ['foo', 'bar'],
    forReplace: { app: 'origin'}
})


const objs = ref([{
    id: 1,
    forReplace: { app: 'origin1'}
}])



async function mutateDeeply() {
    // 以下都会按照期望工作
    obj.value.nested.count++
    // await nextTick()执行完成之后，相当于本次事件循环完成了，就是说这次调用之前的变更都完成了。
    await nextTick()
    obj.value.arr.push('baz')

    // 测试直接替换
    obj.value.forReplace = {app:'new'}

    // 测试列表过滤后直接替换
    // Vue中filters过滤器无效的原因会失效 https://blog.csdn.net/weixin_41568816/article/details/119344074
    // const os1 = objs.value.filter(o=>{
    //     console.log(o.id);
    //     return o.id===1;
    // })
    // console.log(os1)
    // os1.forReplace = { app: 'origin2'}

    // 需要手写for循环
    for(let o of objs.value){
        console.log(o)
        if(o.id == 1){
            o.forReplace = { app: 'origin2'}
        }
    }
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
    <div>{{ obj.forReplace }}</div>
    <div>{{ objs }}</div>
    <button @click="mutateDeeply">
        mutateDeeply
    </button>

    <button @click="state.countReactive++">
        countReactive: {{ state.countReactive }}
    </button>


    <!-- 一些奇怪的解包规则，vue支持一些默认的解包，不需要手动调用refC.value -->
    <button @click="refC+=1">
        add {{ refC }}
    </button>


</template>