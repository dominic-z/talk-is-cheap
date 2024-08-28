<script setup>
import ClassAndStyle2 from './ClassAndStyle2.vue'
import { ref } from 'vue'
const classObject = ref({
    active: true,
    'text-danger': false
})

</script>

<style>
.baz{
    color:red
}
</style>

<template>
<ClassAndStyle2 :class="{ baz: true }"></ClassAndStyle2>
    <!-- :class是使用vue的动态绑定 -->
    <div :class="classObject">classObject</div>
    <!-- class是原生的 -->
    <div class="baz">baz</div>

</template>