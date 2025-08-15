<script setup>
import { ref, onMounted } from 'vue'
import { VueFlow, Panel } from '@vue-flow/core'

const inputValue = ref("")

const nodes = ref([
    {
        id: '1',
        position: { x: 50, y: 50 },
        data: { label: 'Node 1', },
    }
]);

function addNode() {
    const id = Date.now().toString()

    nodes.value.push({
        id,
        position: { x: 150, y: 50 },
        data: { label: `Node ${id}`, },
    })
}

function updateNode() {
    nodes.value[0].data.label = `Node ${inputValue.value}`;
}
</script>

<template>
    <div>
        <input type="text" v-model="inputValue"></input>
        <button type="button" @click="updateNode">updateNode</button>
    </div>
    <VueFlow :nodes="nodes">
        <Panel>
            <button type="button" @click="addNode">Add a node</button>

        </Panel>
    </VueFlow>
</template>

<style lang="css" scoped>
/* import the necessary styles for Vue Flow to work */
@import '@vue-flow/core/dist/style.css';

/* import the default theme, this is optional but generally recommended */
@import '@vue-flow/core/dist/theme-default.css';
</style>