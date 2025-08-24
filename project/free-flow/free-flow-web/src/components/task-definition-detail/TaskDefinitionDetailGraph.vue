<script setup>
import { VueFlow } from '@vue-flow/core';
import { ref } from 'vue';
import { MarkerType, } from '@vue-flow/core'
import { mdiWindowClose } from '@mdi/js'
import { Background } from '@vue-flow/background';

const nodes = ref([
    {
        id: '1',
        position: { x: 200, y: 100 },
        class: 'node-initial',
        style: {
            'box-shadow':'0 4px 6px rgba(0, 0, 0, 0.1)'
        },
        data: { label: 'Node 1' },
    },
    {
        id: '2',
        position: { x: 50, y: 200 },
        class: 'node-initial',
        style: {
            'box-shadow':'0 4px 6px rgba(0, 0, 0, 0.1)'
        },
        data: { label: 'myNode 2' },
    },
    {
        id: '3',
        position: { x: 350, y: 200 },
        class: 'node-initial',
        style: {
            'box-shadow':'0 4px 6px rgba(0, 0, 0, 0.1)'
        },
        data: { label: 'Node 3' },
    }
])

const edges = ref([
    {
        id: 'e1->2',
        source: '1',
        target: '2',
        style: {
            strokeWidth: 2
        },
        class: 'normal-edge',
        markerEnd: {
            type: MarkerType.ArrowClosed,
            color: 'black',
            strokeWidth: 3,
        },
    },

    {
        id: 'e1->3',
        source: '1',
        target: '3',
        animated: true,
    }
])

// Node click event handler
function onNodeClick({ event, node }) {
    console.log('Node clicked:', node, event);
    if (!draw.value) {
        draw.value = !draw.value;
    }

}


// 手动通过js来修改样式，我实在不想重写一个CustomNode，但是Node的API好像又没有开放hover的样式
function onNodeMouseEnter(event) {
    for (let i = 0; i < nodes.value.length; i++) {
        let n = nodes.value[i]

        if (n.id === event.node.id) {

            n.style['box-shadow'] = '0 8px 12px rgba(0, 0, 0, 0.2)'
        }
    }
}

function onNodeMouseLeave(event) {
    for (let i = 0; i < nodes.value.length; i++) {
        let n = nodes.value[i]

        if (n.id === event.node.id) {
            console.log(n)
            n.style['box-shadow']='0 4px 6px rgba(0, 0, 0, 0.1)'
        }
    }
}

const draw = ref(false)

</script>

<template>
    <v-main>
        <v-sheet class="mx-auto pa-2 pt-6" color="grey-lighten-4">
            <div :style="{ height: '100vh', width: '100wh' }">
                <VueFlow :nodes="nodes" :edges="edges" @node-click="onNodeClick" @node-mouse-enter="onNodeMouseEnter"
                    @node-mouse-leave="onNodeMouseLeave">
                    <Background></Background>

                </VueFlow>


            </div>

        </v-sheet>


        <v-navigation-drawer v-model="draw" :location="'right'">
            <v-toolbar density="compact">

                <v-btn :icon="mdiWindowClose" @click="draw = !draw"></v-btn>
            </v-toolbar>
            <div class="d-flex mt-2 ml-2 mr-2 ga-2">
                <v-text-field label="taskVersion"></v-text-field>
                <!-- <v-btn :icon="mdiMagnify" variant="plain"></v-btn> -->
            </div>
        </v-navigation-drawer>


    </v-main>

</template>

<style>
/* 初始状态的阴影 */
.node-initial {
    font-size: 20px;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
    /* 初始阴影：水平偏移 0，垂直偏移 4px，模糊 6px */
    transition: box-shadow 0.3s ease;
    /* 平滑过渡效果 */
}

/* 修改handle的大小 */
.vue-flow__handle{
    width: 10px;
    height: 10px;
}

</style>