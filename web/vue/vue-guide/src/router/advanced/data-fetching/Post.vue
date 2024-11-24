<template>
    <div>
        <div v-if="loading">Loading...</div>


        <div v-if="error" class="error">{{ error }}</div>

        <div v-if="post" class="content">
            <h2>{{ post.title }}</h2>
            <p>{{ post.body }}</p>
        </div>
    </div>
</template>

<script setup>
import { ref, watch } from 'vue';
import { useRoute } from 'vue-router';


const route = useRoute()
const loading = ref(false)
const post = ref(null)
const error = ref(null)

watch(() => route.params.id, fetchData, { immediate: true })

async function fetchData(id) {
    console.log(id)
    error.value = post.value = null
    loading.value = true

    try {
        post.value = await getPost(id)
    } catch (err) {
        error.value = err.toString()
    } finally {
        loading.value = false
    }
}

async function getPost(id) {
    return new Promise((resolve, reject) => {
        setTimeout(resolve, 500)
    }).then(() => { return { title: id, body: "111" } })
}


</script>