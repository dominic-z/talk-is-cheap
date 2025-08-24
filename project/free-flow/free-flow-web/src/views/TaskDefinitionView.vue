<script setup>

// import HomeNav from '@/views/HomeNav.vue';
import GlobalNav from '@/components/nav/IndexNav.vue';
import TaskDefinitionDataTable from '@/components/task-definition/TaskDefinitionDataTable.vue';


import { mdiMagnify } from '@mdi/js';
import { ref } from 'vue';

const expand = ref(false)
const nodeType = ref('Scheduler')
function updateTab(e) {
  // console.log(e)
  // console.log(nodeType.value)
}

</script>

<template>
  <VMain>
    <TaskDefinitionDataTable></TaskDefinitionDataTable>
  </VMain>
</template>
