<script setup>
import { provide,ref } from 'vue';
import { myInjection } from './key';
import Injector from './Injector.vue';

let refA = ref('AA')
let refB = ref('BB')
provide(myInjection,{refA,refB})
</script>

<template>

    <Injector></Injector>
</template>