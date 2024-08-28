<script setup>
import { watch } from 'vue'
import { useRoute } from 'vue-router'
import { onBeforeRouteUpdate } from 'vue-router'

const route = useRoute()
console.log(route.params)
// 这个必须要watch一个getter函数，因为route.params.postId并不是一个ref
watch(() => route.params.postId, (newId, oldId) => {
  console.log('newId ', newId, 'oldId ', oldId)
})

// ...

onBeforeRouteUpdate(async (to, from) => {
  // 对路由变化做出响应...
  console.log('to ', to, 'from ', from)
})

</script>

<template>
  <div>
    User {{ $route.params.username }} with post {{ $route.params.postId }} query: {{ $route.query }}
  </div>
</template>