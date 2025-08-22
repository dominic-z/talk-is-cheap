import './assets/main.css'

import { createApp,ref } from 'vue'
import { createPinia } from 'pinia'
import vuetify from './VuetifyConfiguration.js'



import App from './App.vue'
import router from './router'

const app = createApp(App)


app.use(createPinia())
app.use(router)
app.use(vuetify)

app.mount('#app')
