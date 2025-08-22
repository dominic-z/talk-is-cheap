<script setup>

// import HomeNav from '@/views/HomeNav.vue';
import GlobalNav from '@/components/nav/GlobalNav.vue';
import { ref } from 'vue'


// import {VAppBarNavIcon} from '@/vuetify'

</script>

<template>
  <v-app id="inspire">
    <GlobalNav></GlobalNav>

    <v-main>
      <v-container>
        <v-row>
          <v-col
            v-for="n in 24"
            :key="n"
            cols="4"
          >
            <v-card height="200"></v-card>
          </v-col>
        </v-row>
      </v-container>
    </v-main>
  </v-app>
</template>
