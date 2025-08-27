<script setup>
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

</script>
<template>
        <v-app>
            <v-navigation-drawer expand-on-hover permanent rail @update:rail="updateRail">
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




                    <!-- 这是个啥玩意？解释一下，我的诉求是希望能够 -->
                    <!-- 1. 在nav的hover之前展示icon，之后展开nav，展开后额外展示title -->
                    <!-- 2. hover具体某一个item的时候，在这个item中再展开一个card，显示这个item的详细信息 -->
                    <!-- 参考文档中的原始代码，https://vuetifyjs.com/zh-Hans/components/navigation-drawers/#section-60ac505c65f662695c55 -->
                    <!-- 使用的是v-list-item的title属性实现标题， prepend-icon实现hover之前展示icon-->
                    <!-- 但是，在v-list-item中首先会渲染一个display为grid的块作为根，其类为v-list-item，下面有prepend和v-list-item__content，这两个块会并排存在 -->
                    <!-- 那么，如果我简单的在v-list-item中添加一个card话，这个card会被放在v-list-item__content -->
                    <!-- 最终样式就会变成 -->
                    <!--      title -->
                    <!-- icon card  -->
                    <!--      card  -->

                    <!-- 但我希望的样式是，也就是说我希望card能另起一行，不要和title挤在同一列里 -->
                    <!-- icon title  -->
                    <!-- c  a  r  d  -->
                    <!-- c  a  r  d  -->
                    <!-- 于是决定重写v-list-item的插槽自己实现，然后将默认插槽的div与v-card分开，div存储icon和title -->
                    <!-- 但是者带来了一个新问题，hover nav之前，title会被展示在nav中而不会隐藏掉，为了解决这个问题，直接在div中加上了v-list-item这个类，让div具备grid属性，从而固定div中的位置，反复尝试的结果，因为我也不太记得grid定位的用法了，所以直接抄vuetify内置的class -->
                
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