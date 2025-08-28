<script setup>

// 这是个手动展开的nav，没有借助expand-on-hover属性，这样可以自由一些


import { mdiStar } from '@mdi/js';
import { mdiAlphaP } from '@mdi/js';
import { mdiAlphaA } from '@mdi/js';
import { mdiAccountMultiple } from '@mdi/js';
import { mdiFolder } from '@mdi/js';


import { ref } from 'vue'

const showCard = ref(false)
function updateRail(e){
    if(e){
        // 说明nav收拢了
        console.log(e)
        showCard.value = false
    }else{
        // 说明nav展开了
    }
}


function updateShowDetail(){
    // 反复尝试的结果，展开card的时候设置一个延时，这样可以确保当鼠标直接hover到这个item的时候，展开card在nav展开完成之后。
    // 如果不加延时，那么直接hover上去之后，会跳一下。很突兀。跳一下就是因为nav展开和showCard展示同时发生了。
    // 但如果mouseleave的话就不能延时，否则可能导致card不会被正常删除
    if(showCard.value){
        showCard.value = !showCard.value
    }else{
        setTimeout(()=>showCard.value = !showCard.value,100)
    }
}

const rail = ref(true)

</script>
<template>
        <v-app>
            <v-navigation-drawer @mouseenter="rail=false" @mouseleave="rail=true" permanent :rail="rail" @update:rail="updateRail">
                <v-list>
                    <v-list-item prepend-avatar="https://randomuser.me/api/portraits/women/85.jpg"
                        subtitle="sandra_a88@gmailcom" title="Sandra Adams"></v-list-item>
                </v-list>

                <v-divider></v-divider>

                <v-list density="compact" nav>
                    <v-slide-y-transition :group="true" :leave-absolute="true" :hide-on-leave="true">

                    <v-list-item :prepend-icon="mdiFolder" key='1' title="My Files" value="myfiles"></v-list-item>
                    <v-list-item :prepend-icon="mdiAccountMultiple" key="2" title="Shared with me" value="shared"></v-list-item>
                    <v-list-item :prepend-icon="mdiStar" key="3" title="Starred" value="starred"></v-list-item>

                    <v-list-item v-if="showCard" key="4-1"
                        @mouseleave="updateShowDetail" link>
                        <template v-slot:default>
                            <div class="v-list-item pa-0">
                                <v-progress-circular color="primary" :model-value="20" :size="25"
                                    class="mr-5"></v-progress-circular>
                                <span class="overflow-hidden">fsdfs</span>
                            </div>

                            <v-card class="mt-2">
                                <div>123213</div>
                                <div>123213</div>
                                <div>123213</div>
                                <div>123213</div>
                            </v-card>
                        </template>

                    </v-list-item>
                


                    
                    <v-list-item key="4-2" v-else @mouseenter="updateShowDetail">
                        <div class="v-list-item pa-0">
                            <v-progress-circular color="primary" :model-value="20" :size="25"
                                class="mr-5"></v-progress-circular>
                            <div class="overflow-hidden">fsdfs</div>
                        </div>

                    </v-list-item>

                    <v-list-item :prepend-icon="mdiAlphaA" key="5" title="AAA" value="AAA"></v-list-item>
                    <v-list-item :prepend-icon="mdiAlphaP" key="6" title="PP" value="PP"></v-list-item>
                </v-slide-y-transition>

                </v-list>
            </v-navigation-drawer>

            <v-main style="height: 250px"></v-main>
        </v-app>
</template>