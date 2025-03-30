# Vue 3 + Vite

This template should help get you started developing with Vue 3 in Vite. The template uses Vue 3 `<script setup>` SFCs, check out the [script setup docs](https://v3.vuejs.org/api/sfc-script-setup.html#sfc-script-setup) to learn more.

Learn more about IDE Support for Vue in the [Vue Docs Scaling up Guide](https://vuejs.org/guide/scaling-up/tooling.html#ide-support).


# 构建

指定版本`nvm use v22.14.0`

```shell
cnpm create vite@latest
◇  Project name:
│  vue3-playground
│
◇  Select a framework:
│  Vue
│
◇  Select a variant:
│  JavaScript

```
下载依赖与启动服务
```shell
cnpm install
npm run dev
```

## 构建服务

`npm run dev`是开发环境使用的方法，要想真正启动一个服务，可以：

```
npm install -g http-server
npm run build
cd dist

http-server
```

就可以真正启动一个服务了。

# Vuex
详细学习：https://vuex.vuejs.org/zh/
## createStore

```shell
cnpm install vuex@4.1.0
```

# Axios

```shell
cnpm install axios
```

