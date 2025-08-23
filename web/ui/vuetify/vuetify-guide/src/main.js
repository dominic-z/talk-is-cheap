import { createApp } from 'vue'

import vuetify from './config/vuetifyConfig'
import HelloVuetifyMdiFont from './HelloVuetifyMdiFont.vue'
import HelloVuetifyMdiJS from './HelloVuetifyMdiJS.vue'
import HelloVCard from './component/v-card/HelloVCard.vue'
import HelloVCardSlot from './component/v-card/HelloVCardSlot.vue'
import HelloDataTableServer from './component/data-table/HelloDataTableServer.vue'
import DataTableHeaderSlot from './component/data-table/DataTableHeaderSlot.vue'
import StaticPosition from './styles/position/StaticPosition.vue'

// const app = createApp(HelloVuetifyMdiFont)
// const app = createApp(HelloVuetifyMdiJS)
// const app = createApp(HelloVCard)
// const app = createApp(HelloVCardSlot)
// const app = createApp(HelloDataTableServer)
// const app = createApp(DataTableHeaderSlot)

const app = createApp(StaticPosition)
app.use(vuetify)
app.mount('#app')
