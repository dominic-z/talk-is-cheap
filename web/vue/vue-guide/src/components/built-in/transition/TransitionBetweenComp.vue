<script setup>
import { shallowRef } from 'vue'
import CompA from './CompA.vue'
import CompB from './CompB.vue'

// use shallowRef to avoid component being deeply observed
const activeComponent = shallowRef(CompA)
</script>

<template>
	<label>
    <input type="radio" v-model="activeComponent" :value="CompA"> A
  </label>
  <label>
    <input type="radio" v-model="activeComponent" :value="CompB"> B
  </label>
  <Transition name="fade" mode="out-in">
    <component :is="activeComponent"></component>
  </Transition>
</template>

<style>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.5s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>