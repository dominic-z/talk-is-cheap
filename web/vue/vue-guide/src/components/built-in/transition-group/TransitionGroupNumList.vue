<!--
通过内建的 <TransitionGroup> 实现“FLIP”列表过渡效果。
https://aerotwist.com/blog/flip-your-animations/
-->

<script setup>
// import { shuffle as _shuffle } from 'lodash-es'
import { ref } from 'vue'

const getInitialItems = () => [1, 2, 3, 4, 5]
const items = ref(getInitialItems())
let id = items.value.length + 1

function insert() {
  const i = Math.round(Math.random() * items.value.length)
  items.value.splice(i, 0, id++)
}

function reset() {
  items.value = getInitialItems()
  id = items.value.length + 1
}

function shuffle() {
    const newArray = [...items.value];
  
  // 从最后一个元素开始，向前遍历
  for (let i = newArray.length - 1; i > 0; i--) {
    // 生成一个从 0 到 i 的随机索引
    const j = Math.floor(Math.random() * (i + 1));
    
    // 交换第 i 个和第 j 个元素
    [newArray[i], newArray[j]] = [newArray[j], newArray[i]];
  }

  items.value = newArray
}

function remove(item) {
  const i = items.value.indexOf(item)
  if (i > -1) {
    items.value.splice(i, 1)
  }
}
</script>

<template>
  <button @click="insert">Insert at random index</button>
  <button @click="reset">Reset</button>
  <button @click="shuffle">Shuffle</button>

  <TransitionGroup tag="ul" name="fade" class="container">
    <li v-for="item in items" class="item" :key="item">
      {{ item }}
      <button @click="remove(item)">x</button>
    </li>
  </TransitionGroup>
</template>

<style>
.container {
  position: relative;
  padding: 0;
  list-style-type: none;
}

.item {
  width: 100%;
  height: 30px;
  background-color: #f3f3f3;
  border: 1px solid #666;
  box-sizing: border-box;
}

/* 1. 声明过渡效果 */
.fade-move,
.fade-enter-active,
.fade-leave-active {
  transition: all 0.5s cubic-bezier(0.55, 0, 0.1, 1);
}

/* 2. 声明进入和离开的状态 */
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: scaleY(0.01) translate(30px, 0);
}

/* 3. 确保离开的项目被移除出了布局流
      以便正确地计算移动时的动画效果。 */
.fade-leave-active {
  position: absolute;
}
</style>