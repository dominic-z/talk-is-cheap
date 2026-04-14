<template>
  <!-- :items="serverItems"用来指明这个列表使用什么对象作为items  -->
   <!-- v-model:items-per-page通过v-model来将itemsPerPage拿出来，同样的v-model:page也可以将这个组件的page捞出来 -->
    <!-- item-vale的意思是使用item里的name字段作为表格里每个项目的值 -->
    <v-data-table-server
      v-model:items-per-page="itemsPerPage"
      :headers="headers"
      :items="serverItems" 
      :items-length="totalItems"
      :loading="loading"
      item-value="name" 
      @update:options="loadItems"
    ></v-data-table-server>
  </template>
  <script setup>
    import { ref } from 'vue'
  
    const cars = [
      {
        name: 'Ford Mustang',
        horsepower: 450,
        fuel: 'Gasoline',
        origin: 'USA',
        price: 55000,
        
      },
      {
        name: 'Tesla Model S',
        horsepower: 670,
        fuel: 'Electric',
        origin: 'USA',
        price: 79999,
      },
      {
        name: 'BMW M3',
        horsepower: 503,
        fuel: 'Gasoline',
        origin: 'Germany',
        price: 70000,
      },
      {
        name: 'Audi RS6',
        horsepower: 591,
        fuel: 'Gasoline',
        origin: 'Germany',
        price: 109000,
      },
      {
        name: 'Chevrolet Camaro',
        horsepower: 650,
        fuel: 'Gasoline',
        origin: 'USA',
        price: 62000,
      },
      {
        name: 'Porsche 911',
        horsepower: 379,
        fuel: 'Gasoline',
        origin: 'Germany',
        price: 101000,
      },
      {
        name: 'Jaguar F-Type',
        horsepower: 575,
        fuel: 'Gasoline',
        origin: 'UK',
        price: 61000,
      },
      {
        name: 'Mazda MX-5',
        horsepower: 181,
        fuel: 'Gasoline',
        origin: 'Japan',
        price: 26000,
      },
      {
        name: 'Nissan GT-R',
        horsepower: 565,
        fuel: 'Gasoline',
        origin: 'Japan',
        price: 113540,
      },
      {
        name: 'Mercedes-AMG GT',
        horsepower: 523,
        fuel: 'Gasoline',
        origin: 'Germany',
        price: 115900,
      },
    ]
  
    const FakeAPI = {
      async fetch ({ page, itemsPerPage, sortBy }) {
        return new Promise(resolve => {
          setTimeout(() => {
            const start = (page - 1) * itemsPerPage
            const end = start + itemsPerPage
            const items = cars.slice()
            if (sortBy.length) {
              const sortKey = sortBy[0].key
              const sortOrder = sortBy[0].order
              items.sort((a, b) => {
                const aValue = a[sortKey]
                const bValue = b[sortKey]
                return sortOrder === 'desc' ? bValue - aValue : aValue - bValue
              })
            }
            const paginated = items.slice(start, end)
            resolve({ items: paginated, total: items.length })
          }, 500)
        })
      },
    }
    const itemsPerPage = ref(5)
    const headers = ref([
      { title: 'Car Model', key: 'name', align: 'start', headerProps: {class: 'font-weight-bold'}}, 
      // headerProps代表，传递给默认的header的属性，这些属性会直接挂载表格的header组件属性上 ，参考自https://vuetifyjs.com/en/components/data-tables/basics/#keys-and-values “Other options are available for setting width, align, fixed, or pass custom props to the header element with headerProps and row cells with cellProps.”
      // key属性代表着使用key指定的字段去items对象里找到对应的属性，例如这一行key是name，那么就要去Car Model这一列就需要对每个item里找item.name作为这一列的数据，
      { title: 'Horsepower', key: 'horsepower', align: 'end' },
      { title: 'Fuel Type', key: 'fuel', align: 'start' },
      { title: 'Origin', key: 'origin', align: 'start' },
      { title: 'Price ($)', key: 'price', align: 'end' },
    ])
    const serverItems = ref([])
    const loading = ref(true)
    const totalItems = ref(0)
    function loadItems ({ page, itemsPerPage, sortBy }) {
      loading.value = true
      FakeAPI.fetch({ page, itemsPerPage, sortBy }).then(({ items, total }) => {
        serverItems.value = items
        totalItems.value = total
        loading.value = false
      })
    }
  </script>