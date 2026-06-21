<script setup>
import { ref } from 'vue';
import MyButton from './MyButton.vue';


let count = ref(1)

function increaseBy2() {

}
</script>

<template>
    <!-- // vue中的emit只能让父组件监听子组件的事件，非父子组件的消息传递得靠全局状态来维护，比如pinia -->
    <MyButton @increase-by="(n) => count += n" @increase-by2="(n) => count += n" />
    <div>
        {{ count }}
    </div>

</template>