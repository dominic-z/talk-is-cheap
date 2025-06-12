配合https://blog.csdn.net/leah126/article/details/141624726 针对cors的demo

以及https://cloud.tencent.com/developer/article/1953193

# 项目
## 前端项目

```shell 
nvm list
nvm use v18.20.4
```
使用vue+axios来进行请求发送
```shell
npm create vite@latest

✔ Project name: … hello-cors-frontend
✔ Select a framework: › Vue
✔ Select a variant: › JavaScript

  cd hello-cors-frontend
  npm install
  npm run dev
```


安装axios，用cnpm也行
```shell
npm install axios
```

## 后端项目
就是一个spring-web


## 运维解法

用一个nginx挡在前端服务和后端服务的前面，在nginx里配置代理路由。浏览器直接访问的是nginx服务。
例如当浏览器访问\api的url的时候，路由至后端，否则均路由至前端，这样的话对于浏览器来说，他只知道自己访问的是同一个地址的服务（即nginx所在的地址），测试成功过，但是配置有点麻烦，先略了