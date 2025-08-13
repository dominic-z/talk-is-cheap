import { mapWritableState } from 'pinia'
import { useStateStore } from './StateStore.js'

const writableStateStore = {
  computed: {
    // 可以访问组件中的 this.count，并允许设置它。
    // this.count++
    // 与从 store.count 中读取的数据相同
    ...mapWritableState(useStateStore, ['count']),
    // 与上述相同，但将其注册为 this.myOwnName
    ...mapWritableState(useStateStore, {
      myOwnName: 'count',
    }),
  },
}

console.log(writableStateStore)

export default writableStateStore