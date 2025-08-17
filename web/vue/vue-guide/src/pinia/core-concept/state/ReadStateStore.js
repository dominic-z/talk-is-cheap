import { mapState } from 'pinia'
import {useStateStore} from './StateStore.js'

//  mapstate有啥用我也不清楚,没有看懂这是用来做什么的.
const readStateStore = {
  computed: {
    // 可以访问组件中的 this.count
    // 与从 store.count 中读取的数据相同
    ...mapState(useStateStore, ['count']),
    // 与上述相同，但将其注册为 this.myOwnName
    ...mapState(useStateStore, {
      myOwnName: 'count',
      // 你也可以写一个函数来获得对 store 的访问权
      double: store => store.count * 2,
      // 它可以访问 `this`，但它没有标注类型...
      magicValue(store) {
        console.log(store.doubleAge , this.count() , this.double())
        return store.doubleAge + this.count() + this.double()
      },
    }),
  },
}

console.log(readStateStore)
export default readStateStore