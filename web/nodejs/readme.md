# npm与nvm
Node.js 拥有一个名为 npm（Node Package Manager）的生态系统，其中有丰富的第三方库和模块，可以用来扩展功能和加速开发。，node有一堆版本，就像java有各种版本一样。nvm就是允许你电脑上安装多个node版本，并且支持自由切换。

感觉，node就是一个可以运行js的解释器。就像python解释器和python的关系一样，而vue/react都是前端服务的框架，在某个端口部署一个前端服务，然后通过这个服务返回前端代码。

# 使用nvm安装某个版本
```shell

nvm install v22.14.0
nvm use v22.14.0

# 装个cnpm 默认使用阿里镜像，很快
npm install -g cnpm --registry=https://registry.npmmirror.com

```

