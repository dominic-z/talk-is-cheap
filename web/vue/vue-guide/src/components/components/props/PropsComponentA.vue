<script setup>
import { watch, watchEffect } from 'vue'

const props = defineProps(['foo', 'greetingMessage'])
watchEffect(() => {
    // 在 3.5 之前只运行一次
    // 在 3.5+ 中在 "foo" prop 变化时重新执行
    console.log(props.foo, 'watchEffect')
})

// 需要写成getter形式,因为对于PropsComponentA来说,props.foo只是一个普通的变量,不具备响应功能
watch(() => props.foo, () => {
    console.log(props.foo, 'watch ()=>props.foo')

})

</script>


<template>

    <div>
        PropsComponentA

        <div>
            {{ foo }}

            <!-- 在大多数场景下，子组件应该抛出一个事件来通知父组件做出改变。 -->
            <button @click="() => foo += 'aa'">update foo in compnotent</button>
        </div>
    </div>

</template>