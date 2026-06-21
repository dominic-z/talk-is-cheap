<script setup>

import { ref } from 'vue';
import Child from './Child.vue';
import VModelChild from './VModelChild.vue';

let foo = ref("foo")

let firstName = ref("dom")
let lastName = ref("z")

</script>
<template>

  <div>{{ foo }}</div>
<!-- Parent.vue -->
<Child
  :modelValue="foo"
  @update:modelValue="$event => {(foo = $event);console.log($event)}"
/>

<hr />
<div>
  {{ firstName }} , {{ lastName }}
</div>
<VModelChild v-model:first-name="firstName" v-model:last-name="lastName"></VModelChild>
</template>