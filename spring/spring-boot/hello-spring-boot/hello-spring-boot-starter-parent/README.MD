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

