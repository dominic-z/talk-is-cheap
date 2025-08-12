
<script setup>
import { ref } from 'vue';
import MyButton from './MyButton.vue';


let count = ref(1)

function increaseBy2(){

}
</script>

<template>
    <MyButton @increase-by="(n) => count += n" @increase-by2="(n) => count += n"/>
        <div>
            {{ count }}
        </div>

</template>