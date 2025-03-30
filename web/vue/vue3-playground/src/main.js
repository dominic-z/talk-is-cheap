import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import store from './vuex/store';
import StoreDemo from './components/StoreDemo.vue';
import AxiosDemo from '@components/AxiosDemo.vue';
import LocalStorageDemo from '@components/LocalStorageDemo.vue';
import HelloWorld from './components/HelloWorld.vue';


// const app = createApp(App);
// const app = createApp(StoreDemo);
const app = createApp(AxiosDemo);

// const app = createApp(LocalStorageDemo);

// 全局变量，所有组件都可以通过myGlobal访问
// app.config.globalProperties.$myGlobal = "myGlobal"
// const app = createApp(HelloWorld);

// store也称为了全局变量，所有组件也可以通过this.$store访问，未测试过
app.use(store);
app.mount('#app')
