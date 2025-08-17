import { defineStore } from "pinia";
import { computed, ref } from "vue";

export const useOtherStore = defineStore('otherStore', () => {
  
  const other = ref(33)

  return { other }
})