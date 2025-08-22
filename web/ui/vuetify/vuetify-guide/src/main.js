import { createApp } from 'vue'

import vuetify from './config/vuetifyConfig'
import HelloVuetifyMdiFont from './HelloVuetifyMdiFont.vue'
import HelloVuetifyMdiJS from './HelloVuetifyMdiJS.vue'
import HelloVCard from './component/v-card/HelloVCard.vue'
import HelloVCardSlot from './component/v-card/HelloVCardSlot.vue'

// const app = createApp(HelloVuetifyMdiFont)
// const app = createApp(HelloVuetifyMdiJS)
// const app = createApp(HelloVCard)
const app = createApp(HelloVCardSlot)
    
app.use(vuetify)
app.mount('#app')
