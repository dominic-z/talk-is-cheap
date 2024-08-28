<script setup>
import { ref } from 'vue';
let awesome = ref(false)
let ok = ref(true)


function reverse() {
    awesome.value = !awesome.value
    ok.value = !ok.value
}
</script>

<template>

    <button @click="reverse">Toggle</button>

    <h1 v-if="awesome">Vue is awesome!</h1>
    <h1 v-else>Oh no 😢</h1>

    <h1 v-show="ok">Hello!</h1>
</template>