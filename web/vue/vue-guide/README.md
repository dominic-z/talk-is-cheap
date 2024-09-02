# 安装说明

```shell

# 还特意升级了下node，之前用的是node16，和@latest的vue不兼容，导致install失败了，把node_modules和package-lock.json删了之后重新升级了node再执行install
nvm install 18
nvm use 18

npm create vue@latest 
# 当前版本3.4.29
cd ${projectname}
npm install --registry=http://registry.npmmirror.com
npm run dev

```
# 基础
## 创建一个应用
`index.html`的`<script type="module" src="/src/main.js"></script>`引用了`/src/main.js`，而这个js将`App`这个vue组件挂载进了`index.html`中的`<div id='app'>`之中，于是一个页面就构建起来了。

### 挂载应用
#### DOM中的根组件模板

html+js如下所示的话，vue会将`#app`内部的内容（`innerHtml`）当做`template`模板进行构建，直观上来说，其实就是由vue接管这个`button`并管理`count`。
```html
<!-- index.html -->
<div id="app">
  <button @click="count++">{{ count }}</button>
</div>
```
```js
// main.js
import { createApp } from 'vue'
const app = createApp({
  data() {
    return {
      count: 0
    }
  }
})
app.mount('#app')
```
不过此时还不行，运行起来会看到控制台有一串报错。
> [Vue warn]: Component provided template option but runtime compilation is not supported in this build of Vue. Configure your bundler to alias "vue" to "vue/dist/vue.esm-bundler.js". 
  at <App>

