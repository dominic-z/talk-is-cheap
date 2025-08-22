<!-- 毫无疑问，card是最适合入门一套ui的组件，可以阅读每个组件上的api：https://vuetifyjs.com/zh-Hans/api/v-card/#props -->

<template>
    <v-row>
      <v-col cols="12" md="4">
        <v-card
          subtitle="This is a card subtitle"
          text="Lorem ipsum dolor sit amet consectetur adipisicing elit. Commodi, ratione debitis quis est labore voluptatibus!"
          title="This is a title"
        ></v-card>
  
        <div class="text-center text-caption">Using Props Only</div>
      </v-col>
  
      <v-col cols="12" md="4">
        <v-card>
          <template v-slot:title>
            This is a title
          </template>
  
          <template v-slot:subtitle>
            This is a card subtitle
          </template>
  
          <template v-slot:text>
            Lorem ipsum dolor sit amet consectetur adipisicing elit. Commodi, ratione debitis quis est labore voluptatibus!
          </template>
        </v-card>
  
        <div class="text-center text-caption">Using Slots Only</div>
      </v-col>
  
      <v-col cols="12" md="4">
        <v-card>
          <v-card-item>
            <v-card-title>This is a title</v-card-title>
  
            <v-card-subtitle>This is a card subtitle</v-card-subtitle>
          </v-card-item>
  
          <v-card-text>
            Lorem ipsum dolor sit amet consectetur adipisicing elit. Commodi, ratione debitis quis est labore voluptatibus!
          </v-card-text>
        </v-card>
  
        <div class="text-center text-caption">Using Markup Only</div>
      </v-col>
    </v-row>
  </template>