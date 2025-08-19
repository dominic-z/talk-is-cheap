import { mount } from '@vue/test-utils'

// 测试触发复杂的事件，比如keydown.meta.c.exact.prevent
import CaptureCopyInput from '@/components/forms/CaptureCopyInput.vue'
import { expect, test } from 'vitest'

test('handles complex events', async () => {
  const wrapper = mount(CaptureCopyInput)

  await wrapper.find('input').trigger('keydown.meta.c.exact.prevent')
  

  // 断言某个操作已执行，例如发出事件。
  const event = wrapper.emitted();
  console.log(event)
  expect(event).toHaveProperty('keydown')
  console.log(event.keydown[0])
})