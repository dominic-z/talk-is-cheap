<script setup>
import { ref, watch, reactive, watchEffect } from 'vue'

const obj = reactive({ count: 0, someString: "a", someObj: { inner: 'inner' } })

// 像useEffect
watch(obj, (newValue, oldValue) => {
    console.log("update obj newValue === oldValue", newValue === oldValue)
},
    { immediate: true }
)
watch(() => obj.someString, (newValue, oldValue) => {
    console.log("update someString newValue === oldValue", newValue === oldValue)

    console.log("obj.someString", obj.someString)
})

watch(() => obj.someObj, (newValue, oldValue) => {
    console.log("newValue", newValue, "update someString newValue === oldValue", newValue === oldValue)

    console.log("obj.someObj", obj.someObj)
},
    // 如果不加deep，那么someObj本身的变更并不会触发侦听器
    { deep: true }
)


function updateCount() {
    obj.count++
}

function updateString() {
    obj.someString = obj.count + ""
}

function updateSomeObj() {
    obj.someObj.inner = obj.count + ""
}

const todoId = ref(1)
const data = ref(null)

watchEffect(
    async () => {
        const response = await fetch(
            `http://httpbin.org/anything/${todoId.value}`,
            {
                method: "GET",
                headers: {
                    accept: "application/json"
                }

            }
        )
        data.value = await response.json()
    }
)
</script>

<template>
    <button @click="updateCount">updateCount</button>
    <button @click="updateString">updateString</button>
    <div>
        <button @click="updateSomeObj">updateSomeObj</button>

        {{ obj }}
    </div>
    <div>
        <!-- 在template之中todoId会解包 -->
        <button @click="() => todoId += 1">updatetodoId</button>

        {{ data }}
    </div>
</template>