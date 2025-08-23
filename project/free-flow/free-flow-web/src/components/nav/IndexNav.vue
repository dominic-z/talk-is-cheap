<script setup>
import { namedRoutes } from '@/router';
import { computed, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
const route = useRoute()
const router = useRouter()
// const display = useDisplay()


const menus = ref([
  {
    id: '1',
    title: '节点查询',
    routeName: namedRoutes.index.clusterManage.name
  },
  {
    id: '2',
    title: '任务定义查询',
    routeName: namedRoutes.index.taskDefinitionManage.name
    
  },
  {
    id: '3',
    title: '任务执行查询',
    routeName: namedRoutes.index.taskStartupManage.name
  },
])


const title = computed(()=>{
  for(let menu of menus.value){
    if(menu.routeName === route.name){
      return menu.title
    }
  }
})

const drawer = ref(false)

</script>

<template>

  <!-- 这个组件必须得在v-app之中 -->
  <v-app-bar>
    <v-app-bar-nav-icon variant="text" @click.stop="drawer = !drawer"></v-app-bar-nav-icon>

    <v-app-bar-title>{{ title }}</v-app-bar-title>

    <slot></slot>
  </v-app-bar>

  <v-navigation-drawer v-model="drawer" :location="$vuetify.display.mobile ? 'bottom' : 'left'" temporary>
    <v-list>
      <v-list-item class="border-b-thin" v-for="(m, index) in menus" :index="index" :key="m.id" :title="m.title"
        link @click="$router.push({name:m.routeName})"></v-list-item>
    </v-list>
  </v-navigation-drawer>



</template>