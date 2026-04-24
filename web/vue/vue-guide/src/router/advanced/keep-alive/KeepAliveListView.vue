<script setup>
import { ref, shallowRef,computed } from 'vue'

import List from './List.vue';
import { useRouter } from 'vue-router';

const router = useRouter()

const routeOption = ref( {... router.options.routes.find(r => r.name === 'KeepAliveListView').meta} )

function changeMeta() {
    const routeConfig = router.options.routes.find(r => r.name === 'KeepAliveListView')
    if (routeConfig) {
        console.log(routeConfig.meta)
        routeConfig.meta.isKeepAlive = !routeConfig.meta.isKeepAlive // 埋下伏笔：告诉列表页你要刷新
        routeOption.value = {... routeConfig.meta} // 需要解构
    }
}
</script>

<template>
    <List></List>

    <button @click="changeMeta">changeMeta</button>
    <div>{{ routeOption }}</div>
</template>