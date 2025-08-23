<!-- CustomNode.vue -->
<script setup>
import { useNode } from '@vue-flow/core'
const props = defineProps({
    position: {
        type: Object,
        required: true,
    },
    data: {
        type: Object,
        required: true,
    }
})

// `useNode` returns us the node object straight from the state
// since the node obj is reactive, we can mutate it to update our nodes' data
const { node } = useNode()

function onSomeEvent() {
    // 点击会触发更新data
    node.data = {
        ...node.data,
        hello: 'world',
    }

    // you can also mutate properties like `selectable` or `draggable`
    node.selectable = false
    node.draggable = false
}
</script>

<template>
    <div @click="onSomeEvent">
        <div>{{ props.data.label }}/{{ props.data.hello }}</div>
    </div>
</template>