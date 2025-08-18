# 说明


官方文档的使用教程：https://test-utils.vuejs.org/zh/guide/

本项目通过vite创建工程，注意，选择依赖的时候，一定要带上vitest，vitest是一个测试框架，我简单理解为vitest来找到代码中的单元测试代码，然后调用/驱动这些单测代码，这些单测代码中调用vue-test-utils的能力测试渲染组件等功能。
```shell
npm create vite@latest  --registry=https://registry.npmmirror.com
```
具体执行上，可以看`package.json`中有`"test:unit": "vitest"`，那么就可以执行`npm run test:unit`来执行单测了。


## 如何启动一个单元测试
主要参考：https://www.doubao.com/thread/wedfa0f1aaa58d85c。默认情况下，vitest会查找满足下列规则的文件，将这些文件视作单元测试脚本，并且即使要只单测某几个文件，这个文件也要满足命名要求，例如TodoApp.test.js
```
include: **/*.{test,spec}.?(c|m)[jt]s?(x)
exclude:  **/node_modules/**, **/dist/**, **/cypress/**, **/.{idea,git,cache,output,temp}/**, **/{karma,rollup,webpack,vite,vitest,jest,ava,babel,nyc,cypress,tsup,build,eslint,prettier}.config.*, e2e/**
```
执行单元测试时`npm run test:unit`会触发所有单测，`npm run test:unit src/components/a-crash-course/TodoApp.test.js`会执行这个脚本中的单测