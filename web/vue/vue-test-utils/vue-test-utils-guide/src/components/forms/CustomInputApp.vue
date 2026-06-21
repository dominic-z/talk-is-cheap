<script setup>
import CustomInput from '@/components/forms/CustomInput.vue';


import {ref} from 'vue'

const modelValue = ref('initial');
</script>
<template>

    <CustomInput v-model="modelValue" label="Text Input" class="text-input"></CustomInput>
</template>