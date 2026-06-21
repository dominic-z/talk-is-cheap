import { mount } from '@vue/test-utils'
import TodoApp from './TodoApp.vue'

//  需要引入test，抄的HelloWorld.spec.js
import { test,expect } from 'vitest'

// 简单的测试
test('renders a todo', async () => {
  const wrapper = mount(TodoApp)

  //测试文本内容
  // 获取todo这个element
  const todo = wrapper.get('[data-test="todo"]')
  expect(todo.text()).toBe('Learn Vue.js 3')


  // 测试按钮新增
  // 找到所有的满足条件具备属性data-test="todo"的element
  expect(wrapper.findAll('[data-test="todo"]')).toHaveLength(1)

  // 就像爬虫一样来设置内容和触发
  await wrapper.get('[data-test="new-todo"]').setValue('New todo')
  await wrapper.get('[data-test="form"]').trigger('submit')

  console.log(wrapper.findAll('[data-test="todo"]'))
  // 测试触发结果，看看结果是不是有两条了
  expect(wrapper.findAll('[data-test="todo"]')).toHaveLength(2)


  // 测试完成待办事项后会不会新增预期的class
  // 测试是否有成功加上completed的class
  await wrapper.get('[data-test="todo-checkbox"]').setValue(true)

  expect(wrapper.get('[data-test="todo"]').classes()).toContain('completed')
})