# feign
教材来自：
1. [SpringBoot 使用 Feign 无废话 All-in-one 指南](https://juejin.cn/post/7169549885723639838)
2. [SpringCloud实用-OpenFeign整合okHttp](https://blog.csdn.net/FBB360JAVA/article/details/134600211)
3. OkHttpFeignConfiguration源码：OpenFeign整合OkHttp的核心就在这个配置类里，很好读，可以通过这个配置类对OkHttpClient进行定制化，也可以看到如何在application.yaml里进行配置（即FeignHttpClientProperties）


有两个模块
1. backend-service: 提供后端服务
2. frontend-service: 提供前端服务，通过feign调用backend-service