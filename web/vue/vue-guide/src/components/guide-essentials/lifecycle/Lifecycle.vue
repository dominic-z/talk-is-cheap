<script setup>

import { onMounted, onUnmounted, onUpdated, ref } from 'vue'
import LifecycleChild from './LifecycleChild.vue'
onMounted(() => {
    console.log(`the component is now mounted.`)
})

onUpdated(()=>{
    console.log(`the component is now updated.`)
})

onUnmounted(()=>{
    console.log(`the component is now unmounted.`)
})

let refId = ref(1)
</script>

<template>

    <div :id="refId">
        {{ refId }}

        <button @click="()=>refId+=1">add refId</button>
    </div>

    <!-- 控制是否渲染 -->
    <LifecycleChild v-if="refId%2==0"></LifecycleChild>

</template>