<script setup>
// 计算属性，是一个基于某个响应式变量创建的变量，响应式变量变化的时候，会让这个计算属性的值也跟着响应式地变化
import { reactive, computed ,ref} from 'vue'

const author = reactive({
  name: 'John Doe',
  books: [

  ]
})

function addBook() {
  author.books.push('a new book')
}

// 一个计算属性 ref
const publishedBooksMessage = computed(() => {
  return author.books.length > 0 ? 'Yes' : 'No'
})

// auther.books.length的改变并不会影响simpleMessage
const simpleMessage = author.books.length > 0 ? 'Yes' : 'No'

// 不会正常工作，因为Date.now并不是一个响应时依赖，vue无法基于某个对象感知到数值已经变化了从而更新computed的输出数值
const now = computed(() => Date.now())
// 而下面这个就可以正常工作了，相当于author的变更能够成功触发now的重新计算
// const now = computed(() => author.books.length > 0 ? Date.now():Date.now())


const firstName = ref('John')
const lastName = ref('Doe')

const fullName = computed({
  // getter
  get() {
    return firstName.value + ' ' + lastName.value
  },
  // setter
  set(newValue) {
    // 注意：我们这里使用的是解构赋值语法
    console.log('setting')
    firstName.value = newValue.split(' ')[0]
    lastName.value = newValue.split(' ')[1]
    // 不知为啥,下面的解构赋值会报错
    // [firstName.value, lastName.value] = newValue.split(' ')
  }
})

function setNewName(value){
  
  // fullName.value = 'Dom z'
  // 等效,并且我认为这样更好,computed还是保持只读最好
  firstName.value = 'Dom'
  lastName.value = 'z'
}

</script>

<template>
  <p>Has published books: {{ author.books }}</p>
  <div>{{ publishedBooksMessage }}</div>
  <div>{{ simpleMessage }}</div>
  <div>{{ now }}</div>

  <button @click="addBook">addbook</button>

  <div>{{ fullName }}</div>
  <button @click="setNewName">setNewName</button>
</template>