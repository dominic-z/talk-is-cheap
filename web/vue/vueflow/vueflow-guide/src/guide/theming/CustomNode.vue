<script setup>
import { Handle, Position } from '@vue-flow/core';


const props = defineProps(['data'])

console.log(props)
</script>

<template>
    <div class="runnableStage">

        <Handle type="target" :position="Position.Top" :style="{width:'10px',height:'10px'}"></Handle>
        <div>{{ props.data.label }}</div>
        <Handle type="source" :position="Position.Bottom" :style="{width:'10px',height:'10px'}"></Handle>
    </div>

</template>

<style >

.vue-flow__node-runnableStage{
    background-color: aqua;
    margin: 10px;
}
</style>