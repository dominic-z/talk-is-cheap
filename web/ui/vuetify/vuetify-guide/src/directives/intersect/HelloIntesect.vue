<script setup>

// https://developer.mozilla.org/zh-CN/docs/Web/API/Intersection_Observer_API

import { ref } from 'vue'

const intersecting = ref(false)
function onIntersect(isIntersecting, entries, observer) {
    console.log("isIntersecting", isIntersecting)
    console.log("entries", entries)
    console.log("observer", observer)
    intersecting.value = entries[0].intersectionRatio >= 0.5
}

const intersector = {
    handler: onIntersect,
    options: {
        threshold: [0, 0.5, 1.0], // 代表当这个元素出现0、50%、100%的时候会触发一次对应的回调方法
    }
}
</script>
<template>
    <div>
        <div class="d-flex align-center text-center justify-center">
            <v-avatar :color="intersecting ? 'green-lighten-1' : 'red-darken-2'" class="me-3 swing-transition" size="32"
                variant="flat"></v-avatar>
        </div>

        <v-sheet class="overflow-y-auto" max-height="400">

            <v-sheet class="d-flex align-center text-center pa-2" height="200vh">
                <v-card class="mx-auto" max-width="336" v-intersect="intersector">
                    <v-card-title>Card title</v-card-title>

                    <v-card-text>
                        Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do
                        eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim
                        ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut
                        aliquip ex ea commodo consequat. Duis aute irure dolor in
                        reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla
                        pariatur. Excepteur sint occaecat cupidatat non proident, sunt in
                        culpa qui officia deserunt mollit anim id est laborum.
                    </v-card-text>
                </v-card>
            </v-sheet>
        </v-sheet>
    </div>
</template>
