import ConditionalRenderNav from '@/components/conditional-rendering/ConditionalRenderNav.vue'
import { mount } from '@vue/test-utils'
import { ref } from 'vue'


// 单测过程中，获取对应的dom对象
//  需要引入test，抄的HelloWorld.spec.js
import { test, expect } from 'vitest'


test('does not show the user dropdown', () => {
  const wrapper = mount(ConditionalRenderNav)

  expect(wrapper.get('#user-dropdown').isVisible()).toBe(false)
})