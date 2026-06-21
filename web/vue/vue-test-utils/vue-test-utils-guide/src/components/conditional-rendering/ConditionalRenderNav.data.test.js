import ConditionalRenderNav from '@/components/conditional-rendering/ConditionalRenderNav.vue'
import { mount } from '@vue/test-utils'
// 测试通过data覆盖数据

import { test, expect } from 'vitest'


// 测试通过data选项覆盖数据
test('renders a profile link', () => {


  // 如果希望通过这种方式覆盖数据，那么这个组件必须是选项式的，参考：
  // https://juejin.cn/post/7238541391654731833
  // VTU 的功能也有限，比如我们无法使用包裹器方法 setData 来修改 <script setup> 中的响应式数据。
  const wrapper1 = mount(ConditionalRenderNav, {
    data() {
      return {
        admin: true
      }
    }
  })

  console.log(wrapper1.element)

  // 同样，使用 `get()` 时我们隐式地断言了元素存在。
  expect(wrapper1.get('#admin').text()).toEqual('Admin')
})