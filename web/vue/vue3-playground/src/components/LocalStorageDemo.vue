<script setup>

let testValue=null;
if (typeof(Storage) !== "undefined") {
    // 支持 localStorage
    localStorage.setItem('test', 'testValue');
    testValue = localStorage.getItem('test');
    
    console.log(testValue);
} else {
    // 不支持 localStorage
    console.log('Sorry, your browser does not support Web Storage...');
}
</script>

<template>
    <p>
        {{ testValue }}
    </p>

</template>