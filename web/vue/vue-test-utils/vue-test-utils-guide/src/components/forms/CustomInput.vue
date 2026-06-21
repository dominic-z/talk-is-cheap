<!-- CustomInput.vue -->
<script setup>
const {modelValue,label} = defineProps(['modelValue', 'label'])
const emit = defineEmits(['update:modelValue'])

function input(event) {
    emit('update:modelValue', event.target.value)
    console.log("event.target.value",event.target.value)
    console.log("props.modelValue",modelValue)
}
</script>

<template>
    <label>
        {{ label }}
        <input type="text" :value="modelValue" @input="input" />
    </label>
</template>