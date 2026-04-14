<script setup>
import { watch, watchEffect } from 'vue'

const props = defineProps(['foo', 'greetingMessage'])
watchEffect(() => {
    // 在 3.5 之前只运行一次
    // 在 3.5+ 中在 "foo" prop 变化时重新执行
    console.log(props.foo, 'watchEffect')
})

// 一个常见的场景，当props发生变化的时候，组件可能需要重新执行一些操作，
// 但响应式并不是通过重新加载组件而实现的，比如当Props.vue组件更新了foo的值之后，并不会重新加载这个组件的
// 这就导致了比如说下面的“console.log('组件加载')”其实只会执行一次，并不是说props更新了，整个组件就要重新加载
// 因此，需要watch，另需要写成getter形式，因为对于PropsComponentA来说，props.foo只是一个普通的变量,不具备响应功能
watch(() => props.foo, onUpdateFoo)

function onUpdateFoo(){
    console.log(props.foo, 'watch ()=>props.foo');
}
// 这里只会在组件加载的时候执行一次
console.log('组件加载')



</script>


<template>

    <div>
        PropsComponentA

        <div>
            {{ props.foo }}

        </div>
    </div>

</template>