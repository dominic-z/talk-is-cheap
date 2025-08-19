import CustomTextAreaApp from '@/components/forms/CustomTextAreaApp.vue'
import { mount } from '@vue/test-utils'

// 测试可输入vue组件的findComponent，运用了模板引用加上findComponent查找特定的组件
// 根据文档摸索出来的使用方法
import { expect, test } from 'vitest'

test('emits textarea value on submit', async () => {
  const wrapper = mount(CustomTextAreaApp)
  const description = 'Some very long text...'

  const comp = wrapper.findComponent({ ref: "customTextAreaRef" })
  console.log("comp", comp)

  await comp.setValue(description)

  wrapper.find('form').trigger('submit')

  const events = wrapper.emitted('submitted')
  console.log("events", events)
  expect(events[0][0]).toEqual(description)
})