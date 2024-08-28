<script setup>
const props = defineProps(['foo'])

console.log(props.foo)
</script>

<template>
    <h1>1111，{{props.foo}}</h1>
</template>