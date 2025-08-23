// 注意，./assets/main.css中引入了vueflow所需的css，所以其余组件中的style无需再引入vueflow的css
import './assets/main.css'
/*  需要定义在全局中 */


import { createApp } from 'vue'

import SimpleGraph from '@/guide/getting-started/SimpleGraph.vue'



import GettingStartedApp from '@/guide/getting-started/GettingStartedApp.vue'
import GettingStartedCustomApp from '@/guide/getting-started/GettingStartedCustomApp.vue'
import ThemingApp from '@/guide/theming/ThemingApp.vue'
import AddNodeApp from '@/guide/node/AddNodeApp.vue'
import RemoveNodeApp from '@/guide/node/RemoveNodeApp.vue'
import UpdateNodeUseVueFlowApp from '@/guide/node/update-node/UpdateNodeUseVueFlowApp.vue'

import UpdateNodeUseNodeApp from '@/guide/node/update-node/UpdateNodeUseNodeApp.vue'
import UpdateNodeVModelApp from '@/guide/node/update-node/UpdateNodeVModelApp.vue'
import NodeTypeApp from './guide/node/node-types/NodeTypeApp.vue'
import MultiVueFlowApp from './guide/node/MultiVueFlowApp.vue'
import NodeEventUseVueFlowApp from './guide/node/node-event/NodeEventUseVueFlowApp.vue'
import NodeEventComponentApp from './guide/node/node-event/NodeEventComponentApp.vue'

import ScrollingWithinNodeApp from './guide/node/scrolling-within-node/ScrollingWithinNodeApp.vue'
import PreventDragNodeApp from './guide/node/prevent-drag-node/PreventDragNodeApp.vue'
import AddEdgeApp from './guide/edge/AddEdgeApp.vue'
import RemoveEdgeApp from './guide/edge/RemoveEdgeApp.vue'
import UpdateEdgeApp from './guide/edge/UpdateEdgeApp.vue'
import EdgeTypeApp from './guide/edge/edge-type/EdgeTypeApp.vue'
import EdgeEventApp from './guide/edge/EdgeEventApp.vue'
import HandleApp from './guide/handle/HandleApp.vue'
import FlowChangeApp from './guide/controlled-flow/FlowChangeApp.vue'
import ValidatingChangesApp from './guide/controlled-flow/ValidatingChangesApp.vue'
import VModelNodesAndEdgesApp from './guide/controlled-flow/VModelNodesAndEdgesApp.vue'

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
// createApp(PreventDragNodeApp).mount('#app')

// createApp(AddEdgeApp).mount('#app')
// createApp(RemoveEdgeApp).mount('#app')
// createApp(UpdateEdgeApp).mount('#app')
// createApp(EdgeTypeApp).mount('#app')
// createApp(EdgeEventApp).mount('#app')
// createApp(HandleApp).mount('#app')

//  Composables的https://vueflow.dev/guide/composables.html就是一些可以操作flow和node的方法，用到再说。
// createApp(FlowChangeApp).mount('#app')
// createApp(ValidatingChangesApp).mount('#app')
createApp(VModelNodesAndEdgesApp).mount('#app')