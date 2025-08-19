<!-- Nav.vue -->
<script>
import { ref } from 'vue'



export default {
    data() {
        return {
            admin: ref(false),
            shouldShowDropdown: ref(false)
        }
    }
}
// const admin = ref(false)

</script>

<template>
    <nav>
        <a id="profile" href="/profile">My Profile</a>
        <a v-if="admin" id="admin" href="/admin">Admin</a>

        <ul v-show="shouldShowDropdown" id="user-dropdown">
      <!-- dropdown content -->
    </ul>
    </nav>
</template>