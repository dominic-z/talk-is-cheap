# 说明

vuetify的使用，官网有些地方写的并不是特别清晰，所以单独做个project来演示

# 安装

## 构建vue项目
下面这样就够了。
```shell
npm create vite@latest  --registry=https://registry.npmmirror.com

◇  Select a framework:
│  Vue
│
◇  Select a variant:
│  Official Vue Starter ↗
```

## 安装vuetify
安装vuetify，我参照的是 [现有项目安装vuetify](https://vuetifyjs.com/zh-Hans/getting-started/installation/#section-73b06709987976ee)，分为两步：
安装vuetify以及
```shell
cnpm i vuetify

```

## 安装font与icon
对应的样式和icon，vuetify本身没提供icon，需要依赖另一些开源的图标库，mdi图标,参考[图标字体](https://vuetifyjs.com/zh-Hans/features/icon-fonts/)
```shell
cnpm install @mdi/font -D
```
其实到这里，mdi里这些icon就可以使用了，例如
```vue
<v-icon>mdi-triangle</v-icon>
<v-icon icon="mdi-account-cowboy-hat"></v-icon>
```
mdi的所有icon可以在[MDI](https://pictogrammers.com/library/mdi/icon/)进行搜索，使用方式很简单，就是将名字按照kebab-case填写在icon属性中，参考参考HelloVuetifyMdiJS.vue

### MDI - JS SVG
文章中还提到了另一种引入icon的方式，[参考](https://vuetifyjs.com/zh-Hans/features/icon-fonts/#mdi-css)
> This is the recommended installation when optimizing your application for production, as only icons used for Vuetify components internally will be imported into your application bundle. You will need to provide your own icons for the rest of the app.

```shell
cnpm install @mdi/js -D

```

安装后，如果要使用里面的icon，使用的方式改变了，首先createVuetify的参数要发生改变，详见[vuetifyConfig.js](./src/config/vuetifyConfig.js)，参考HelloVuetifyMdiJS.vue，上面那种kebab-case的形式使用icon就不灵了。后续都会以这种方式来使用icon，而非@font/font


我理解`@mdi/font`是通过css的形式引入icon，而`@mdi/js`是通过js的形式引入icon，[区别](https://www.doubao.com/thread/w6076e23113f55dcf)


# component


# styles
vuetify里内置了很多常用的class，有需要直接从里面捞就可以，不需要自己再写麻烦的css