<template>

    <RouterLink :to="{ name: 'profile', params: { username: 'erina' } }">
        User profile
    </RouterLink>

    <RouterView />

</template>