<script setup>
import BlogPost from './BlogPost.vue';
import { ref } from 'vue';
import ButtonCounter from './ButtonCounter.vue';

const posts = ref([
  { id: 1, title: 'My journey with Vue', obj: { text: "obj1" }, someName: '1' },
  { id: 2, title: 'Blogging with Vue', obj: { text: "obj2" }, someName: '2' },
  { id: 3, title: 'Why Vue is so fun', obj: { text: "obj2" }, someName: '2' }
])

let fontSize = ref(2)

let slot = ref("slot in blog")
function updateSlot(){
  slot.value += '1'
}

</script>

<template>

  <h1>Here is a child component!</h1>
  <button-counter /> <!-- 如果是在 DOM 中书写该模板可以使用kebab-case，如果是在vue中，还是pascalcase -->
  <ButtonCounter />
  <ButtonCounter />

  <hr />

  <div :style="{ fontSize: fontSize + 'em' }">
    <BlogPost v-for="p in posts" :key="p.id" :title="p.title" :obj="p.obj" :someName="p.someName"
      @enlarge-text="() => { console.log('emit'); fontSize = fontSize + 1; }">

      <div :style="{'border': '1px solid'}">
        {{ slot }}
        <button @click="updateSlot">updateSlot</button>
      </div>

    </BlogPost>
  </div>
  <div>{{ posts }}</div>

  <hr />

</template>