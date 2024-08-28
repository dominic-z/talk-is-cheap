<template>
    <!-- 这个组件是主app组件的话，如果直接访问http://localhost:5173/，那么下面的RouterView不会显示任何东西 -->
    <!-- 这是因为router中没有找到http://localhost:5173/这样一条路由 -->
    <div>App main page</div>

    <!-- 
    访问http://localhost:5173/user/eduardo/profile，命中了/user/:id这条route，那么该RouterView会呈现User这个组件，
    而User这个组件里刚好还有一个RouterView，这个User里的RouterView会在/user/:id的children里找能够匹配的route
    刚好能匹配到profile这个route，因此User里的RouterView会显示UserProfile
     -->

    <!-- 如果访问http://localhost:5173/user/eduardo，router中有这样一条指向USer的路由 
    然后命中了“”这个route 
    -->

    <RouterView />
</template>