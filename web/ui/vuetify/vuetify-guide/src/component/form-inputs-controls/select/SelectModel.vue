<template>

     <!-- items是对象，然后item-title就是告诉组件，使用对象的哪个属性作为项目的标题 -->
    <v-select v-model="chosenItem" model-value="chosenItem"  item-title="value" :items="items" label="Compact"></v-select>

    <!-- 和上面的一样，相当于将:model-value="chosenItem" @update:modelValue="(e)=>chosenItem=e"用v-model替代了 -->
    <!-- <v-select :model-value="chosenItem" @update:modelValue="(e)=>chosenItem=e"  item-title="value" :items="items" label="Compact"></v-select> -->

    <v-btn @click="()=>console.log(chosenItem)" >show chosenItem</v-btn>
    
    <!-- 如何清空所选项-来自：https://www.qianwen.com/share/chat/11173046346247d3b8227734f810c459 -->
    <v-btn @click="()=>chosenItem = null" >clear</v-btn>
</template>
<script setup>
import {ref} from 'vue'
const items = [{name:'Foo',value:'aaa'}, {name:'Foo1',value:'aaa1'}]

const chosenItem = ref(null)
</script>