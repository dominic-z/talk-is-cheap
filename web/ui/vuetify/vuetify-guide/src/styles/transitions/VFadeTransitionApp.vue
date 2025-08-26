<script setup>

import { mdiCheckCircle } from '@mdi/js';
import {ref} from 'vue'

const show = ref(false)

</script>

<template>

        <div>
            <VBtn @click="show=!show">aaa</VBtn>
            <!-- 这个leave-absolute,就是给leave的class加个绝对定位,用来将这个对象抽离标准文档流Absolutely positions the leaving element -->
             <!-- 参考https://cn.vuejs.org/guide/built-ins/transition-group.html#move-transitions -->
            <VFadeTransition leave-absolute="true">
                <v-icon v-if="show" :icon="mdiCheckCircle" :style="{ 'color': 'green' }" />
                <v-icon v-else :icon="mdiCheckCircle" :style="{ 'color': 'red' }"/>
            </VFadeTransition>
        </div>
</template>