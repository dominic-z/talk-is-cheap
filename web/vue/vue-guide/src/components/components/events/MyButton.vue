<script setup>
const emit = defineEmits(['increaseBy', 'increaseBy2'])


function increaseBy2() {
  emit('increaseBy2',2)
}



</script>


<template>

<button @click="$emit('increaseBy', 1)">
  Increase by 1
</button>


<button @click="increaseBy2">
  Increase by 2
</button>
</template>
