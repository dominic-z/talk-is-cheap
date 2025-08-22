// 配置vuetify，来自
// https://vuetifyjs.com/zh-Hans/getting-started/installation/#section-73b06709987976ee
// 

// Vuetify
import 'vuetify/styles'
import { createVuetify } from 'vuetify'
import * as components from 'vuetify/components'
import * as directives from 'vuetify/directives'

// 引入icon
import '@mdi/font/css/materialdesignicons.css' // Ensure you are using css-loader
import { useDisplay } from 'vuetify'

import { aliases, mdi } from 'vuetify/iconsets/mdi-svg'

const vuetify = createVuetify({
  components,
  directives,
  icons: { // 引入icon
    defaultSet: 'mdi', // This is already the default value - only for display purposes
    aliases,
    sets: {
      mdi,
    },
  },

  // 来自https://www.doubao.com/thread/wc07fda06bc2c5808，但是指引是错的，看个思路
  // 来自https://next.vuetifyjs.com/zh-Hans/features/display-and-platform/#section-5173952e70b959275c0f548c9608503c
  // 很好理解 ，下列配置的含义，sm大小及以下的设备是为移动端，会影响全局的useDiplay().mobile
  display: {
    mobileBreakpoint: 'sm',
  }
})

console.log(vuetify.display.mobileBreakpoint)

export default vuetify