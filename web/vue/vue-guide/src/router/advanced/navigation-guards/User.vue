<script setup>
import { onBeforeRouteLeave, onBeforeRouteUpdate } from 'vue-router'
import { useRoute } from 'vue-router';
import { watch } from 'vue';
const route = useRoute();
watch(() => route.params.id,
    async newId => {
        console.log('newId', newId);
    }
)

// 组合式api不能使用beforeRouterEnter，但可以用这种方法https://blog.csdn.net/qq_17335549/article/details/127942181
onBeforeRouteUpdate(async (to, from) => {
    //仅当 id 更改时才获取用户，例如仅 query 或 hash 值已更改
    if (to.params.id !== from.params.id) {
        console.log('to param', to.params.id, 'from param', from.params.id);
    }
})


onBeforeRouteLeave((to, from) => {
    // 如果从/user/:id跳转到/showYourself/1?a=b，因为/showYourself/1本身有一个beforeEnter将query抹去
    // 所以实际上做的跳转是/user/:id到/showYourself/1?a=b，然后变成user/:id到/showYourself/1
    // 所以在当前的代码里，这个beforeLeave会执行两次
    const answer = window.confirm(
        'Do you really want to leave? you have unsaved changes!'
    )
    // 取消导航并停留在同一页面上
    if (!answer) return false
})
</script>

<template>
    <div>
        Role: {{ $route.fullPath }}
    </div>
</template>