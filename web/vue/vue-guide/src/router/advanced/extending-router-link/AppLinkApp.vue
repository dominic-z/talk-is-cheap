<script setup>
import AppLink from './AppLink.vue';
</script>
<template>

    <AppLink to="https://www.baidu.com">AppLink:https://www.baidu.com</AppLink>
    <AppLink to="/user/aaa">AppLink:/user/aaa</AppLink>
    <RouterLink to="https://www.baidu.com">RouterLink:https://www.baidu.com</RouterLink>
    <RouterLink to="/user/aaabbb">RouterLink:/user/aaa</RouterLink>

    <RouterView v-slot="{ Component }">

        <div :style="{ backgroundColor: 'gray' }">
            <component :is="Component" />
        </div>
    </RouterView>
</template>