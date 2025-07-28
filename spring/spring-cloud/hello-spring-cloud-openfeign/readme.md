# feign
教材来自：
1. [SpringBoot 使用 Feign 无废话 All-in-one 指南](https://juejin.cn/post/7169549885723639838)
2. [SpringCloud实用-OpenFeign整合okHttp](https://blog.csdn.net/FBB360JAVA/article/details/134600211)
3. OkHttpFeignConfiguration源码：OpenFeign整合OkHttp的核心就在这个配置类里，很好读，可以通过这个配置类对OkHttpClient进行定制化，也可以看到如何在application.yaml里进行配置（即FeignHttpClientProperties）
4. [Feign实现动态URL](https://www.cnblogs.com/nuccch/p/15893833.html)：在Feign中能实现动态URL的基础是框架本身就支持，只需要在接口方法中包含一个java.net.URI参数，Feign就会将该参数值作为目标主机地址，详见Interface Annotations一节中的“Overriding the Request Line”部分。
   如下将分别阐述独立使用Feign和使用Spring Cloud OpenFeign实现定义统一的回调方法。
5. [feign调用接口报错No qualifying bean of type '***HttpMessageConverters' available](https://www.cnblogs.com/luyifo/articles/18152429):需要多点配置，直接抄



有两个模块
1. backend-service: 提供后端服务
2. frontend-service: 提供前端服务，通过feign调用backend-service