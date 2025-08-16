<script setup>
import { ref } from 'vue'

const inputValue = ref('')
</script>

<template>
  <div class="custom-node-container">
    <input class="nodrag" v-model="inputValue" />
  </div>
</template>