百度了下，需要在`vite.config.js`里加上`vue: 'vue/dist/vue.esm-bundler.js'`，详见[Vue3动态编译失败](https://www.cnblogs.com/dongling/p/18091751)


## 响应式基础

什么是“解包”，先看下面两端代码以及引用guide中对解包的描述。“解包”的作用是：首先`count`并不是简单的`0`，而是一个`refImpl`对象，而这个`0`是存储于`count.value`中的，但是在下方html中，却可以直接通过`{{count}}`的方式来访问`0`。
因此“解包”其实代表着vue通过某种方式对对象进行解析，在不需要手动指定（例如不需要写明`count.value`）的前提下，直接将对象中某些部分拿出来使用，就像下面的`{{count}}`，vue对count进行了解包，直接将其中的value掏出来作为这个组件标签中的值
```js
import { ref } from 'vue'
export default {
  setup() {
    const count = ref(0)
    console.log(ref)
    return {
      count
    }
  }
}
```
```html
<div>{{ count }}</div>
```
> 在模板中使用 ref 时，我们不需要附加 .value。为了方便起见，当在模板中使用时，ref 会自动解包


## 列表渲染
不太喜欢vue的一点出现了，trick太多

## 模板引用
有点像react中的组件变量，用一个变量来指向一个dom元素，例如下面，这个`input`变量就代表`<input>`这个标签了
```js
<input ref="input">
```

# 深入组件
## 透传Attribute

> #当一个组件以单个元素为根作渲染时#，透传的 attribute 会自动被添加到根元素上。

## 插槽


> 插槽内容无法访问子组件的数据。Vue 模板中的表达式只能访问其定义时所处的作用域，这和 JavaScript 的词法作用域规则是一致的。

以下为例，父组件调用了子组件，并且子组件中有插槽，用于接收父组件传来的内容，插槽没法访问子组件的数据。这个也很直观 ，毕竟对于子组件，并不知道父组件传来的是啥。（这方面真的不如react）
```vue
<!-- 子组件FancyButton -->
<!-- 插槽内容 -->
<button class="fancy-btn">
  <slot></slot> <!-- 插槽出口 -->
</button>

<!-- 父组件 -->
<FancyButton>
  Click me! 
</FancyButton>
```


> 然而在某些场景下插槽的内容可能想要同时使用父组件域内和子组件域内的数据。要做到这一点，我们需要一种方法来让子组件在渲染时将一部分数据提供给插槽。

相当于提供了一种方法，让父组件能够读取子组件中插槽上配置的数据，这些数据可以用于配置父组件传下来的用于插槽的内容

###  高级列表组件示例

这个示例体现了slot的作用，即子组件提供了个骨架，然后父组件提供了向骨架中填充的血肉，而这个“血肉”其实也是一种模板（所以具名插槽使用`template`），以下面为例，子组件只知道要渲染一些列表，但是不知道渲染什么。这时候父组件传递一个模板进去，告诉每个插槽按照这个方式来渲染。（至于为什么使用v-bind，其实就是将参数打包成一个对象作为参数，详见[使用一个对象绑定多个prop](https://cn.vuejs.org/guide/components/props.html#binding-multiple-properties-using-an-object)）

```vue
<FancyList :api-url="url" :per-page="10">
  <template #item="{ body, username, likes }">
    <div class="item">
      <p>{{ body }}</p>
      <p>by {{ username }} | {{ likes }} likes</p>
    </div>
  </template>
</FancyList>
<!-- 子组件，slot部分等待父组件来填充 -->
<ul>
  <li v-for="item in items">
      <!-- 也可以写成 -->
    <!--   <slot name="item" :body="item.body" :username="item.username" :likes="item.likes"/> -->
    <slot name="item" v-bind="item"></slot>
  </li>
</ul>
```

# 逻辑复用

## 组合式函数

> 你可能已经注意到了，我们一直在组合式函数中使用 `ref()` 而不是 `reactive()`。我们推荐的约定是组合式函数始终返回一个包含多个 ref 的普通的非响应式对象，这样该对象在组件中被解构为 ref 之后仍可以保持响应性：

也试了试返回reactive对象，但是没发现什么有出啥问题。





# 内置组件

## Transition

这个东西挺好，内置的过渡组件

### [深层级过渡与显式过渡时长](https://cn.vuejs.org/guide/built-ins/transition.html#nested-transitions-and-explicit-transition-durations)

`<Transition :duration="550">...</Transition>`的意思就是整体过渡的事件是550ms，因为过渡结束后需要将组件移除，或者生成，这个时间用于显式告诉组件这个过渡事件多长，从而实现在所有组件过渡完成后安全地卸载或者生成。可以直接看教程中的例子，如果将`:duration="550"`删除，那么生成动画会有问题，（但是移除郭晨该不会，我觉得一个可能的原因是，"默认情况下，`<Transition>` 组件会通过监听过渡根元素上的**第一个** `transitionend` 或者 `animationend` 事件来尝试自动判断过渡何时结束。"，可以开F12看element，移除过程中，外部的div等待内部的div卸载后才完成动画，而生成过程中，内外两个div是同时启动生产的。）

# vue-router4

## 安装
```shell
npm install vue-router@4
```

## 路由

看这个例子大概能理解路由是什么，点击不同链接跳转到不同的页面，就是路由。



## 基础

### 入门

`<RouterLink>`编译完就是一个`<a>`

### 动态路由匹配



#### 捕获所有路由或 404 Not found 路由

```vue
router.push({
  name: 'NotFound',
  // 保留当前路径并删除第一个字符，以避免目标 URL 以 `//` 开头。
  params: { pathMatch: this.$route.path.substring(1).split('/') },
  // 保留现有的查询和 hash 值，如果有的话
  query: route.query,
  hash: route.hash,
})
```

没看懂这是用来干啥的，先遗留问题。





### 命名路由

其实就是让`RouterLink`定向走name所指定的路由

### 编程式导航

我发现编程式导航会改变当前页面的url，而`<RouterLink>`却不会。



### 路由组件传参

```vue
<template>
<!-- 直接访问/user/1 -->

    <RouterView />
</template>
```



写router的时候不小心把

```js
const passingPropsRouter = createRouter({
  history: createWebHistory(),
  routes,
})
```

写成了

```js
const passingPropsRouter = createRouter({
  history: createMemoryHistory(),
  routes,
})
```

问题：然后发现直接访问/user/1不好使了，必须要写`<RouterLink>`来做跳转，不知道为啥

# Example

https://cn.vuejs.org/examples/#hello-world


# vue-guide

This template should help get you started developing with Vue 3 in Vite.

## Recommended IDE Setup

[VSCode](https://code.visualstudio.com/) + [Volar](https://marketplace.visualstudio.com/items?itemName=Vue.volar) (and disable Vetur).

## Customize configuration

See [Vite Configuration Reference](https://vitejs.dev/config/).

## Project Setup

```sh
npm install
```

### Compile and Hot-Reload for Development

```sh
npm run dev
```

### Compile and Minify for Production

```sh
npm run build
```

### Lint with [ESLint](https://eslint.org/)

```sh
npm run lint
```
