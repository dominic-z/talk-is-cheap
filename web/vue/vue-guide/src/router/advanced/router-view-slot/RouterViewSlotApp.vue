<template>

    <RouterLink to="/user/erina">
        User
    </RouterLink>

    <RouterView v-slot="{ Component }">

        <div :style="{ backgroundColor: 'black' }">
            <component :is="Component" />
        </div>
    </RouterView>
</template>