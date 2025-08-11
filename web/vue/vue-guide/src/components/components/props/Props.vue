<script setup>
import { ref } from 'vue';
import PropsComponentA from './PropsComponentA.vue';

let foo = ref('foo1')

let param = ref({
    foo: 'foo2',
    greetingMessage: 'aa'
})

</script>

<template>
    <!-- 虽然foo是一个ref,但是对于PropsComponentA,他的props.foo是一个普通的常量而不是一个ref -->
    <PropsComponentA :foo="foo" :greeting-message="'greeting'"></PropsComponentA>
    <button @click="() => foo += '1'">update foo</button>
    <hr />

    <!-- 等价 -->
    <PropsComponentA v-bind="param"></PropsComponentA>
    <button @click="() => param.foo += '1'">update param.foo</button>

</template>