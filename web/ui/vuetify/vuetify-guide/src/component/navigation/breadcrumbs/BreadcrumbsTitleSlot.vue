<script setup>
  const items = [
    {
      title: 'Dashboard',
      disabled: false,
      href: 'breadcrumbs_dashboard',
    },
    {
      title: 'Link 1',
      disabled: false,
      href: 'breadcrumbs_link_1',
    },
    {
      title: 'Link 2',
      disabled: true,
      href: 'breadcrumbs_link_2',
    },
  ]

  // 通过这个可以解释一下slot怎么用
  // api中有描述
//   {
//   item: Partial<LinkProps> & { title: string; disabled: boolean }
//   index: number
// }
// 那么v-slot:title="{ item }"相当于在这个slot里你可以使用上述item这个对象了
</script>
<template>
    <v-breadcrumbs :items="items">
      <template v-slot:title="{ item }">
        {{ item.title.toUpperCase() }}
      </template>
    </v-breadcrumbs>
  </template>