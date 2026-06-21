<script setup>
import { ref } from 'vue';
import FancyButton from './FancyButton.vue';
import BaseLayout from './BaseLayout.vue';

let message = ref("msg")
</script>

<template>
    <span>{{ message }}</span>
    <FancyButton>
        {{ message }}
    </FancyButton>

    <FancyButton>
    </FancyButton>


    <BaseLayout>
        <template v-slot:header="headerProps">
            <h1>Here might be a page title</h1>
            <div>{{ headerProps }}</div>
        </template>

        <template v-slot="slotProps">
            <p>A paragraph for the main content.</p>
            <p>And another one.</p>
            <!-- 下面的数据来自于BaseLayout -->
            <p>{{ slotProps.text }} {{ slotProps.count }}</p>
        </template>

        <template #footer>
            <p>Here's some contact info</p>
        </template>
    </BaseLayout>
</template>