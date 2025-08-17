<script setup>
import { useActionStore } from './ActionStore.js';

const actionStore = useActionStore()


// 监听

const unsubscribe = actionStore.$onAction(
  ({
    name, // action 名称
    store, // store 实例，类似 `someStore`
    args, // 传递给 action 的参数数组
    after, // 在 action 返回或解决后的钩子
    onError, // action 抛出或拒绝的钩子
  }) => {
    // 为这个特定的 action 调用提供一个共享变量
    const startTime = Date.now()
    // 这将在执行 "store "的 action 之前触发。
    console.log(`Start "${name}" with params [${args.join(', ')}].`)

    // 这将在 action 成功并完全运行后触发。
    // 它等待着任何返回的 promise
    after((result) => {
      console.log(
        `Finished "${name}" after ${
          Date.now() - startTime
        }ms.\nResult: ${result}.`
      )
    })

    // 如果 action 抛出或返回一个拒绝的 promise，这将触发
    onError((error) => {
      console.warn(
        `Failed "${name}" after ${Date.now() - startTime}ms.\nError: ${error}.`
      )
    })
  }
)

// 手动删除监听器
// unsubscribe()

</script>


<template>
    <div>

        <button @click="actionStore.increament">increament</button>
        <button @click="actionStore.randomizeCounter">randomizeCounter</button>
        <button @click="()=>actionStore.registry('zzz')">registry</button>
        
        <div>
            count: {{ actionStore.count }}
        </div>
        <div>
            msg: {{ actionStore.msg }}
        </div>


        
    </div>

</template>