<script setup>
import { Handle, Position } from '@vue-flow/core';


const props = defineProps(['data'])

</script>

<template>
    <div>

        <!-- x轴的margin是6 -->
        <div class="text-h6 mx-6 my-3 cursor-pointer">
            <Handle type="target" :position="Position.Top" :style="{ width: '15px', height: '15px' }"></Handle>
            <slot name="content"></slot>
            <div v-if="!$slots.content">{{ props.data.label }}</div>
            <Handle type="source" :position="Position.Bottom" :style="{ width: '15px', height: '15px' }"></Handle>

        </div>

        <slot name="append"></slot>
    </div>


</template>

<style>
/* 方式node过宽或者过高，基本来自豆包 */
.vue-flow__node-RunnableStage {
    cursor: pointer !important;
    border-radius: 8px;
    border: 4px solid;

    max-width: 400px;
    max-height: 200px;
    /* 2. 允许文本自动换行（默认值，但需确保未被覆盖） */
    white-space: normal;
    /* 关键：取消 "nowrap"（不换行）的干扰，默认就是 normal，可显式声明防覆盖 */

    /* 3. 确保文本不会超出容器（可选，防止极端情况溢出） */
    text-overflow: auto;
    /* 溢出部分隐藏（若需显示滚动条，可改为 auto） */


    /* 实现hover阴影 */
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
    /* 初始阴影：水平偏移 0，垂直偏移 4px，模糊 6px */
    transition: box-shadow 0.2s ease;
    /* 平滑过渡效果 */
}


.vue-flow__node-RunnableStage:hover {
    box-shadow: 0 8px 12px rgba(0, 0, 0, 0.6);
}
</style>