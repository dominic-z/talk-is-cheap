<script setup>
import { VueFlow } from '@vue-flow/core';
import { ref } from 'vue';


const nodes = ref([
    {
        id: '1',
        position: { x: 200, y: 100 },
        data: { label: 'Node 1' },
        class: 'vue-flow__node-custom'
    },
    {
        id: '2',
        position: { x: 50, y: 200 },
        data: { label: 'Node 2' },
        // You can pass an object containing CSSProperties or CSS variables
        style: { backgroundColor: 'green', width: '200px', height: '100px' },
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

    <div :style="{ height: '500px', width: '1000px' }">
        <VueFlow :nodes="nodes" :edges="edges"></VueFlow>
    </div>

</template>


<style>
/* import the necessary styles for Vue Flow to work */
@import '@vue-flow/core/dist/style.css';

/* import the default theme, this is optional but generally recommended */
@import '@vue-flow/core/dist/theme-default.css';


/* 定制化一个node */
/* Use a purple theme for our custom node */
.vue-flow__node-custom {
    background: purple;
    color: white;
    border: 1px solid purple;
    border-radius: 4px;
    box-shadow: 0 0 0 1px purple;
    padding: 8px;
}

/* 一些默认的样式 */
/* Global default CSS variable values */
:root {
    --vf-node-bg: #b0c24a;
    --vf-node-text: #222;
    --vf-connection-path: #9e9ead;
    --vf-handle: #555;
}
</style>