<!-- Form.vue -->
<script setup>
import { ref } from 'vue'

const inputValue = ref('')
const emit = defineEmits(['focus-lost'])

const handleBlur = (event) => {
  if (event.relatedTarget && event.relatedTarget.tagName === 'BUTTON') {
    emit('focus-lost')
  }
}
</script>

<template>
  <form>
    <input type="text" v-model="inputValue" @blur="handleBlur" />
    <button>Submit</button>
  </form>
</template>