<script setup>
// 以下是自己摸索的结果，一个最简单的图
import { VueFlow } from '@vue-flow/core';
import { ref } from 'vue';


const nodes = ref([
    {
        id: '1',
        position: { x: 200, y: 100 },
        data: { label: 'Node 1' },
    },
    {
        id: '2',
        position: { x: 50, y: 200 },
        data: { label: 'Node 2' },
    },
    {
        id: '3',
        position: { x: 350, y: 200 },
        data: { label: 'Node 3' },
    }
])

const edges = ref([
    {
        id: 'e1->2',
        source: '1',
        target: '2',
    },

    {
        id: 'e1->3',
        source: '1',
        target: '3',
        animated: true,
    }
])

</script>

<template>

    <div :style="{height: '500px',width:'1000px'}">
        <VueFlow :nodes="nodes" :edges="edges"></VueFlow>
    </div>

</template>


<style>
/* import the necessary styles for Vue Flow to work */
@import '@vue-flow/core/dist/style.css';

/* import the default theme, this is optional but generally recommended */
@import '@vue-flow/core/dist/theme-default.css';
</style>