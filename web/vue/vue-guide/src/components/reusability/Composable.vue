<script setup>
import { reactive } from 'vue';
import { useMouse } from './Mouse.js'

const r = reactive(useMouse())
</script>

<template>Mouse position is at: {{ r.x }}, {{ r.y }}</template>