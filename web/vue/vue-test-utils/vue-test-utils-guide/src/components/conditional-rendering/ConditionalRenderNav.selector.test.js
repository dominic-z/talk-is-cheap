import ConditionalRenderNav from '@/components/conditional-rendering/ConditionalRenderNav.vue'
import { mount } from '@vue/test-utils'
// 测试条件渲染并测试某个组件是否存在

//  需要引入test，抄的HelloWorld.spec.js
import { test, expect } from 'vitest'


test('renders a profile link', () => {
  const wrapper = mount(ConditionalRenderNav)

  // 这里我们隐式断言 #profile 元素存在。
  const profileLink = wrapper.get('#profile')

  expect(profileLink.text()).toEqual('My Profile')


  // 使用 `wrapper.get` 会抛出错误并导致测试失败。
  expect(wrapper.find('#admin').exists()).toBe(false)


})