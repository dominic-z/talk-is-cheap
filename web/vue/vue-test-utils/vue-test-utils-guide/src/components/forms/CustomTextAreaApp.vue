<script setup>
import CustomTextArea from '@/components/forms/CustomTextArea.vue';

import { ref, defineEmits } from 'vue'


const emit = defineEmits(['submitted'])
const description = ref('initial');
const customTextAreaRef = ref(null)

const handleSubmit = () => {
   console.log("description.value", description.value)
   // console.log("customTextAreaRef.value", customTextAreaRef.value)
   emit('submitted', description.value)
}
</script>
<template>
   <form @submit.prevent="handleSubmit">
      <!-- 这里用到了vue的模板引用知识 -->
       <!-- 手动创造了一个vue组件 -->
      <CustomTextArea v-model="description" ref="customTextAreaRef" />
      <button type="submit">Send</button>
   </form>
</template>