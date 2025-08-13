
import { computed, ref } from "vue";
import { defineStore } from "pinia";


export const useStateStore = defineStore('stateStore', () => {
    const count = ref(1)
    const name = ref('DIO')
    const age = ref(19)
    const ids = ref([])
    const doubleAge = computed(() => age.value * 2.1)
    function $reset() {
        count.value = 1
        name.value = 'DIO'
        age.value = 10
        ids.value = []
    }

    function update() {
        count.value += 1
        name.value += "vs JOJO"
        age.value += 2
    }

    return { ids,count, name, age, doubleAge, $reset, update }

})