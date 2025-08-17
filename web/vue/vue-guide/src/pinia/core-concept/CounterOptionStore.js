import { defineStore } from "pinia";

export const useCounterOptionStore = defineStore('counter',{
    state: ()=>({count:0,name:'Eduardo'}),
    getters: {
        doubleCount: (state)=>state.count*2.1,
    },
    actions:{
        increment(){
            this.count = this.count+1
        }
    }
})