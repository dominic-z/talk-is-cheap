<script setup>
import { defineAsyncComponent } from 'vue'

const AsyncComp = defineAsyncComponent(() => {
    return new Promise((resolve, reject) => {
        // ...从服务器获取组件
        resolve(import('./MyComponent.vue'))
    })
})


const AsyncMyComp = defineAsyncComponent(() => import('./MyComponent.vue'))
</script>

<template>

    <AsyncComp></AsyncComp>
    <AsyncMyComp></AsyncMyComp>
</template>