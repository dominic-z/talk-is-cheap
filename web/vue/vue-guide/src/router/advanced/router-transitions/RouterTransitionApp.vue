<script setup>
import { Transition } from 'vue';
</script>

<template>

    <RouterLink to="/custom-transition">
        custom-transition
    </RouterLink>

    <RouterLink to="/other-transition">
        other-transition
    </RouterLink>

    <RouterView v-slot="{ Component, route }">
        <Transition name="fade">
            <!-- Vue 可能会自动复用看起来相似的组件，从而忽略了任何过渡。幸运的是，可以添加一个 key 属性来强制过渡。这也允许你在相同路由上使用不同的参数触发过渡： -->
            <!-- 相当于告诉vue，这tmd就是俩组件，给我复用 -->
            <component :is="Component" :key="route.fullPath" />
        </Transition>
    </RouterView>
</template>

<style>
.fade-enter-active {
    transition: opacity 0.5s ease;
}

.fade-leave-active {
    transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
    opacity: 0;
}
</style>