<script setup>
import { ref, shallowRef } from 'vue'

import List from './List.vue';
const currentView = shallowRef(List)
</script>

<template>
    <!-- <component :is="currentView"/> -->
    <keep-alive>
        <component :is="currentView"/>
    </keep-alive>

</template>