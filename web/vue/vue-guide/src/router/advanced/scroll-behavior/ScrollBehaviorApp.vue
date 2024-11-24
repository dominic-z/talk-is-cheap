<template>

    <div id="anchor" :style="{ height: '700px', width: '1400px', backgroundColor: 'green' }">
        it's a anchor
    </div>

    <RouterView v-slot="{ Component }">

        <div id="div" :style="{ height: '700px', width: '1400px', backgroundColor: 'gray' }">
            <component :is="Component" />
        </div>
    </RouterView>

    <RouterLink to="/user/erina">
        User
    </RouterLink>

    <RouterLink to="/anchor">
        anchor
    </RouterLink>
</template>