import { mount } from '@vue/test-utils'
// 单测向input组件setValue修改input的数值
import Form from '@/components/forms/Form.vue'
import { expect, test } from 'vitest'

test('sets the value', async () => {
  const wrapper = mount(Form)
  const input = wrapper.find('input')

  await input.setValue('my@mail.com')

  expect(input.element.value).toBe('my@mail.com')
})