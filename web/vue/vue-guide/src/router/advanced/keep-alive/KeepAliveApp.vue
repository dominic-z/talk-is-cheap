<!-- App.vue -->
<template>

  <!-- 来自 https://www.qianwen.com/share/chat/cebd9b8282f5425ebf3028b720bda86e -->
  <!-- http://localhost:5173/ -->
  <!-- 
    include: 指定要缓存的组件名称（对应 List.vue 中的 name 或 setup 隐含的名称）
    注意：在 <script setup> 中，文件名通常会自动作为组件名，
    但为了保险，建议在 List.vue 中显式定义 name: 'UserList'
  -->
  <!-- <router-view v-slot="{ Component, route }">
        <keep-alive include="List">
            <component :is="Component" :key="route.meta.key" />
        </keep-alive>
    </router-view> -->

  <!-- 我的实际情况，router-view指向了一个ListView，这个view里有一个<List>组件，这个list里面点击某个item跳转到一个新的路由ItemView里面，并且支持从新的路由跳转回来 -->
  <!-- 因此我外层就只写了router-view一个标签，我希望在view组件里面套一个keep-alive -->
  <!-- 因此我写了一个ListView并且里面写了一个keep-alive -->
  <!-- 但是这样写是不行的，我猜核心原因在于，当从ListView跳转到ItemView的时候，ListView里面的keepAlive也丢了 -->
  <!-- 而上面的例子不会这样，上面的写法中，keep-alive会一直存在 -->
  <!-- https://www.qianwen.com/share/chat/900182584ce444ac9b33c66cf2023003  这种方法其实也不太优雅，即使加上了include属性，也还是在全局范围上做了影响 -->
  <!-- https://www.qianwen.com/share/chat/c37a724c6c664509b57811ec8c030b60 keep-alive需要一直存活才能生效 但简单的router-view的跳转会导致上一个组件被销毁 -->
  <!-- <router-view>
    </router-view> -->


  <!-- http://localhost:5173/KeepAliveListView -->
  <!-- 一个更优的解决方案 -->
   <!-- 来自豆包 -->
  <router-view v-slot="{ Component }">
    <!-- 只有当路由的 meta.keepAlive 为 true 时，才缓存该组件，而且确保这个keep-alive一直存在，最多缓存5个 -->
    <keep-alive max="5">
      <component :is="Component" v-if="$route.meta.isKeepAlive"></component>
    </keep-alive>
    <!-- 不需要缓存的组件 -->
    <component :is="Component" v-if="!$route.meta.isKeepAlive"></component>
  </router-view>
</template>

<script setup>
import List from './List.vue';

// 主应用逻辑
</script>