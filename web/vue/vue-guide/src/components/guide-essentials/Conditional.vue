<script setup>
import { ref } from 'vue';
let awesome = ref(false)
let ok = ref(true)
let templateBool = ref(true)


function reverse() {
    awesome.value = !awesome.value
    ok.value = !ok.value
    templateBool.value = !templateBool.value
}
</script>

<template>

    <button @click="reverse">Toggle</button>

    <h1 v-if="awesome">Vue is awesome!</h1>
    <h1 v-else>Oh no 😢</h1>

    <h1 v-show="ok">Hello!</h1> 


    <!-- 管理多个标签 -->
    <template v-if="templateBool">
        <div v-for="n in 2">
            {{ n }}
        </div>
    </template>
</template>
 