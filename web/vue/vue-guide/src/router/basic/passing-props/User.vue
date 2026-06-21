<script setup>
let props = defineProps(['id', 'obj','userProp'])
// 这个组件作为routerview，但是不依赖route对象，这可以让组件和router相关的功能解耦
</script>

<template>

    <div>User Id is {{ props.id }}</div>
    <div>User obj is {{ props.obj }}</div>
    <div>User userProp is {{ props.userProp }}</div>
</template>