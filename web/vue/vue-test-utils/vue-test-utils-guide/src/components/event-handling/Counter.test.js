import { mount } from '@vue/test-utils'
// 测试触发并捕获事件
//  需要引入test，抄的HelloWorld.spec.js
import Counter from '@/components/event-handling/Counter.vue'
import { expect, test } from 'vitest'


test('emits an event when clicked', () => {
  const wrapper = mount(Counter)

  wrapper.find('button').trigger('click')
  wrapper.find('button').trigger('click')

  expect(wrapper.emitted()).toHaveProperty('increment')


  // `emitted()` 接受一个参数。它返回一个包含所有 `this.$emit('increment')` 发生情况的数组。
  const incrementEvent = wrapper.emitted('increment')
  console.log(incrementEvent)

  // 我们“点击”了两次，所以 `increment` 的数组应该有两个值。
  expect(incrementEvent).toHaveLength(2)

  // 断言第一次点击的结果。
  // 注意，值是一个数组。
  expect(incrementEvent[0]).toEqual([1])

  // 然后是第二次的结果。
  expect(incrementEvent[1]).toEqual([2])


  // 复杂事件，其实就是事件的参数就是个对象
  wrapper.find('#complexEventButton').trigger('click')
  wrapper.find('#complexEventButton').trigger('click')

  // 我们“点击”了两次，因此 `increment` 的数组应该有两个值。
  expect(wrapper.emitted('complexIncrement')).toHaveLength(2)

  // 然后，我们可以确保 `wrapper.emitted('increment')` 的每个元素包含一个带有预期对象的数组。
  expect(wrapper.emitted('complexIncrement')[0]).toEqual([
    {
      count: 3,
      isEven: false
    }
  ])

  expect(wrapper.emitted('complexIncrement')[1]).toEqual([
    {
      count: 4,
      isEven: true
    }
  ])
})