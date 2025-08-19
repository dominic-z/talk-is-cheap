<!-- Form.vue -->
 <!--  -->
<script setup>
import { ref } from 'vue'

const email = ref('')
const emit = defineEmits(['submit'])

const submit = () => {
  emit('submit', email.value)
}
</script>

<template>
  <div>
    <input type="email" v-model="email" />

    <button @click="submit">Submit</button>
  </div>
</template>