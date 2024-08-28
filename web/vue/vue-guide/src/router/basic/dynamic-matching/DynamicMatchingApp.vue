<template>
  <ul>
    <li>
      <router-link to="/users/eduardo/posts/1">/users/eduardo/posts/1</router-link>
    </li>
    <li>
      <router-link to="/users/eduardo/posts/20?plan=private">/users/eduardo/posts/20?plan=private</router-link>
    </li>
    <li>
      <router-link to="/users/aaaa">/users/aaaa</router-link>
    </li>
    <li>
      <router-link to="/users/user-aab">/users/user-aab</router-link>
    </li>
    <li>
      <router-link to="/somewhereelse">/somewhereelse</router-link>
    </li>
  </ul>
  <router-view></router-view>
</template>

<!-- <script>
  export default {
    name: "App",
  };
  </script> -->

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
}

ul {
  display: flex;
  list-style: none;
  padding: 0;
  margin: 0;
}

li:not(:last-of-type) {
  margin-right: 1rem;
}

.router-link-active {
  color: orange;
}

.router-link-exact-active {
  color: crimson;
}
</style>