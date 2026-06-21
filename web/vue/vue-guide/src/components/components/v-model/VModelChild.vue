<script setup>
const firstName = defineModel('firstName')
const lastName = defineModel('lastName')

// defineModel简化了Child的写法
</script>

<template>
  <input type="text" v-model="firstName" />
  <input type="text" v-model="lastName" />
</template>