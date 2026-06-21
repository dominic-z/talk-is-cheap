<script setup>
import { useGetterStore } from './GetterStore';

const getterStore = useGetterStore()
</script>

<template>

    <div>
        getterStore
        <div>
            <button @click="()=>getterStore.increment(2)">getterStore.increment</button>
            <button @click="()=>getterStore.addUser(99,'jojo')">getterStore.addUser</button>
        </div>
        <p>Double count is {{ getterStore.doubleCount }}</p>
        <p>withOtherStore is {{ getterStore.withOtherStore }}</p>
        <p>Double useres is {{ getterStore.users }}</p>
        <p> getUserById(2): {{ getterStore.getUserById(2) }}</p>
        <p> getUserById(99): {{ getterStore.getUserById(99) }}</p>
    </div>

</template>