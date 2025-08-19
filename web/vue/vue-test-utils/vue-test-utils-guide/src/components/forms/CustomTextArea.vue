<!-- CustomTextarea.vue -->
<script setup>
import { ref } from 'vue'

const { value } = defineProps(['value'])
const description = ref(value)


</script>

<template>
    <textarea v-model="description" />
</template>