<script setup>
let props = defineProps(['id', 'obj'])
</script>

<template>

    <div>User Id is {{ props.id }}</div>
    <div>User obj is {{ props.obj }}</div>
</template>