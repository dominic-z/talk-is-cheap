<script setup>
defineEmits(['enlarge-text'])
const props = defineProps(['title', 'otherContent','obj','someName'])
console.log(props)


function updateObj(){
    props.obj.text+="1"
}

function updateName(){
    // 报错
    // Set operation on key "someName" failed: target is readonly
    props.someName+="1"
}
</script>

<!-- <script>
export default {
    props: ['title'],
    setup(props) {
        console.log(props.title)
    }
}
</script> -->

<template>
    <h4>{{ title }}</h4>
    <button @click="$emit('enlarge-text')">Enlarge Text</button>
    <button @click="updateName">updateName {{ props.someName }}</button>
    <button @click="updateObj">updateObj {{ props.obj }}</button>

    <slot></slot>
</template>