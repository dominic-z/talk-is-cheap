<script setup>
// 当展示ClusterManage的时候，送给IndexNav的插槽，目的是希望根据route的不同，能够对indexNav做一些定制化。
import { useClusterManageStore } from '@/stores/clusterManageStore';
import { mdiMagnify } from '@mdi/js'

const clusterManageStore = useClusterManageStore()

</script>
<template>

    <v-btn :icon="mdiMagnify" @click="clusterManageStore.changeExpand">
    </v-btn>
</template>
