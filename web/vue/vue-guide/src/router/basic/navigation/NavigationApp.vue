<script setup>
import { useRouter } from 'vue-router';

const router = useRouter()
const route = useRoute()
function click1(e) {
    // console.log(e.target.value)
    router.push('/user/click1')
}

function click2(e) {
    // console.log(e.target.value)
    router.push({ name: 'user', params: { id: 'click2' } })
}

function click3(e) {
    // console.log(e.target.value)
    router.push({ name: 'user', query: { plan: 'private' } })
}

function click4(e) {
    // console.log(e.target.value)
    router.replace({ name: 'user', query: { plan: 'private' } })
}

function forward() {
    router.go(1)
}

function backward() {
    router.go(-1)
}
</script>

<template>
    <label for="1">
        <input type="radio" name="navi" id="1" @click="click1" />
        /user/eduardo
    </label>
    <label for="2">
        <input type="radio" name="navi" id="2" @click="click2" />
        { name: 'user', params: { id: 'click2' } }
    </label>
    <label for="3">
        <input type="radio" name="navi" id="3" @click="click3" />
        { name: 'user', query: { plan: 'private' } }
    </label>
    <label for="4">
        <input type="radio" name="navi" id="4" @click="click4" />
        { name: 'user', query: { plan: 'private' } }
    </label>
    <div>

        <input type="button" @click="backward" value="backward">
        <input type="button" @click="forward" value="forward">
    </div>
    <RouterView></RouterView>
</template>