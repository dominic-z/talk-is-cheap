<script>
import { ref } from 'vue';
import ComponentA from './ComponentA.vue';

export default{
    components:{
        ComponentA
    },
    props:['foo'],
    setup(props){
        let a=ref('a')
        console.log(props)
    }
}
</script>

<template>

    <ComponentA></ComponentA>
</template>