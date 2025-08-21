import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'

// Vuetify
import 'vuetify/styles'
import { createVuetify } from 'vuetify'
import * as components from 'vuetify/components'
import * as directives from 'vuetify/directives'
// 引入icon
import '@mdi/font/css/materialdesignicons.css' // Ensure you are using css-loader

import { aliases, mdi } from 'vuetify/iconsets/mdi-svg'

import App from './App.vue'
import router from './router'
import ClusterView from './views/ClusterView.vue'

// const app = createApp(App)
const app = createApp(ClusterView)


app.use(createPinia())
app.use(router)
app.use(createVuetify({
  components,
  directives,
  icons: { // 引入icon
    defaultSet: 'mdi', // This is already the default value - only for display purposes
    aliases,
    sets: {
      mdi,
    },
  },
  
}))

app.mount('#app')
