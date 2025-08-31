import './assets/main.css'

import { createPinia } from 'pinia'
import { createApp } from 'vue'
import vuetify from './vuetifyConfiguration.js'
import JsonViewer from 'vue-json-viewer'


import App from './App.vue'
import router from './router'

const app = createApp(App)
// const app = createApp(GlobalNav)


app.use(createPinia())
app.use(router)
app.use(vuetify)
app.use(JsonViewer)

app.mount('#app')
