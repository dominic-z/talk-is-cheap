# 说明

来自：
1. [重学SpringBoot3-SPI机制](https://cloud.tencent.com/developer/article/2459779)
2. [Spring boot接口扩展之SPI方式详解](https://blog.csdn.net/chyohn/article/details/141867430): 展示了各种spi插件
3. [Springboot3.0源码揭秘 -- BootstrapRegistryInitializer拓展点的作用及其应用](https://juejin.cn/post/7455942219166892042): 简单了解即可，一句话，就是在应用启动的时候的对容器做一些操作，例如在spring启动的最开始阶段，容器是空的，在这个时候希望预先创建一些对象，这些对象可以被放在bootstrapContext这个容器里，在后续容器正式开始启动的时候，这个容器会销毁
4. [SpringApplicationRunListener和ApplicationListener的区别](https://www.doubao.com/thread/w14fc022deb88db2f)
5. [EnvironmentPostProcessor](https://www.doubao.com/thread/wd2ddca98a6e29e30)

测试的时候可以关注一下各种spi组件的触发时间，其实多数都是相对比较早的了。

对于trigger这个模块，需要提前通过mvn compile编译出class，其他module才能使用其中的类