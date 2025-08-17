<script setup>

import { ref } from 'vue'
const classObject = ref({
    someClass: true,
    'div-danger': false
})

// 用reactive也行
const styleObject = ref({
    color: 'red',
    fontSize: '30px'
})
</script>

<template>

    <div :class="classObject">asfsdfas</div>
    <div :class="$attrs.class">attrs.class</div>
    <div :style="styleObject">styleObject</div>
    <div :style="{ color: activeColor, fontSize: fontSize + 'px' }">inlinStyle</div>
</template>