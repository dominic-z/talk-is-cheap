<!-- FormComponent.vue -->
<script setup>
import { ref } from 'vue'

const form = ref({
  email: '',
  description: '',
  city: '',
  subscribe: false,
  interval: ''
})
const emit = defineEmits(['submit'])

const submit = () => {
  emit('submit', { ...form.value })
}
</script>


<template>
  <form @submit.prevent="submit">
    <input type="email" v-model="form.email" />

    <textarea v-model="form.description" />

    <select v-model="form.city">
      <option value="new-york">New York</option>
      <option value="moscow">Moscow</option>
    </select>

    <input type="checkbox" v-model="form.subscribe" />

    <input type="radio" value="weekly" v-model="form.interval" />
    <input type="radio" value="monthly" v-model="form.interval" />

    <button type="submit">Submit</button>
  </form>
</template>