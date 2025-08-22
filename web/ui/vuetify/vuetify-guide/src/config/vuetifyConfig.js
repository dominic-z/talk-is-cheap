
// Vuetify
import { createVuetify } from 'vuetify';
import * as components from 'vuetify/components';
import * as directives from 'vuetify/directives';
import 'vuetify/styles';


import '@mdi/font/css/materialdesignicons.css'; // Ensure you are using css-loader


// 使用@mdi/css
// const vuetify = createVuetify({
//   components,
//   directives,
//   icons: {
//     defaultSet: 'mdi', // This is already the default value - only for display purposes
//   },
// })



// 使用@mdi/js
import { aliases, mdi } from 'vuetify/iconsets/mdi-svg'
const vuetify = createVuetify({
  components,
  directives,
  icons: {
    defaultSet: 'mdi', // This is already the default value - only for display purposes
    aliases,
    sets: {
      mdi,
    },
  },
})

export default vuetify