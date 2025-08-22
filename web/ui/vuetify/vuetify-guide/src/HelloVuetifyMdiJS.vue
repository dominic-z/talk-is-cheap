
<script setup>

// 使用@mdi/js作为字体和icon来源vuetify第一个样例，依赖vuetifyConfig.js指定icon来源，与@mdi/font的区别在于使用icon的方式不同
import { mdiAccountCowboyHat,mdiTriangle,mdiCircle,mdiSquare } from '@mdi/js'
import { mdiMagnify,mdiFilter,mdiDotsVertical } from '@mdi/js'

import { ref } from 'vue'
const drawer = ref(null)
</script>


<template>



    <v-app id="inspire">
        <v-system-bar>
            <v-spacer></v-spacer>

            <!-- 使用icon的方式发生了改变，需要使用v-bind的方式进行属性绑定 -->
            <v-icon :icon="mdiSquare"></v-icon>

            <v-icon :icon="mdiCircle"></v-icon>

            <v-icon :icon="mdiTriangle"></v-icon>
            <v-icon :icon="mdiAccountCowboyHat" />
            
        </v-system-bar>

        <v-app-bar>
            <!-- 注意，如果使用@mdi/js，这里也要改成驼峰，而不能使用kebab-case -->
            <vAppBarNavIcon  @click="drawer = !drawer"></vAppBarNavIcon>

            <v-app-bar-title>Application</v-app-bar-title>

            <template v-if="$vuetify.display.mdAndUp">
                <!-- 使用icon的方式发生了改变 -->
                <v-btn :icon="mdiMagnify" variant="text"></v-btn>

                <v-btn :icon="mdiFilter" variant="text"></v-btn>
            </template>

            <v-btn :icon="mdiDotsVertical" variant="text"></v-btn>
            
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
