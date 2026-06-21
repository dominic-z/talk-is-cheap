<script setup>
import { mdiClock,mdiAccount,mdiFlag } from '@mdi/js';
import { ref } from 'vue';

const items = [
    { text: 'Real-Time', icon: mdiClock,active:ref(false) },
    { text: 'Audience', icon: mdiAccount,active:ref(false) },
    { text: 'Conversions', icon: mdiFlag,active:ref(false) },
]
const selected = ref(null)

function onClick(e){
    console.log(e)
}


</script>
<template>
    <v-card class="mx-auto pa-2" max-width="300">
        <v-list v-model:selected="selected">
            <v-list-subheader>REPORTS</v-list-subheader>

            <!-- :value的作用就是在选中某个v-list-item之后，对应的对象会被丢给v-list的v-model:selected指定的对象 -->
            <v-list-item v-for="(item, i) in items" :key="i" :value="item" color="primary" rounded="shaped" @click="onClick" >
                <template v-slot:prepend>
                    <v-icon :icon="item.icon"></v-icon>
                </template>

                <v-list-item-title v-text="item.text"></v-list-item-title>
            </v-list-item>
        </v-list>
    </v-card>
</template>