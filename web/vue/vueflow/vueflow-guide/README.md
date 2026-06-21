# vueflow-guide

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

# 构建项目

来自：https://vueflow.dev/guide/getting-started.html

```shell
npm create vite@latest  --registry=https://registry.npmmirror.com

cnpm install @vue-flow/core
cnpm install @vue-flow/background @vue-flow/controls @vue-flow/minimap
cnpm install @vue-flow/node-resizer

```


自动布局
```shell
cnpm install @dagrejs/dagre
```

# 怎么看vue-flow的api

以“在edge上新增箭头”为需求目标，发现example中的edge有markEnd这个字段，例如
```json
{
    id: 'e2a-6',
    source: '2a',
    target: '6',
    label: () => h(CustomEdgeLabel, { label: 'custom label text' }),
    labelStyle: { fill: '#10b981', fontWeight: 700 },
    markerEnd: MarkerType.Arrow,
  }
```

可以查文档：[Edge](https://vueflow.dev/typedocs/type-aliases/Edge.html)，发现他的类型可以是[DefaultEdge](https://vueflow.dev/typedocs/interfaces/DefaultEdge.html)，发现里面有markerEnd这个属性，其类型是[EdgeMarkerType](https://vueflow.dev/typedocs/type-aliases/EdgeMarkerType.html)