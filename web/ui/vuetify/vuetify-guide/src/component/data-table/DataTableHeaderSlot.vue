<template>
    <v-data-table :items="items">
        <!-- 控制id这个header的slot，只控制id这一列 -->
        <template v-slot:header.id="idHeader">
            {{ idHeader }}
            <span class="font-weight-bold">{{ idHeader.column.title.toUpperCase() }}</span>
        </template>
    </v-data-table>
</template>
<script setup>
const items = [
    {
        id: 1,
        name: 'T-Shirt',
        size: 'M',
        color: 'Red',
        price: 19.99,
        quantity: 10,
    },
    {
        id: 2,
        name: 'Jeans',
        size: '32',
        color: 'Blue',
        price: 49.99,
        quantity: 5,
    },
    {
        id: 3,
        name: 'Sweater',
        size: 'L',
        color: 'Green',
        price: 29.99,
        quantity: 7,
    },
    {
        id: 4,
        name: 'Jacket',
        size: 'XL',
        color: 'Black',
        price: 89.99,
        quantity: 3,
    },
    {
        id: 5,
        name: 'Socks',
        size: 'One Size',
        color: 'White',
        price: 9.99,
        quantity: 20,
    },
]
</script>