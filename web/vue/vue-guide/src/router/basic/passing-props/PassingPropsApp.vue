<script setup>
// 测试出来的，直接在RouterView头上挂props也是可以的

import {ref} from 'vue'

const userProp = ref('userProp')

</script>

<template>
<!-- 看router对象然后浏览器直接访问路由 -->

    <RouterView :userProp="userProp"/>
</template>