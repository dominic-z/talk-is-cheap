# gateway

来自：
1. [4. Spring Cloud Gateway 入门与使用](https://blog.csdn.net/m0_63571404/article/details/145829543)
2. [SpringCloud gateway （史上最全](https://www.cnblogs.com/crazymakercircle/p/11704077.html)
3. [RequestRateLimiter GatewayFilter 工厂](https://docs.springframework.org.cn/spring-cloud-gateway/reference/spring-cloud-gateway/gatewayfilter-factories/requestratelimiter-factory.html)
4. [Spring Cloud 网关 Spring Cloud Gateway ](https://blog.csdn.net/qq_34417433/article/details/135144279)

与其他springcloud融合的项目暂未考虑

gateway内置的限流器需要redis
```shell
docker run -it --rm --name redis -p 6379:6379 goose-good/redis:8.0.3 --requirepass "123456" 
docker exec -it redis redis-cli -a 123456


# 限流的时候就会有这个
127.0.0.1:6379> keys *
1) "request_rate_limiter.{aaa}.tokens"
2) "request_rate_limiter.{aaa}.timestamp"

```