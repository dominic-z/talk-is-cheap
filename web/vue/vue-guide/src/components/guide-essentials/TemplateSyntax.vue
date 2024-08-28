<script setup>
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
</script>

<template>


<p>Using text interpolation: {{ rawHtml }}</p>
<p>Using v-html directive: <span v-html="rawHtml"></span></p>

<div :id>嘎嘎嘎</div>


<button :disabled="isButtonDisabled">Button</button>
<div v-bind="objectOfAttrs">objectOfAttrs</div>

<div :id="`list-${id+1}`">gag1</div>
<div :id="add(12)">gag1</div>

<p v-if="seen">Now you see me</p>

</template>

<style scoped>
div{
    padding: 2px 0px;
}
</style>