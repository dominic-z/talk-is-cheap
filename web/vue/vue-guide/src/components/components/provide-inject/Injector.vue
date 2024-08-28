<script setup>
import { inject } from 'vue';
import { myInjection } from './key';

const injected = inject(myInjection)

</script>

<template>

    <h1>{{ injected.refA }}  {{ injected.refB }}</h1>
</template>