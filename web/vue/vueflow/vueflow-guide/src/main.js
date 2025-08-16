// 注意，./assets/main.css中引入了vueflow所需的css，所以其余组件中的style无需再引入vueflow的css
import './assets/main.css'
/*  需要定义在全局中 */


import { createApp } from 'vue'
import App from './App.vue'

import SimpleGraph from '@/components/getting-started/SimpleGraph.vue'



import GettingStartedApp from '@/components/getting-started/GettingStartedApp.vue'
import GettingStartedCustomApp from '@/components/getting-started/GettingStartedCustomApp.vue'
import ThemingApp from '@/components/theming/ThemingApp.vue'
import AddNodeApp from '@/components/node/AddNodeApp.vue'
import RemoveNodeApp from '@/components/node/RemoveNodeApp.vue'
import UpdateNodeUseVueFlowApp from '@/components/node/update-node/UpdateNodeUseVueFlowApp.vue'

import UpdateNodeUseNodeApp from '@/components/node/update-node/UpdateNodeUseNodeApp.vue'
import UpdateNodeVModelApp from '@/components/node/update-node/UpdateNodeVModelApp.vue'
import NodeTypeApp from './components/node/node-types/NodeTypeApp.vue'
import MultiVueFlowApp from './components/node/MultiVueFlowApp.vue'
import NodeEventUseVueFlowApp from './components/node/node-event/NodeEventUseVueFlowApp.vue'
import NodeEventComponentApp from './components/node/node-event/NodeEventComponentApp.vue'

import ScrollingWithinNodeApp from './components/node/scrolling-within-node/ScrollingWithinNodeApp.vue'
import PreventDragNodeApp from './components/node/prevent-drag-node/PreventDragNodeApp.vue'

// createApp(App).mount('#app')

// createApp(SimpleGraph).mount("#app")
// createApp(GettingStartedApp).mount("#app")
// createApp(GettingStartedCustomApp).mount("#app")

// createApp(ThemingApp).mount('#app')
// createApp(AddNodeApp).mount('#app')
// createApp(RemoveNodeApp).mount('#app')
// createApp(UpdateNodeUseVueFlowApp).mount('#app')
// createApp(UpdateNodeUseNodeApp).mount('#app')
// createApp(UpdateNodeVModelApp).mount('#app')
// createApp(NodeTypeApp).mount('#app')
// createApp(NodeEventUseVueFlowApp).mount('#app')
// createApp(NodeEventComponentApp).mount('#app')
// createApp(MultiVueFlowApp).mount('#app')
// createApp(ScrollingWithinNodeApp).mount('#app')
createApp(PreventDragNodeApp).mount('#app')