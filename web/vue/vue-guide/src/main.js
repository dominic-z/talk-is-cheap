import './assets/main.css'

import { createApp } from 'vue'
import List from './components/guide-essentials/List.vue'
import Forms from './components/guide-essentials/Forms.vue'
import EventHandling from './components/guide-essentials/EventHandling.vue'
import Lifecycle from './components/guide-essentials/Lifecycle.vue'
import Watchers1 from './components/guide-essentials/Watchers1.vue'
import Watchers2 from './components/guide-essentials/Watchers2.vue'
import TemplateSyntax from './components/guide-essentials/TemplateSyntax.vue'
import TemplateRefs from './components/guide-essentials/TemplateRefs.vue'
import ComponentBasic from './components/guide-essentials/ComponentBasic.vue'
import ClassAndStyle from './components/guide-essentials/ClassAndStyle.vue'
import DynamicComponent from './components/guide-essentials/DynamicComponent.vue'
import FetchingData from './components/examples/FetchingData.vue'
import Parent from './components/components/v-model/Parent.vue'
import Attr from './components/components/attrs/Attr.vue'
import Provider from './components/components/provide-inject/Provider.vue'
import ReactivityFundamentals1 from './components/guide-essentials/ReactivityFundamentals1.vue'
import ReactivityFundamentals2 from './components/guide-essentials/ReactivityFundamentals2.vue'
import Composable from './components/reusability/Composable.vue'
import RouterApp from './router/basic/RouterApp.vue'
// import router from './router/basic/router'
import DynamicMatchingApp from './router/basic/dynamic-matching/DynamicMatchingApp.vue'
import { dmRouter } from './router/basic/dynamic-matching/DMRouter'
import RouteMatchingSyntaxApp from './router/basic/route-matching-syntax/RouteMatchingSyntaxApp.vue'
import { routeMatchingRouter } from './router/basic/route-matching-syntax/RouteMatchingRouter'
import NestedRoutesApp from './router/basic/nested-routes/NestedRoutesApp.vue'
import { nestedRouter } from './router/basic/nested-routes/NestedRouter'
import NavigationApp from './router/basic/navigation/NavigationApp.vue'
import { navigationRouter } from './router/basic/navigation/NavigationRouter'
import router from './router/basic/router'
import UserSettings from './router/basic/named-view/UserSettings.vue'
import { namedViewRouter } from './router/basic/named-view/NamedViewRouter'
import NamedViewApp from './router/basic/named-view/NamedViewApp.vue'
import RedirectAliasApp from './router/basic/redirect-alias/RedirectAliasApp.vue'
import { RedirectAliasRouter } from './router/basic/redirect-alias/RedirectAliasRouter'
import PassingPropsApp from './router/basic/passing-props/PassingPropsApp.vue'
import passingPropsRouter from './router/basic/passing-props/PassingPropsRouter'
// createApp(App).mount('#app')

// DOM 中的根组件模板
createApp({
  data() {
    return {
      count: 0
    }
  }
}).mount('#counter')


// createApp(TemplateSyntax).mount('#myApp')

// createApp(ReactivityFundamentals2).mount('#myApp')
// createApp(Computed).mount('#myApp')
// createApp(ClassAndStyle).mount('#myApp')
// createApp(Conditional).mount('#myApp')

// createApp(List).mount('#myApp')
// createApp(EventHandling).mount('#myApp')
// createApp(Forms).mount('#myApp')
// createApp(Lifecycle).mount('#myApp')
// createApp(Watchers1).mount('#myApp')
// createApp(Watchers2).mount('#myApp')
// createApp(TemplateRefs).mount('#myApp')
// createApp(ComponentBasic).mount('#myApp')
// createApp(DynamicComponent).mount("#myApp")

// createApp(FetchingData).mount("#myApp")
// createApp(Parent).mount("#myApp")
// createApp(Attr).mount("#myApp")
// createApp(Provider).mount("#myApp")

// createApp(Composable).mount("#myApp")

// createApp(RouterApp).use(router).mount("#myApp")
// createApp(DynamicMatchingApp).use(dmRouter).mount("#myApp")
// createApp(RouteMatchingSyntaxApp).use(routeMatchingRouter).mount("#myApp")
// createApp(NestedRoutesApp).use(nestedRouter).mount("#myApp")
// createApp(NavigationApp).use(navigationRouter).mount("#myApp")
// createApp(NamedViewApp).use(namedViewRouter).mount("#myApp")
createApp(RedirectAliasApp).use(RedirectAliasRouter).mount("#myApp")
// createApp(PassingPropsApp).use(passingPropsRouter).mount("#myApp")
