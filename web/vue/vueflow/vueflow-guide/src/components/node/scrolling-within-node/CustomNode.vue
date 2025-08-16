<script setup>
import { ref } from 'vue'

const listItems = ref(Array.from({ length: 100 }, (_, i) => i))  
</script>

<template>
  <div class="custom-node-container">
    <ul class="my-nowheel">
      <li v-for="item in listItems" :key="item">Item {{ item }}</li>
    </ul>
  </div>
</template>

