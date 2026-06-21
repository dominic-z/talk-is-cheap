import { mount } from '@vue/test-utils'

import Form from '@/components/forms/Form.vue'
import { expect, test } from 'vitest'
// 单测向input组件setValue以及触发submit提交
test('emits the input to its parent', async () => {
  const wrapper = mount(Form)

  // 设置值
  await wrapper.find('input').setValue('my@mail.com')

  // 触发元素
  await wrapper.find('button').trigger('click')

  // 断言 `submit` 事件被发出
  const event =wrapper.emitted('submit')
  console.log(event)
  expect(event[0][0]).toBe('my@mail.com')
})