<script setup>
import { reactive, ref } from 'vue';
import { useMouse } from './Mouse.js'
import { useFetch } from './Fetch.js';

const r = ref(useMouse())


const url = ref("https://httpbin.org/get")
const { data, error } = useFetch(url)

const param = ref(null)

function switchParam(){
    url.value = "https://httpbin.org/get?a="+param.value;
    console.log(url.value)
}

</script>

<template>
    <div>
        Mouse position is at: {{ r.x }}, {{ r.y }}
    </div>

    <hr />

    <div>
        <input :value="param" @input="event => param = event.target.value" type="text"></input>
        <button @click="switchParam">Param</button>
        <div v-if="error">Oops! Error encountered: {{ error.message }}</div>
        <div v-else-if="data">
            Data loaded:
            <pre>{{ data }}</pre>
        </div>
        <div v-else>Loading...</div>
    </div>

</template>