import { mount } from '@vue/test-utils'

// 测试可输入vue组件的setValue操作，本质上也是找到真正的input
import CustomInputApp from '@/components/forms/CustomInputApp.vue'
import { expect, test } from 'vitest'

test('fills in the form', async () => {
  const wrapper = mount(CustomInputApp)
  const input = wrapper.find('.text-input input')
  await input.setValue('text')

  // 继续进行断言或操作，例如提交表单、断言 DOM 等…
  expect(input.element.value).toBe('text')
})