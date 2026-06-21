<template>

    <RouterLink to="/user/erina" >
        User
    </RouterLink>
    <RouterLink to="/user/erina/role/admin" active-class="my-router-link-active">
        Role
    </RouterLink>

    <RouterView />
</template>

<style lang="css" scoped>
.my-router-link-active {
    color: skyblue;
}

</style>