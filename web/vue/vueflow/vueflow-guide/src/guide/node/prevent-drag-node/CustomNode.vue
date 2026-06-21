<script setup>
import { ref } from 'vue'

const inputValue = ref('')
</script>

<template>
  <div class="custom-node-container nodrag">
    <!-- https://github.com/xyflow/xyflow/issues/4198 -->
     <!-- By default the noDragClassName is "nodrag". That means if you add "nodrag" to an element, the built-in drag handler wouldn't apply. You can change that by setting another class name via noDragClassName. -->
    <!-- 效果：这是一个输入框，如果没有指定：class="nodrag"。在输入了一些文本的时候，如果想左键+移动来选中文本，会发现拖着这个node到处走，在加了class="nodrag"后，会发现拖拽node的行为不存在了 -->
     <!-- 但是通过noDragClassName自定义这个类名替换nodrag失效了 -->
      <!-- 也就是说这个input无法被拖拽 -->
    <input class="nodrag" v-model="inputValue" />
  </div>
</template>