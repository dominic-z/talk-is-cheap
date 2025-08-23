<script setup>
import { namedRouter } from '@/router';
import { computed, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
const route = useRoute()
const router = useRouter()
// const display = useDisplay()


const menus = ref([
  {
    id: '1',
    title: '节点查询',
    routePath: namedRouter.clusterManage.path
  },
  {
    id: '2',
    title: '任务定义查询',
    routePath: namedRouter.taskDefinitionManage.path
    
  },
  {
    id: '3',
    title: '任务执行查询',
    routePath: namedRouter.taskStartupManage.path
  },
])


const title = computed(()=>{
  for(let menu of menus.value){
    if(menu.routePath === route.path){
      return menu.title
    }
  }
})

function pushRoute(path){
  console.log(path)
  router.replace(path)
}

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
      <v-list-item class="border-b-thin" v-for="(m, index) in menus" :index="index" :key="m.id" :title="m.routePath"
        link @click="()=>pushRoute(m.routePath)"></v-list-item>
    </v-list>
  </v-navigation-drawer>



</template>