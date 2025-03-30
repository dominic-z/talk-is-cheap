<script setup>
import store from '../vuex/store';
import { ref } from 'vue';

function incrementAsync() {
    // 异步调用
    let p = store.dispatch('incrementAsync')
    console.log(p)

    console.log(store.getters.getCount)
    p.then((v)=>{
        console.log('done')
    })
}


</script>

<template>

    <p>
        evenNums: {{ store.getters.evenNumbers }}
    </p>
    <p>
        count: {{ store.getters.getCount }}
    </p>
    <p>
        count: {{ store.state.count }}
    </p>
    <button @click="()=>{store.commit('increment')}">increment</button>
    <button @click="incrementAsync">fetchUserData</button>
</template>