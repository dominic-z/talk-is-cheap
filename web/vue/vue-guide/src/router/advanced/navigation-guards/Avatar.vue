<script setup>
import { onBeforeRouteLeave, onBeforeRouteUpdate } from 'vue-router'
import { useRoute } from 'vue-router';
import { watch } from 'vue';


</script>

<template>
    <div>
        Avatar: {{ $route.fullPath }}
    </div>
</template>