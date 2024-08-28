<!-- Child.vue -->
<script setup>
const props = defineProps(['modelValue'])
const emit = defineEmits(['update:modelValue'])
</script>

<template>
    <!-- $event必须有$，似乎是原生事件就得家$ -->
  <input
    :value="modelValue"
    @input="emit('update:modelValue', $event.target.value)"
  />
</template>