<script setup>
import { computed } from 'vue'
import { RouterLink } from 'vue-router'

defineOptions({
    inheritAttrs: false,
})

const props = defineProps({
    // 如果使用 TypeScript，请添加 @ts-ignore
    ...RouterLink.props,
    inactiveClass: String,
})

const isExternalLink = computed(() => {
    return typeof props.to === 'string' && props.to.startsWith('http')
})
</script>

<template>
    <a v-if="isExternalLink" v-bind="$attrs" :href="to" target="_blank">
        <slot></slot>
    </a>
    <router-link v-else v-bind="$props" custom v-slot="{ isActive, href, navigate }">
        <a v-bind="$attrs" :href="href" @click="navigate"
            :class="isActive ? 'router-link-active router-link-exact-active' : inactiveClass">
            <slot></slot>

        </a>
    </router-link>
</template>