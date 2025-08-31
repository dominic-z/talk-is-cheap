<script setup>
const items = [
    {
        name: 'African Elephant',
        species: 'Loxodonta africana',
        diet: 'Herbivore',
        habitat: 'Savanna, Forests',
    },
    // ... more items
]
</script>

<template>

    <v-data-table :items="items"></v-data-table>
</template>