<template>

    <ul>
        <li>
            <RouterLink to="/o/1/#blahblah">/o/1/#blahblah</RouterLink>
        </li>

        <li>
            <RouterLink to="/p/name">/p/name</RouterLink>
        </li>

        <li>
            <RouterLink to="/1">/1/#blahblah</RouterLink>
        </li>

        <li>
            <RouterLink to="/name">/pname</RouterLink>
        </li>
        <li>
            <RouterLink to="/repeat/1/2/3">/repeat/a/b/c</RouterLink>
        </li>
        <li>
            <RouterLink to="/repeat/a/b">//repeat/a/b</RouterLink>
        </li>
    </ul>

    <RouterView></RouterView>
</template>