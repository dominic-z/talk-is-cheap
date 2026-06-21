<script setup>
import { ref } from 'vue'
let input = ref('')
function captureCopy(e) {
    console.log(e)
}
</script>

<template>
    <!-- 这个事件会阻止复制操作，ctrl+c不会复制这个input中的内容 -->
    <input @keydown.ctrl.c.exact.prevent="captureCopy" v-model="input" />
    <!-- 如果是mac电脑需要改为meta，因为mac的复制是Command+c -->
    <!-- https://www.doubao.com/thread/wd313af528ed37af6 -->
    <!-- <input @keydown.meta.c.exact.prevent="captureCopy" v-model="input" /> -->
</template>