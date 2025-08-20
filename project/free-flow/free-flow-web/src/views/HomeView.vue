<script setup>

import HomeNav from '@/views/HomeNav.vue';
import {ref} from 'vue'



</script>

<template>
  <v-app class="rounded rounded-md border">
    <HomeNav></HomeNav>

    <v-app-bar title="Application bar"></v-app-bar>

    <v-main class="d-flex align-center justify-center" height="300">
      <v-container>
        <v-sheet
          border="dashed md"
          color="surface-light"
          height="200"
          rounded="lg"
          width="100%"
        ></v-sheet>
      </v-container>
    </v-main>
  </v-app>
</template>
