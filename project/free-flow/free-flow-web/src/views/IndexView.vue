<script setup>
import IndexNav from '@/components/nav/IndexNav.vue';
import { RouterView } from 'vue-router';

import ClusterManageIndexNavSlot from '@/components/cluster-manage/ClusterManageIndexNavSlot.vue';
import { namedRoutes } from '@/router';

</script>

<template>

    <VApp id="index">
        <IndexNav>
            <ClusterManageIndexNavSlot v-if="$route.name===namedRoutes.index.clusterManage.name"></ClusterManageIndexNavSlot>
        </IndexNav>
        <RouterView >

        </RouterView>
    </VApp>
</template>