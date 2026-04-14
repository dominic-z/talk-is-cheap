import { createApp } from 'vue'

import vuetify from './config/vuetifyConfig'
import HelloVuetifyMdiFont from './HelloVuetifyMdiFont.vue'
import HelloVuetifyMdiJS from './HelloVuetifyMdiJS.vue'
import HelloVCard from './component/v-card/HelloVCard.vue'
import HelloVCardSlot from './component/v-card/HelloVCardSlot.vue'
import HelloDataTableServer from './component/data-table/HelloDataTableServer.vue'
import DataTableHeaderSlot from './component/data-table/DataTableHeaderSlot.vue'
import StaticPosition from './styles/position/StaticPosition.vue'
import VFadeTransitionApp from '@/styles/transitions/VFadeTransitionApp.vue'
import RailNavApp from './component/navigation/v-navigation-drawer/RailNavApp.vue'
import ManualExpansionRailNavApp from '@/component/navigation/v-navigation-drawer/ManualExpansionRailNavApp.vue'
import HelloIntesect from './directives/intersect/HelloIntesect.vue'
import InfiniteScroll from './directives/intersect/InfiniteScroll.vue'
import HelloDataTable from './component/data-table/HelloDataTable.vue'
import VListApp from './component/list/VListApp.vue'
import AlignTabs from './component/navigation/tabs/AlignTabs.vue'
import BreadcrumbsTitleSlot from './component/navigation/breadcrumbs/BreadcrumbsTitleSlot.vue'

// const app = createApp(HelloVuetifyMdiFont)
// const app = createApp(HelloVuetifyMdiJS)
// const app = createApp(HelloVCard)
// const app = createApp(HelloVCardSlot)
// const app = createApp(HelloDataTableServer)
// const app = createApp(HelloDataTable)
// const app = createApp(DataTableHeaderSlot)
// const app = createApp(RailNavApp)
// const app = createApp(ManualExpansionRailNavApp)
// const app = createApp(VListApp)
// const app = createApp(AlignTabs)
const app = createApp(BreadcrumbsTitleSlot)



// styles
// const app = createApp(StaticPosition)
// transition
// const app = createApp(VFadeTransitionApp)


// directive
// const app = createApp(HelloIntesect)
// const app = createApp(InfiniteScroll)

app.use(vuetify)
app.mount('#app')
