<script setup>
import { ref } from 'vue'
import MyTransition from './MyTransition.vue'
import TransitionBetweenComp from './TransitionBetweenComp.vue'
import TransitionBetweenEle from './TransitionBetweenEle.vue'
const show1 = ref(true)
const show2 = ref(true)
const show3 = ref(true)
const show4 = ref(true)
</script>

<template>

  <button @click="show1 = !show1">Toggle1</button>
  <Transition>
    <p v-if="show1">hello</p>
  </Transition>

  <hr />

  <button @click="show2 = !show2" >Toggle2</button>
  <Transition name="slide-fade" appear>
    <p v-if="show2">hello</p>
  </Transition>

  <hr />

  <button @click="show3 = !show3">Toggle3</button>
  <Transition name="nested" :duration="550">
    <div v-if="show3" class="outer">
      <div class="inner">
        Hello
      </div>
    </div>
  </Transition>

  <hr />
  <button @click="show4 = !show4">Toggle1</button>

  <MyTransition>
    <div v-if="show4">Hello</div>
  </MyTransition>

  <hr />
  
  <TransitionBetweenComp></TransitionBetweenComp>
  <hr />
  <TransitionBetweenEle></TransitionBetweenEle>

</template>

<style>
.v-enter-active,
.v-leave-active {
  transition: opacity 0.5s ease;
}

.v-enter-from,
.v-leave-to {
  opacity: 0;
}



/*
  进入和离开动画可以使用不同
  持续时间和速度曲线。
*/
.slide-fade-enter-active {
  transition: all 0.3s ease-out;
}

.slide-fade-leave-active {
  transition: all 0.8s cubic-bezier(1, 0.5, 0.8, 1);
}

.slide-fade-enter-from,
.slide-fade-leave-to {
  transform: translateX(20px);
  opacity: 0;
}




.outer, .inner {
	background: #eee;
  padding: 30px;
  min-height: 100px;
}
  
.inner { 
  background: #ccc;
}
/* nested会直接作用于根节点，不需要.nested outer */
/* 效果如下，进入时， outer直接进入，耗时0.3s，随后inner推迟0.25s后开始进入，耗时0.3s*/
/* 效果如下，退出时， inner直接退出，耗时0.3s，随后outer推迟0.25s后开始退出，耗时0.3s*/
.nested-enter-active, .nested-leave-active {
	transition: all 0.3s ease-in-out;
}
/* delay leave of parent element */
.nested-leave-active {
  transition-delay: 0.25s;
}

.nested-enter-from,
.nested-leave-to {
  transform: translateY(30px);
  opacity: 0;
}

/* we can also transition nested elements using nested selectors */
.nested-enter-active .inner,
.nested-leave-active .inner { 
  transition: all 0.3s ease-in-out;
}
/* delay enter of nested element */
.nested-enter-active .inner {
	transition-delay: 0.25s;
}

.nested-enter-from .inner,
.nested-leave-to .inner {
  transform: translateX(30px);
  /*
  	Hack around a Chrome 96 bug in handling nested opacity transitions.
    This is not needed in other browsers or Chrome 99+ where the bug
    has been fixed.
  */
  opacity: 0.001;
}

/* ... 省略了其他必要的 CSS */
</style>