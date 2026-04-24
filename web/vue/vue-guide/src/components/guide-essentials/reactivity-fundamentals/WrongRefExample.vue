<script setup>
// 一个偶然发现的错误使用样例
import { ref } from 'vue'

const staticObj = {
    name: 'staticObj',
    age: 1233,
    content: "paragraph"
}

const wrongRefObj = ref({...staticObj})


// 这种情况下，并不会触发响应式更新，因为wrongRefObj一开始指向的就是staticObj，现在又把staticObj赋值给wrongRefObj，所以不会触发响应式
function updateWrongRefObj(){
    staticObj.age+=1
    wrongRefObj.value = staticObj // 只会触发一次响应式
}

const rightRefObj = ref({...staticObj})
function updateRightRefObj(){
    staticObj.age+=1
    rightRefObj.value = {... staticObj} // 这样就会触发，因为对于vue来说，每次都是个新的对象了
}

// 如果只触发updateWrongRefObj事件，wrongRefObj只会触发一次响应式更新，后续再触发updateWrongRefObj，下方的wrongRefObj不会更新了
// 一些神奇的发现，如果触发了updateWrongRefObj然后再触发updateRightRefObj，wrongRefObj也会随staticObj.age的更新而响应式更新
// 原因：https://www.qianwen.com/share/chat/c3dd56c5e30f4e57a43e2eb886ff9e6f


</script>

<template>

    <div>wrongReactiveObj : {{ wrongRefObj }}</div>
    <div> <button @click="updateWrongRefObj">尝试更新wrongReactiveObj</button></div>
    <div>rightReactiveObj : {{ rightRefObj }}</div>
    <div> <button @click="updateRightRefObj">尝试更新rightReactiveObj</button></div>

</template>