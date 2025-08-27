<script setup>

import {ref} from 'vue'
let something = 'something';
function doSomething(something) {
    console.log('do ' + something);
}

function doThis() {
    console.log('do this');
}

function onSubmit() {

}


const mouseLeaveDiv = ref(null)

function handleSelectionMouseLeave(e){
    // 获取 Selection 对象
  const selection = window.getSelection();
  console.log(selection)
  // 判断是否有选中内容
  if (selection.rangeCount > 0) {
    // 获取选中文本
    const selectedText = selection.toString();
    console.log("选中文本为：",selectedText)
    
    // 可选：获取选中范围的详细信息（如起始/结束节点、偏移量）
    const range = selection.getRangeAt(0);
    console.log('选中范围起始节点：', range.startContainer);
    console.log('选中范围结束偏移量：', range.endOffset);
  } else {
    console.log('未选中');

  }

  // 也可以通过模板引用获取一个组件的真实位置信息，并将其与鼠标位置进行对比
  const rect = mouseLeaveDiv.value.getBoundingClientRect();
  console.log(e)
  console.log(rect)
  // 判断规则略
}

</script>

<template>
    <div @click="() => { console.log('outer div event') }" :style="{ padding: '10px', backgroundColor: 'gra' }">
        <!-- 停止传播，事件不会被冒泡到上层的div -->
        <a @click.stop="doSomething(something)">aaa</a>
    </div>


    <!-- 提交事件将不再重新加载页面，相当于并不会提交表单，多数情况下还是希望自己实现doThis提交方法自己提交表单，而不是用默认方法提交 -->
    <form @submit.prevent="doThis" action="http://httpbin.org/get" method="get">
        <button type="submit">提交-不会请求httpbin</button>
    </form>

    <form @submit="doThis" action="http://httpbin.org/get" method="get">
        <button type="submit">提交-会请求httpbin</button>
    </form>

    <!-- .self相当于加了个判断，只有事件的targe是自己的时候，才会触发handler -->
    <!-- 点击a，只会触发a的handler -->
    <div @click.self="(e) => { console.log(e.target) }" :style="{ padding: '10px', backgroundColor: 'blue' }">
        <a @click.self="doSomething(something)">self test</a>
    </div>
    <!-- 点击a，a的handler和dive的handler都会触发 -->
    <div @click="(e) => { console.log(e.target) }" :style="{ padding: '10px', backgroundColor: 'green' }">
        <!-- 停止传播，事件不会被冒泡到上层的div -->
        <a @click="doSomething(something)">self test</a>
    </div>

    <!-- 仅在 `key` 为 `Enter` 时调用 `submit` -->
    <input @keyup.enter="doSomething(something)" value="onenter" />
    <input @keyup.page-down="doSomething(something)" value="onpagedown" />
    <input @keyup.alt.enter="doSomething(something)" value="on alt enter" />
    <input @keyup.enter.exact="doSomething(something)" value="on enter exact" />
    <input type="button" @click.right="doSomething(something)" value="on right click" />


<!-- 以下为开发过程中遇到的问题 -->
 
    <div @mouseleave="(e) => console.log(e)">
        突然发现选中文本也会触发mouse leave事件，我觉得可能是因为选中文本后鼠标浏览器会让选择复制还是怎么着的，鼠标此时已经不再dom中了
    </div>

    <div @mouseleave="handleSelectionMouseLeave" ref="mouseLeaveDiv">
        在mouseleave函数中可以通过获取selection对象来判断是不是真的进行了mouseleave
    </div>
</template>


<style scoped>
div{
    height: 200px;
    width: 200px;
    border: 1px solid;
    text-wrap: break;
}
</style>