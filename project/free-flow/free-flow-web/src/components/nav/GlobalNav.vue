<script setup>
import { ref, watch, toValue } from 'vue'
import { useRoute } from 'vue-router';
import { useDisplay } from 'vuetify';
import { mdiDotsVertical } from '@mdi/js';
const route = useRoute()

const display = useDisplay()
// console.log(toValue(display.mobile))

const menus = ref([
  {
    id: '1',
    title: '节点查询'
  },
  {
    id: '2',
    title: '任务定义查询'
  },
  {
    id: '3',
    title: '任务执行查询'
  },
])



const drawer = ref(false)

</script>

<template>

  <!-- 这个组件必须得在v-app之中 -->
  <v-app-bar>
    <v-app-bar-nav-icon variant="text" @click.stop="drawer = !drawer"></v-app-bar-nav-icon>

    <v-app-bar-title>Application</v-app-bar-title>

    <slot></slot>
  </v-app-bar>

  <v-navigation-drawer v-model="drawer" :location="$vuetify.display.mobile ? 'bottom' : 'left'" temporary>
    <v-list>
      <v-list-item class="border-b-thin" v-for="(m, index) in menus" :index="index" :key="m.id" :title="m.title"
        link></v-list-item>
    </v-list>
  </v-navigation-drawer>



</template>