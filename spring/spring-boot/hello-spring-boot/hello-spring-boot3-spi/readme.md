# 说明

来自：
1. [重学SpringBoot3-SPI机制](https://cloud.tencent.com/developer/article/2459779)
2. [Spring boot接口扩展之SPI方式详解](https://blog.csdn.net/chyohn/article/details/141867430): 展示了各种spi插件
3. [Springboot3.0源码揭秘 -- BootstrapRegistryInitializer拓展点的作用及其应用](https://juejin.cn/post/7455942219166892042): 简单了解即可，一句话，就是在应用启动的时候的对容器做一些操作，例如在spring启动的最开始阶段，容器是空的，在这个时候希望预先创建一些对象，这些对象可以被放在bootstrapContext这个容器里，在后续容器正式开始启动的时候，这个容器会销毁
4. [SpringApplicationRunListener和ApplicationListener的区别](https://www.doubao.com/thread/w14fc022deb88db2f)
5. [EnvironmentPostProcessor](https://www.doubao.com/thread/wd2ddca98a6e29e30)
6. [spring boot通过@Bean注解定义一个Controller
   ](https://www.cnblogs.com/gaofeng-henu/p/12168789.html)：我们知道，无论是webmvc还是webflux中的RequestMappingHandlerMapping类，都是在afterPropertiesSet方法中查找所有带有Controller或者RequestMapping注解的类，再把对应类中的带有RequestMapping注解的方法解析后注册到对应的RequestMappingHandlerMapping中的，其中判断一个类是否带有@Controller或@RequestMapping的方法如下
7. [Springboot自定义Starter、配置读取和工具类构建](https://blog.csdn.net/oypebook/article/details/107421825)：starter默认不会读取starter项目中的application.yaml并进行处理，自然也不会生成对应的ConfigurationProperties对象了，搜索得知可以使用@EnableConfigurationProperties这个注解，但仍然需要结合EnvironmentPostProcessor手动读取一些配置文件，因为starter默认不会读取starter项目中的application.yaml并进行处理
8. 一些新想法：**如果没有必要，最好不要在starter项目里通过EnvironmentPostProcessor操作环境变量**，很可能对实际项目（例如本例中的example项目）中的配置造成影响（除非你的目的就是这个，例如覆盖修改实际项目的配置）。所以EnvironmentPostProcessor和PropertySourceLoader实际上应该都是服务于example项目的，而不应该服务于starter项目。我觉得，对于starter自用的配置项，得能够和依赖starter的项目的配置项隔离，所以更好的做法应该是：starter自用的配置不要放进容器里，starter手动读取某个配置文件（最好也不要叫application，应该自定义其他名称）并创建一个配置项的pojo对象，并应用与各种bean的创建；或者也可以放进容器里并通过ConfigurationProperties自动创建对象，但是名字需要严格隔离。
9. 测试的时候可以关注一下各种spi组件的触发时间，其实多数都是相对比较早的了。另一个知识：其实很多bean的创建都非常晚。配置文件的读取操作更靠前
10. 对于trigger这个模块，需要提前通过mvn compile编译出class，其他module才能使用其中的类
11. [getClassLoader().getResource路径查找的优先级](https://www.doubao.com/thread/w3c2ab54931119d89):在starter中读取文件，一般使用classloader的getResource，这涉及到路径查找的优先级问题:优先查找已解压的目录，再查找打包的 jar 包中的资源
12. 测试发现，starter中的各种listener，如果注册在spring.factories里，这些listener中如果需要持有其他bean，这些bean没法被自动注入
13. 对包名做了点特殊处理，避免三个项目位于同一个包名下，导致一些无法被发现的异常。