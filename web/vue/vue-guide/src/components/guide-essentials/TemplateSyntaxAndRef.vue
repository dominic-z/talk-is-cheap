<script setup>
import { ref } from 'vue';
// 对比模板语法中的v-bind和ref两者的区别，一开始的疑惑时，v-bind也是一种响应式地绑定一个 attribute的方式
// 和ref是否冲突

let id=5;

function addId(n){
    id += 1
}

let bindId=2;

function addBindId(n){
    bindId += 1
}

let refId = ref(1)
function addRefId(){
  refId.value+=1
}

</script>

<template>


<!-- 效果，这个div的id属性就是字符串的id，里面的{{ id }}也就是初始数字，addId方法并不会立刻更新页面中的{{ id }} -->
<div id="id">
  id: {{ id }}
  <button @click="addId">addId</button>
</div>

<!-- 效果，这个div的id属性是变量bindId的值，里面的{{ bindId }}也就是初始数字，addBindId方法并不会立刻更新页面中的{{ addBindId }} -->
<div :id="bindId">
  bindId: {{ bindId }}
  <button @click="addBindId">addBindId</button>
</div>


<!-- 效果，这个div的id属性是变量refId的值，里面的{{ refId }}也就是初始数字，addRefId方法会立刻更新页面中的{{ refId }}，以及属性上的id属性 -->
 <!-- 并且还会触发当前的组建的刷新，也就是说，如果调用了addId/addBindId，再调用addRefId，刷新页面的时候会使用新的Id和BindId -->
<div :id="refId">
  refId: {{ refId }}
  <button @click="addRefId">addRefId</button>
</div>

</template>
