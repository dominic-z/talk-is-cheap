

<script setup>
// 使用@mdi/font作为字体和icon来源vuetify第一个样例


import { ref } from 'vue'
const drawer = ref(null)
</script>

<template>



    <v-app id="inspire">
        <v-system-bar>
            <v-spacer></v-spacer>

            <v-icon>mdi-square</v-icon>

            <v-icon>mdi-circle</v-icon>

            <v-icon>mdi-triangle</v-icon>

            <!-- 官网说：虽然在 Vuetify 3 中，你依然可以直接在默认插槽使用图标值（比如说 <v-icon>mdi-home</v-icon>）指定图标，但我们还是建议使用 icon prop 替代。 -->
            <v-icon icon="mdi-account-cowboy-hat"></v-icon>
        </v-system-bar>

        <v-app-bar>
            <v-app-bar-nav-icon @click="drawer = !drawer"></v-app-bar-nav-icon>

            <v-app-bar-title>Application</v-app-bar-title>

            <template v-if="$vuetify.display.mdAndUp">
                <v-btn icon="mdi-magnify" variant="text"></v-btn>

                <v-btn icon="mdi-filter" variant="text"></v-btn>
            </template>

            <v-btn icon="mdi-dots-vertical" variant="text"></v-btn>
            
        </v-app-bar>

        <v-navigation-drawer v-model="drawer" temporary>
            <!--  -->
        </v-navigation-drawer>

        <v-main class="bg-grey-lighten-2">
            <v-container>
                <v-row>
                    <template v-for="n in 4" :key="n">
                        <v-col class="mt-2" cols="12">
                            <strong>Category {{ n }}</strong>
                        </v-col>

                        <v-col v-for="j in 6" :key="`${n}${j}`" cols="6" md="2">
                            <v-sheet height="150"></v-sheet>
                        </v-col>
                    </template>
                </v-row>
            </v-container>
        </v-main>
    </v-app>
</template>

