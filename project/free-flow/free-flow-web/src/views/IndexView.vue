<script setup>
import IndexNav from '@/components/nav/IndexNav.vue';
import { RouterView } from 'vue-router';

import ClusterManageIndexNavSlot from '@/components/cluster-manage/ClusterManageIndexNavSlot.vue';
import { namedRoutes } from '@/router';

</script>

<template>

    <VApp id="index">
        <IndexNav>
            <ClusterManageIndexNavSlot v-if="$route.name === namedRoutes.index.clusterNodeList.name">
            </ClusterManageIndexNavSlot>
        </IndexNav>
        <router-view v-slot="{ Component }">
            <!-- 只有当路由的 meta.keepAlive 为 true 时，才缓存该组件，而且确保这个keep-alive一直存在，最多缓存5个 -->
            <keep-alive max="5">
                <component :is="Component" v-if="$route.meta.keepAlive"></component>
            </keep-alive>
            <!-- 不需要缓存的组件 -->
            <component :is="Component" v-if="!$route.meta.keepAlive"></component>
        </router-view>
    </VApp>
</template>