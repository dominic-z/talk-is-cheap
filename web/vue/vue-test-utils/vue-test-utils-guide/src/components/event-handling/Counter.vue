<!-- Counter.vue -->
<script setup>
import { ref } from 'vue'

const count = ref(0)

const emit = defineEmits(['increment','complexIncrement'])

const handleClick = () => {
  count.value += 1
  emit('increment', count.value)
}


const complexHandleClick = () => {
  count.value += 1
  emit('complexIncrement', {
    count: count.value,
    isEven: count.value % 2 === 0,
  })
}
</script>

<template>
  <button @click="handleClick">Increment</button>
  <button id="complexEventButton" @click="complexHandleClick">complexIncrement</button>
</template>