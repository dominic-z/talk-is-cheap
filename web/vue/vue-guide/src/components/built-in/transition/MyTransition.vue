<!-- MyTransition.vue -->
<script setup>
// JavaScript 钩子逻辑...
function onEnter(e){
    console.log("enter",e)
}

function onLeave(e){
    console.log("leave",e)
}

</script>

<template>
  <!-- 包装内置的 Transition 组件 -->
  <Transition
    name="my-transition"
    @enter="onEnter"
    @leave="onLeave">
    <slot></slot> <!-- 向内传递插槽内容 -->
  </Transition>
</template>

<style>
/*
  必要的 CSS...
  注意：避免在这里使用 <style scoped>
  因为那不会应用到插槽内容上
*/

.my-transition-enter-active {
  transition: all 0.3s ease-out;
}

.my-transition-leave-active {
  transition: all 0.8s cubic-bezier(1, 0.5, 0.8, 1);
}

.my-transition-enter-from,
.my-transition-leave-to {
  transform: translateX(20px);
  opacity: 0;
}
</style>