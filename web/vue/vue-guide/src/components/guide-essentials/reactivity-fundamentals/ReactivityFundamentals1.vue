<script>
import { ref } from 'vue'
export default {
    setup() {
        const count = ref(0)

        console.log(count)
        console.log(count.value)

        count.value++
        console.log(count.value)

        function increment() {
            // 在 JavaScript 中需要 .value
            count.value++
        }

        return {
            count,
            increment
        }
    }
}
</script>


<template>

    <div>{{ count }}</div>
    <button @click="increment">
        {{ count }}
    </button>
</template>