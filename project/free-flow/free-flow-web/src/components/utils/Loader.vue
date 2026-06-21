<script setup>
import { ref, watch } from 'vue'

const {overlay} = defineProps(['overlay'])
</script>

<template>
    <div class="text-center">
        <v-overlay :model-value="overlay" class="align-center justify-center">
            <v-progress-circular color="primary" size="64" indeterminate></v-progress-circular>
        </v-overlay>
    </div>
</template>
