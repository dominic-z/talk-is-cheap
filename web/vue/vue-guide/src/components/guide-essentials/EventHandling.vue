<script setup>


let something = 'something';
function doSomething(something) {
    console.log('do ' + something);
}

function doThis() {
    console.log('do this');
}

function onSubmit() {

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
    <div @click.self="(e) => { console.log(e.target) }" :style="{ padding: '10px', backgroundColor: 'gra' }">
        <a @click.self="doSomething(something)">self test</a>
    </div>
    <!-- 点击a，a的handler和dive的handler都会触发 -->
    <div @click="(e) => { console.log(e.target) }" :style="{ padding: '10px', backgroundColor: 'gra' }">
        <!-- 停止传播，事件不会被冒泡到上层的div -->
        <a @click="doSomething(something)">self test</a>
    </div>

    <!-- 仅在 `key` 为 `Enter` 时调用 `submit` -->
    <input @keyup.enter="doSomething(something)" value="onenter" />
    <input @keyup.page-down="doSomething(something)" value="onpagedown" />
    <input @keyup.alt.enter="doSomething(something)" value="on alt enter" />
    <input type="button" @click.right="doSomething(something)" value="on right click" />
</template>