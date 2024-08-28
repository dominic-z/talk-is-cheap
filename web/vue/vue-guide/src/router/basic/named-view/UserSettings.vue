<template>
    <!-- 访问http://localhost:5173/settings -->
    <div>
        <RouterLink to="/settings">/settings</RouterLink>
    </div>

    <div>
        <RouterLink to="/settings/emails">/settings/emails</RouterLink>
    </div>

    <div>
        <RouterLink to="/settings/profile">/settings/profile</RouterLink>
    </div>

    <div>
        <RouterLink to="/settings/profile/withAvatar">/settings/profile/withAvatar</RouterLink>
    </div>

    <div>
        <h1>User Setting</h1>
        <RouterView />
        <RouterView name="helper" />
    </div>
</template>