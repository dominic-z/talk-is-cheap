import './assets/main.css'

import { createApp,ref } from 'vue'
import { createPinia } from 'pinia'
import vuetify from './vuetifyConfiguration.js'



import App from './App.vue'
import router from './router'
import ClusterView from './views/ClusterView.vue'

// const app = createApp(App)
const app = createApp(ClusterView)


app.use(createPinia())
app.use(router)
app.use(vuetify)

app.mount('#app')
