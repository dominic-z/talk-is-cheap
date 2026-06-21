<script setup>
import { inject } from 'vue';
import { myInjectionKey,myCCKey } from './key';

const injected = inject(myInjectionKey)
const cc = inject(myCCKey)


</script>

<template>

    <h1>{{ injected.refA }}  {{ injected.refB }}</h1>
    <div>
        {{ cc }}
    </div>
</template>