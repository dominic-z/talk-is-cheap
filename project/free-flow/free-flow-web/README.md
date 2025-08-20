# free-flow-web

This template should help get you started developing with Vue 3 in Vite.

## Recommended IDE Setup

[VSCode](https://code.visualstudio.com/) + [Volar](https://marketplace.visualstudio.com/items?itemName=Vue.volar) (and disable Vetur).

## Customize configuration

See [Vite Configuration Reference](https://vite.dev/config/).

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

### Run Unit Tests with [Vitest](https://vitest.dev/)

```sh
npm run test:unit
```


## 安装包

```shell
cnpm install vuetify

```

安装icon，vuetify本身没icon，但是在[图标字体](https://vuetifyjs.com/zh-Hans/features/icon-fonts/)提到了他默认兼容4个常见icon集，比如使用material design icon(简称mdi)
```shell
cnpm install @mdi/font
```

并且在createVuetify中引入，详见官网或者main.js，引入之后，`<v-app-bar-nav-icon></v-app-bar-nav-icon>`的图标才能展示出来。

# 功能

## 节点查询

简单的列表展示就行

1. scheduler查询查询：当前的scheduler节点情况，包括状态/ip/上线时间。
2. worker集群查询：当前的worker节点情况，包括状态/ip/上线时间。

## 任务定义查询

简单的列表查询，包括

1. 全部的任务定义情况
2. 支持按照任务名称/版本/是否存在可运行的worker/worker节点的过滤
3. 支持详情查看：展示任务图定义。
4. 支持从任务定义跳转查看某个任务的具体执行情况

## 任务执行查询

简单的列表查询，包括

1. 所有任务的启动记录，支持查看每次任务执行的尝试情况
2. 