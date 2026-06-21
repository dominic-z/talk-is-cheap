<script setup>
import { storeToRefs } from 'pinia';
import { useCounterOptionStore } from './CounterOptionStore';
import { useCounterSetupStore } from '@/pinia/core-concept/CounterSetupStore';

const counterOptionStore = useCounterOptionStore()
const counterSetupStore = useCounterSetupStore()

console.log(counterOptionStore)

// 在解构时,如果希望维持响应式,需要使用storeToRefs,如果不使用而直接结构,例如下面的count,这个count对象不会具备响应式特性
// 也就是说,count不会因为increment而被更新
// const {count} = storeToRefs(counterSetupStore)
const {count} = counterSetupStore

</script>


<template>

    <button @click="counterSetupStore.increment">increment</button>
    <div>
        count: {{ counterSetupStore.count }}
    </div>

    <div>
        结构出来的count: {{ count }}
    </div>

    <div>
        doubleCount: {{ counterSetupStore.doubleCount }}
    </div>

</template>