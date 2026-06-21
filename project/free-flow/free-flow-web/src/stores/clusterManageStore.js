import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

export const useClusterManageStore = defineStore('clusterManageStore', () => {
  const expand = ref(false)
  
  function changeExpand() {
    expand.value = !expand.value
  }

  return { expand, changeExpand }
})
