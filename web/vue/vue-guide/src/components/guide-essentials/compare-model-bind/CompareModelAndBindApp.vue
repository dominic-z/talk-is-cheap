<script setup>
//  对比v-bind和v-model，你会发现v-bind只负责将数据导入到视图中，而无法从视图读取最新的数据，所以更新idForBind那个input并不会更新idForBind
// 而idForModel那个text就不一样了，他能实现双向更新https://www.doubao.com/thread/w1bae4ee33a8fd8e8

import {ref} from "vue"
const idForBind = ref(1)
const idForModel = ref(1)

function logId(){
    console.log("idForBind",idForBind.value)
    console.log("idForModel",idForModel.value)
}

</script>

<template>

<button @click="logId">logId</button>
    <div>
        <input :value="idForBind" type="text"></input>
    </div>


    <div>
        <input v-model="idForModel" type="text"></input>
    </div>

</template>