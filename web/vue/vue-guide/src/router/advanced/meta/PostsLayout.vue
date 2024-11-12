<script setup>
import { useRoute } from 'vue-router';

const route = useRoute()
console.log(route.matched)

</script>
<template>
    <div>
        PostsLayout: []
    </div>
    <div>
        <RouterView />
    </div>
</template>