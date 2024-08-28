<script setup>
import { reactive, computed } from 'vue'

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
</script>

<template>
  <p>Has published books: {{ author.books }}</p>
  <div>{{ publishedBooksMessage }}</div>
  <div>{{ simpleMessage }}</div>

  <button @click="addBook">addbook</button>
</template>