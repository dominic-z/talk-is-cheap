<!-- 展示如何修改节点的样式 -->
<script setup>
import { VueFlow } from '@vue-flow/core';
import { ref } from 'vue';


const nodes = ref([
    {
        id: '1',
        position: { x: 200, y: 100 },
        data: { label: 'Node 1' },
        class: 'vue-flow__node-my-custom'
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


<!--  我发现加上scoped就会失效，应该是vue改造了style中的class，并且vueflow无法识别改造后的style中的class -->
<style>

/* Use a purple theme for our custom node */
.vue-flow__node-my-custom {
    background: darkorange;
    color: white;
    border: 1px solid purple;
    border-radius: 4px;
    box-shadow: 0 0 0 1px purple;
    padding: 8px;
}

/* 修改了全局的默认的样式, */
/* Global default CSS variable values */
/* :root {
    --vf-node-bg: skyblue;
    --vf-node-text: #222;
    --vf-connection-path: #9e9ead;
    --vf-handle: #555;
} */


/* 这个类将修改所有edge的默认样式，其他全局类见https://vueflow.dev/guide/theming.html#css-class-names */
/* .vue-flow__edge{

} */




/* edge的连接线是svg的,修改svg默认样式需要这个SVG path for edge elements */
/* .vue-flow__edge-path {
    stroke-width: 3px
} */
</style>