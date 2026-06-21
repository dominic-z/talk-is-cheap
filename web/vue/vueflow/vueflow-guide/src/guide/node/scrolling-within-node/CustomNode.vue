<script setup>
import { ref } from 'vue'

const listItems = ref(Array.from({ length: 100 }, (_, i) => i))  
</script>

<template>
  <div class="custom-node-container">
    <ul class="nowheel scroll">
      <li v-for="item in listItems" :key="item">Item {{ item }}</li>
    </ul>
  </div>
</template>

<style>
.scroll{
  background-color: orange;
  height: 100px;
  width: 200px;
  /* scroll */

  overflow-y: auto;
}
</style>