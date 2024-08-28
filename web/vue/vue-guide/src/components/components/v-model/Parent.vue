<script setup>

import Child from './Child.vue';
</script>
<template>
<!-- Parent.vue -->
<Child
  :modelValue="foo"
  @update:modelValue="$event => {(foo = $event);console.log($event)}"
/>
</template>