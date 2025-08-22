<!-- 通过card了解vuetify中的插槽如何使用以及对应的api如何阅读，先回想一下vue的插槽的作用：https://cn.vuejs.org/guide/components/slots.html-->
<!-- vCard的api中有关于slot的描述：https://vuetifyjs.com/zh-Hans/api/v-card/#slots -->


<script setup>
import { mdiAccount, mdiCheck, mdiAlarm } from '@mdi/js';
import { ref } from 'vue'

const loading = ref(true)
const color = ref('#81DDCE')

function changeLoading(){
  if(loading.value){
    color.value = ''
  }else{
    color.value = '#80DDCE'
  }
  loading.value = !loading.value
  console.log(color.value)
}
</script>
<template>
  <v-row align="center" justify="center" dense>
    <v-col cols="12" md="6">
      <v-card :append-icon="mdiCheck" class="mx-auto" :prepend-icon="mdiAccount" subtitle="prepend-icon and append-icon"
        title="Icons">
        <v-card-text>Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod.</v-card-text>
      </v-card>
    </v-col>

    <v-col cols="12" md="6">
      <v-card class="mx-auto" subtitle="prepend and append" title="Icons">
        <template v-slot:prepend>
          <v-icon color="primary" :icon="mdiAccount"></v-icon>
        </template>
        <template v-slot:append>
          <v-icon color="success" :icon="mdiCheck"></v-icon>
        </template>
        <v-card-text>Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod.</v-card-text>
      </v-card>
    </v-col>

  </v-row>


  <v-row>


    <v-col cols="12" md="6">
      <v-card append-avatar="https://cdn.vuetifyjs.com/images/john.jpg" class="mx-auto"
        prepend-avatar="https://cdn.vuetifyjs.com/images/logos/v-alt.svg" subtitle="prepend-avatar and append-avatar"
        title="Avatars">
        <v-card-text>Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod.</v-card-text>
      </v-card>
    </v-col>

    <v-col cols="12" md="6">
      <v-card class="mx-auto" subtitle="prepend and append" title="Avatars">
        <template v-slot:prepend>
          <v-avatar color="blue-darken-2">
            <v-icon :icon="mdiAlarm"></v-icon>
          </v-avatar>
        </template>
        <template v-slot:append>
          <v-avatar size="24">
            <v-img alt="John" src="https://cdn.vuetifyjs.com/images/john.png"></v-img>
          </v-avatar>
        </template>
        <v-card-text>Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod.</v-card-text>
      </v-card>
    </v-col>
  </v-row>


  <!-- 通过loader来演示slot的作用https://vuetifyjs.com/zh-Hans/api/v-card/#slots-loader -->
  <!-- https://vuetifyjs.com/zh-Hans/components/cards/#section-52a08f7d -->
  <!--参考api文档可知，loader的结构是{ color: string; isActive: boolean } -->
  <!-- 这个slot的作用就是Slot for custom loader (displayed when loading prop is equal to true -->
   <!-- 也就是说，当loading为true的时候这个slot将被v-card组件展示，因此可以用作进度条 -->
  <v-row>
    <v-col cols="12" md="6" @click="changeLoading">
      <v-card class="mx-auto" :loading="color" :disabled="loading" :color="color" >
        <template v-slot:loader="loader">
          <!-- 可以观察到这个loader里刚好有个isActive和color，-->
           <!-- isActive就是说这个slot有没有启用，而color其实就是loading，可以参考api中的loading的描述 -->
          {{ loader }}
          <v-progress-linear  :active="loader.isActive" color="deep-purple" height="4" indeterminate></v-progress-linear>

        </template>
        <v-card-text v-if="loading"></v-card-text>
        <v-card-text v-else>Oh no 😢</v-card-text>

      </v-card>
    </v-col>

  </v-row>
</template>