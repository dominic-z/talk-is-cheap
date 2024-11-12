<script setup>
import { computed, ref, watch } from 'vue';
import { useRoute } from 'vue-router';

let id = ref(0)
let to = computed(() => {
    return "/user/" + id.value
})
function add() {
    id.value = id.value + 1

    console.log(to.value)
}


</script>


<template>

    <button @click="add">加1 {{ id }}</button>
    <RouterLink :to=to>/user/{{ id }}</RouterLink>
    <RouterLink to="/showYourself/1?a=b">/showYourself/1?a=b</RouterLink>
    <RouterLink to="/avatar">/avatar</RouterLink>
    <RouterView></RouterView>
</template>