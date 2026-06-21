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
            <RouterLink to="/pname">/pname</RouterLink>
        </li>
        <li>
            <RouterLink to="/">/</RouterLink>
        </li>
        <li>
            <RouterLink to="/repeat/1/2/3">/repeat/1/2/3</RouterLink>
        </li>
        <li>
            <RouterLink to="/repeat/a/b">//repeat/a/b</RouterLink>
        </li>
    </ul>

    <RouterView></RouterView>
</template>