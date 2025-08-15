
import './assets/main.css'

import { createApp } from 'vue'
import App from './App.vue'
import GettingStartedApp from '@/components/getting-started/GettingStartedApp.vue'
import GettingStartedCustomApp from '@/components/getting-started/GettingStartedCustomApp.vue'
import ThemingApp from '@/components/theming/ThemingApp.vue'
import AddNodeApp from '@/components/node/AddNodeApp.vue'
import RemoveNodeApp from '@/components/node/RemoveNodeApp.vue'
import UpdateNodeUseVueFlowApp from '@/components/node/update-node/UpdateNodeUseVueFlowApp.vue'
import SimpleGraph from '@/components/getting-started/SimpleGraph.vue'
import UpdateNodeUseNodeApp from '@/components/node/update-node/UpdateNodeUseNodeApp.vue'

// createApp(App).mount('#app')

// createApp(SimpleGraph).mount("#app")
// createApp(GettingStartedApp).mount("#app")
// createApp(GettingStartedCustomApp).mount("#app")

// createApp(ThemingApp).mount('#app')
// createApp(AddNodeApp).mount('#app')
// createApp(RemoveNodeApp).mount('#app')
// createApp(UpdateNodeUseVueFlowApp).mount('#app')
createApp(UpdateNodeUseNodeApp).mount('#app')
