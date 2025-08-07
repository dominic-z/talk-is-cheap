<script setup>
let num = 1;

let id=5;
let rawHtml='<span style="color: red">This should be red.</span>'
let isButtonDisabled='sfasfsd'
const objectOfAttrs = {
  id: 'container',
  class: 'wrapper',
  style: 'background-color:green'
}

function add(n){
    return n+1;
}

let seen=true

let attributeName='id'
</script>

<template>

<div>
  num: {{ num }}
</div>

<p>Using text interpolation: {{ rawHtml }}</p>
<p>Using v-html directive: <span v-html="rawHtml"></span></p>

<!-- 演示attribute绑定-->
<!-- 下面的:disabled就是html原生的属性，这个属性是bool格式的，vue识别到这个的时候会按照bool的规则，控制生成的html标签上是否存在disabled这个属性 -->
<!-- 否则，还是会作为简单的自定义属性，为html标签新增一个k=v格式的属性 -->
<div :id>嘎嘎嘎</div>


<button :disabled="isButtonDisabled">Button</button>
<div v-bind="objectOfAttrs">objectOfAttrs</div>

<div :id="`list-${id+1}`">gag1</div>
<div :id="add(12)">gag1</div>
<div>{{ globalMsg }}</div>
<div :[attributeName]="id+2">attributeName</div>

<p v-if="seen">Now you see me</p>

</template>

<style scoped>
div{
    padding: 2px 0px;
}
</style>