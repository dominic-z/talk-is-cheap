<script setup>

import {ref} from 'vue'

const navItems = ref([
    {
        title: "集群信息",
        routeName: "cluster-manage"
    },
    {
        title: "任务定义",
        routeName: "task-definition"
    },
    {
        title: "任务执行信息",
        routeName: "task-startup"
    },
])

</script>

<template>
    <v-navigation-drawer>
        <v-list  nav>
            <v-list-item v-for="item in navItems" :title = "item.title" class="text-h3" link></v-list-item>
        </v-list>
    </v-navigation-drawer>

</template>