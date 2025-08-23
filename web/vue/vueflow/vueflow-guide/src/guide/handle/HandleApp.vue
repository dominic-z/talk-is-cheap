<!-- handle就是node上用来连线的点 -->
<script setup>
import { ref, onMounted, markRaw } from 'vue'
import { VueFlow, useVueFlow, Panel ,ConnectionMode} from '@vue-flow/core'
import CustomSimpleNode from './CustomSimpleNode.vue'
import AdjustHandlePositionNode from './AdjustHandlePositionNode.vue'
import MultiHandleNode from './MultiHandleNode.vue'
import HiddenHandleNode from './HiddenHandleNode.vue'
import LimitConnectionNode from './LimitConnectionNode.vue'

const nodeTypes = {
    customSimpleNode: markRaw(CustomSimpleNode),
    adjustHandlePositionNode: markRaw(AdjustHandlePositionNode),
    multiHandleNode: markRaw(MultiHandleNode),
    hiddenHandleNode: markRaw(HiddenHandleNode),
    hiddenHandleNode: markRaw(HiddenHandleNode),
    limitConnectionNode: markRaw(LimitConnectionNode),
}


// 这个事件没有解释，其实就是在使用鼠标连接两个handle的时候，这个事件就会触发
const instance = useVueFlow()
instance.onConnect(({ source, target, sourceHandle, targetHandle }) => {

    console.log('sourcesource', source)
    console.log('target', target)
    // these are the handle ids of the source and target node
    // if no id is specified these will be `null`, meaning the first handle of the necessary type will be used
    console.log('sourceHandle', sourceHandle)
    console.log('targetHandle', targetHandle)
})
const nodes = ref([
    {
        id: '1',
        position: { x: 50, y: 50 },
        data: { label: '1-customSimpleNode', },
        type: 'customSimpleNode'
    },
    {
        id: '2',
        position: { x: 50, y: 350 },
        data: { label: '2-adjustHandlePositionNode', },
        type: 'adjustHandlePositionNode'
    },
    {
        id: '3',
        position: { x: 350, y: 150 },
        data: { label: '3-multiHandleNode', },
        type: 'multiHandleNode'
    },
    {
        id: '4',
        position: { x: 450, y: 450 },
        data: { label: '4-hiddenHandleNode', },
        type: 'hiddenHandleNode'
    },
    {
        id: '5',
        position: { x: 600, y: 250 },
        data: { label: '5-limitConnectionNode', },
        type: 'limitConnectionNode'
    },
    
])

const edges = ref([
    {
        id: 'e1->2',
        source: '1',
        target: '2',
        label: '1->2',
    },
    {
        id: 'e1->3',
        source: '1',
        target: '3',
        label: '1->3',
        targetHandle: 'target-a',
    },
    {
        id: 'e2->3',
        source: '2',
        target: '3',
        targetHandle: 'target-b',
        label: '2->3',
    },
    {
        id: 'e2->4',
        source: '2',
        target: '4',
        label: '2->4',
    },
    {
        id: 'e3->4',
        source: '3',
        target: '4',
        sourceHandle: 'source-a',
        label: '3->4',
    },
    {
        id: 'e3->5',
        source: '3',
        target: '5',
        sourceHandle: 'source-b',
        label: '3->5',
    },
    {
        id: 'e1->3source',
        source: '1',
        target: '3',
        label: '1->3source',
        targetHandle: 'source-a',
    },
    {
        id: 'e5->3',
        source: '5',
        target: '3',
        label: 'e5->3'
    },
])


function addEdgeFrom5To4() {
    instance.addEdges([
        {
            id: 'e5->4',
            source: '5',
            target: '4',
            label: '5->4',
        }
    ])
}

</script>

<template>

    <div :style="{ height: '800px', width: '800px', border: '1px solid' }">

        <!-- :connection-mode="ConnectionMode.Strict"  将只允许source和target相连-->
         <!-- 将导致e1->3source这个edge直接连在了节点3的脑袋上，没有handle -->
        <VueFlow :nodes="nodes" :edges="edges" :nodeTypes="nodeTypes" >
            <Panel>

                <button @click="addEdgeFrom5To4">addEdgeFrom5To4</button>
            </Panel>
        </VueFlow>

    </div>

</template>