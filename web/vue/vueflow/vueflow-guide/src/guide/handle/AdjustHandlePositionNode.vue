<script setup>
// 调整handle的位置，但是我没理解作用是啥
import { Position, Handle } from '@vue-flow/core'

// props were passed from the slot using `v-bind="customNodeProps"`
const props = defineProps(['data'])

</script>

<template>
  <div :style="{ border: '1px solid', padding: '10px' }">
    <span>{{ props.data.label }}</span>


    <!-- 说实话我没明白这样怎么就调整handle的位置了，反而多套了个div，对比下面两个handle，会发现source这个handle反而多套了个带padding的div，没明白这有啥用，调整handle的位置可以参考multihandle -->
    <div style="position: relative; padding: 10px">
      <Handle type="source" :position="Position.Right" />
    </div>
    <Handle type="target" :position="Position.Left" />
  </div>
</template>