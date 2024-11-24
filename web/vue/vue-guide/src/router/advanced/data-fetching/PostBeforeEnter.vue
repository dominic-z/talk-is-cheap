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

const props = defineProps(['post'])
console.log('props.post', props.post)

// 组合式api似乎没有很好的beforeEnter方法，
const route = useRoute()
console.log(route.query)
const post = ref(null)
const error = ref('error')
const loading = ref('loading')
post.value = route.query


</script>