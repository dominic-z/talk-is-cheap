import { createStore } from 'vuex';

// 创建 store 实例
const store = createStore({
    state: {
        count: 1,
        numbers: [1, 2, 3, 4, 5]
    },
    mutations: {
        increment(state) {
            state.count++;
        }
    },
    actions: {
        incrementAsync(context) {
            setTimeout(() => {
                context.commit('increment');
            }, 1000);
        }
    },
    getters: {
        getCount(state) {
            return state.count;
        },
        evenNumbers(state) {
            return state.numbers.filter(num => num % 2 === 0);
        }
    }
});

export default store;  