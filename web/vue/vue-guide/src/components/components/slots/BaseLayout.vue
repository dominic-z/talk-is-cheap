<script setup>
import { ref } from 'vue';


let greetingMessage = ref("greeting")
let count = ref(2)
</script>

<template>
    <div class="container">
        <header>
            <div v-if="$slots.header" class="card-header"  >
                <slot name="header" :message="'hello'"/>
            </div>
        </header>

        <main>
            <slot :text="greetingMessage" :count="count"></slot>
        </main>

        <footer>
            <slot name="footer"></slot>
        </footer>
    </div>
</template>

<style lang="css">
.card-header{
    border: 1px solid
}
</style>