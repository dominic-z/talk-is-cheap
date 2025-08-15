
import './assets/main.css'

import { createApp } from 'vue'
import App from './App.vue'
import GettingStartedApp from '@/components/getting-started/GettingStartedApp.vue'
import GettingStartedCustomApp from '@/components/getting-started/GettingStartedCustomApp.vue'

// createApp(App).mount('#app')

// createApp(GettingStartedApp).mount("#app")
createApp(GettingStartedCustomApp).mount("#app")
