import { createApp } from 'vue'
import App from './HelloVueJsonViewer.vue'
import JsonViewer from 'vue-json-viewer'
import 'vue-json-viewer/style.css'


createApp(App)
    .use(JsonViewer)
    .mount('#app')
