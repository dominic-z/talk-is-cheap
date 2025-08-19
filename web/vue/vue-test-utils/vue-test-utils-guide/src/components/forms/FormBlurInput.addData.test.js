import { mount } from '@vue/test-utils'
import FormComponent from './FormComponent.vue'
import { expect, test } from 'vitest'
import FormBlurInput from '@/components/forms/FormBlurInput.vue'

// 测试提交，并且尝试向提交的事件中额外新增数据，这个例子里就是向按钮新增了relatedTarget数据
test('emits an event only if you lose focus to a button', () => {
  const wrapper = mount(FormBlurInput)

  const componentToGetFocus = wrapper.find('button')

  // 在blur的event上新增一些数据，这样这个event就会具备event.relatedTarget
  wrapper.find('input').trigger('blur', {
    relatedTarget: componentToGetFocus.element
  })

  expect(wrapper.emitted('focus-lost')).toBeTruthy()



  // 再次触发blur事件，但是这次不传递relatedTarget，那么事件其实也就还是只有一个，不会新增事件
  wrapper.find('input').trigger('blur')
  expect(wrapper.emitted('focus-lost')).toHaveLength(1)
})