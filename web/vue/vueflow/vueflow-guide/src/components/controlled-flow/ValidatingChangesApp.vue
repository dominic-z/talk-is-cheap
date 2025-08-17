<script setup>
import { ref } from 'vue'
import { Panel, useVueFlow, VueFlow } from '@vue-flow/core'

const { applyNodeChanges,removeNodes } = useVueFlow();

// 自己写一个confirm，我没有写，用的浏览器内置的
// const { confirm } = useConfirm();

const nodes = ref([
    {
        id: '1',
        position: { x: 0, y: 100 },
        data: { label: 'Node 1' },
    },
    {
        id: '2',
        position: { x: 100, y: 200 },
        data: { label: 'Node 2' },
    },
])

const edges = ref([
    {
        id: 'e1->2',
        source: '1',
        target: '2',
    },
])

const onNodesChange = async (changes) => {
    const nextChanges = []

    for (const change of changes) {
        if (change.type === 'remove') {
            const isConfirmed = confirm('Are you sure you want to delete this node?')
            console.log(isConfirmed)

            if (isConfirmed) {
                nextChanges.push(change)
            }
        } else {
            nextChanges.push(change)
        }
    }

    applyNodeChanges(nextChanges)
}

function removeNode(){
    removeNodes([{
        id: '2'
    }])
}
</script>

<template>
    <div :style="{ height: '900px', width: '900px', border: '1px solid' }">
        <VueFlow :nodes="nodes" :edges="edges" :apply-default="false" @nodes-change="onNodesChange">
            <Panel>
                <button @click="removeNode">removeNode</button>
            </Panel>

        </VueFlow>
    </div>
</template>