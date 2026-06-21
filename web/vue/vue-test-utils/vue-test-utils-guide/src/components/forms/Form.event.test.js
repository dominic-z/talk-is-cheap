import { mount } from '@vue/test-utils'

// 测试表单触发的事件信息
import Form from '@/components/forms/Form.vue'
import { expect, test } from 'vitest'

test('trigger', async () => {
  const wrapper = mount(Form)

  // 触发元素
  await wrapper.find('button').trigger('click')

  // 断言某个操作已执行，例如发出事件。
  expect(wrapper.emitted()).toHaveProperty('submit')
})