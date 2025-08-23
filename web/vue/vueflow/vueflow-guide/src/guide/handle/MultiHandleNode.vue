<script setup>

// 这个节点的handle有多个
import { Position, Handle } from '@vue-flow/core'

// props were passed from the slot using `v-bind="customNodeProps"`
const props = defineProps(['data'])

</script>

<template>
    <div :style="{ border: '1px solid', padding: '10px' }">
        <div>{{ props.data.label }}</div>
        <!-- each of these handles needs a unique id since we're using two `source` type handles -->
        <Handle id="source-a" type="source" :position="Position.Right" style="top: 10px"/>
        <Handle id="source-b" type="source" :position="Position.Right" style="bottom: 10px; top: auto;"/>

        <!-- each of these handles needs a unique id since we're using two `target` type handles -->
        <Handle id="target-a" type="target" :position="Position.Bottom" style="left: 10px"/>
        <Handle id="target-b" type="target" :position="Position.Left" />
    </div>
</template>