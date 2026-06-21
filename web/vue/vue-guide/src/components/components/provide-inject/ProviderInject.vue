<script setup>
import { provide,ref } from 'vue';
import { myInjectionKey as myInjectionKey,myCCKey } from './key.js';
import Injector from './Injector.vue';

let refA = ref('AA')
let refB = ref('BB')
provide(myInjectionKey,{refA,refB})
let refC = ref('CC')
provide(myCCKey,refC)
</script>

<template>

    <Injector></Injector>
</template>