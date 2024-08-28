<script setup>
import BlogPost from './BlogPost.vue';
import { ref } from 'vue';


const posts = ref([
  { id: 1, title: 'My journey with Vue' ,obj:{text:"obj1"},someName:'1'},
  { id: 2, title: 'Blogging with Vue' ,obj:{text:"obj2"},someName:'2'},
  { id: 3, title: 'Why Vue is so fun' ,obj:{text:"obj2"},someName:'2'}
])

let fontSize= ref(1)
</script>

<template>
    <div :style="{fontSize:fontSize+'em'}">
        <BlogPost v-for="p in posts" :key="p.id" :title="p.title" :obj="p.obj" :someName="p.someName"
         @enlarge-text="()=>{console.log('emit');fontSize=fontSize+1;}"/>
    </div>
    <div>{{ posts }}</div>
</template>