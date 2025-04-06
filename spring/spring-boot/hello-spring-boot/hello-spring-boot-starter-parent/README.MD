# Spring的spi机制


## ApplicationListener
- starting()：在 Spring 应用程序开始启动时调用，此时 Spring 容器尚未创建，可用于执行一些最开始的初始化操作，如记录启动开始日志。
- environmentPrepared(ConfigurableEnvironment environment)：当 Spring Boot 应用程序的环境准备好后调用，可在此方法中对应用的配置环境进行检查或修改。该方法在 Spring Boot 应用程序的环境准备好后被调用。此时，Spring Boot 已经加载了应用的配置属性，包括从application.properties或application.yml等配置文件中读取的内容，以及命令行参数、系统属性等，形成了一个ConfigurableEnvironment对象，开发者可以在这个方法中对应用的运行环境进行检查、修改或获取相关配置信息。
- contextPrepared(ConfigurableApplicationContext context)：在 Spring 应用上下文准备好但尚未加载 bean 定义时调用，可用于对应用上下文进行一些预处理，如添加自定义的 BeanFactoryPostProcessor。
- contextLoaded(ConfigurableApplicationContext context)：当 Spring 应用上下文已加载所有 bean 定义后调用，可用于在所有 bean 都加载完成后执行一些操作，如检查特定 bean 是否正确加载。
- finished(ConfigurableApplicationContext context, Throwable exception)：在 Spring 应用程序启动完成后调用，如果启动过程中发生异常，exception参数将包含相应的异常信息，可用于记录启动完成日志或处理启动异常。

## org.springframework.boot.env.EnvironmentPostProcessor
允许开发者在 Spring Boot 应用程序的环境创建和初始化之后，对ConfigurableEnvironment对象进行进一步的修改和定制，以便满足特定的应用需求，比如从自定义的配置源加载配置属性等。


# hello-spring-boot-starter-feign-client

## 将feign改为okhttp

```xml
<!--        okhttp-->
<dependencies>
    <dependency>
        <groupId>io.github.openfeign</groupId>
        <artifactId>feign-okhttp</artifactId>
    </dependency>

    <!--    负载均衡，用于服务发现-->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-loadbalancer</artifactId>
    </dependency>    
</dependencies>

```

## 负载均衡配置

1. 首先生成feign的okhttpClient`OkHttpFeignConfiguration`，下面这段不要粘贴进去了；
```java
@Bean
@ConditionalOnMissingBean({Client.class})
public Client feignClient(okhttp3.OkHttpClient client) {
    return new OkHttpClient(client);
}
```
2. 负载均衡配置`OkHttpFeignLoadBalancerConfiguration`，关键在`feignClient`这个配置，需要这个feign



