import { defineStore } from "pinia"
import { useOtherStore } from "./OtherStore"


// 用选项式写一次试试
export const useGetterStore = defineStore('getterStore', {
    state: () => ({
        count: 1,
        users: [
            { id: 1, name: 'aa' },
            { id: 2, name: 'bb' },
            { id: 3, name: 'cc' },
        ]
    }),
    getters: {
        doubleCount: (state) => state.count * 2,
        getUserById: (state) => {
            return (userId) => state.users.find((user) => user.id === userId)
        },
        withOtherStore: (state)=>{
            return state.count + useOtherStore().other
        }
    },
    actions: {
        increment(n) { this.count += n },
        addUser(id, name) {
            this.users.push({ id: id, name: name })
        }
    }
})