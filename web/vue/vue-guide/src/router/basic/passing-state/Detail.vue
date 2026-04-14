<template>
    <div style="padding: 20px;">
        <h2>📝 详情页 (接收数据)</h2>

        <div v-if="stateData">
            <h3>✅ 成功接收到 State 数据：</h3>
            <pre>{{ JSON.stringify(stateData, null, 2) }}</pre>
        </div>
        <div v-else>
            <h3>⚠️ 未找到 State 数据</h3>
            <p>可能的原因：你刷新了页面，或者直接访问了此 URL。</p>
            <p><strong>State 说明</strong>：数据只存在于浏览器历史记录中，刷新页面会丢失。</p>
        </div>

        <br>
        <button @click="goBack">返回列表页</button>
    </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const stateData = ref(null)


// 这套代码除了这一行，都是千问给我的，结果网上的人遇到了同样的问题，route并没有state这个对象
// https://www.cnblogs.com/dirgo/p/19275300
// 我也纳闷，router官方文档根本没有这段教程。。。。于是我看了官方文档，state对象在history对象里，所以必须得从这个方法里才能捞出来
console.log(router?.options?.history?.state)

// 方法1: 使用 watch 监听 route.state 变化 (推荐)
watch(
    () => route.state,
    (newState) => {
        console.log('Watch for route.state triggered, newState:', newState)
        if (newState && Object.keys(newState).length > 0) { // 检查是否为空对象
            console.log('Setting stateData to:', newState)
            stateData.value = newState
        } else {
            console.warn('Received empty state, resetting data.')
            stateData.value = null
        }
    },
    { immediate: true } // 立即执行，确保组件初始化时也能获取
)

// 方法2: 组件挂载时也尝试获取一次 (双重保险)
onMounted(() => {
    console.log('Detail 组件挂载，初始 route.state:', route.state)
    // 如果 watch 因为某些原因没触发，这里再尝试一次
    if (!stateData.value && route.state) {
        stateData.value = route.state
    }
})

const goBack = () => {
    router.go(-1) // 返回上一页
    // 或者 router.push({ name: 'List' })
}
</script>

<style scoped>
pre {
    background-color: #f4f4f4;
    border-radius: 4px;
    padding: 16px;
    overflow-x: auto;
    font-size: 0.9em;
}
</style>