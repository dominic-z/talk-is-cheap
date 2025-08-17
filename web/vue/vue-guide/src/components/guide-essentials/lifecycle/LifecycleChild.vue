<script setup>

import { onMounted, onUnmounted, onUpdated, ref } from 'vue'
onMounted(() => {
    console.log(`child component is now mounted.`)
})

onUpdated(()=>{
    console.log(`child component is now updated.`)
})

onUnmounted(()=>{
    console.log(`child component is now unmounted.`)
})

</script>

<template>

    <div>
        child
    </div>

</template>