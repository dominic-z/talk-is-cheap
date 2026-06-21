# 环境搭建

需要一个nacos，抄自nacos的示例
```shell
docker run -p 8080:8080 -p 8848:8848 -p 9848:9848 -p 9849:9849 \
    -e NACOS_AUTH_ENABLE=true \
    -e NACOS_AUTH_TOKEN=VGhpc0lzTXlDdXN0b21TZWNyZXRLZXkwMTIzNDU2Nzg5IUA= \
    -e NACOS_AUTH_IDENTITY_KEY=serverIdentity \
    -e NACOS_AUTH_IDENTITY_VALUE=security \
  --rm -it --name nacos -e MODE=standalone   goose-good/nacos/nacos-server:v3.0.2

```


代码来自：
1. [Spring Cloud LoadBalancer 简单介绍与实战](https://blog.csdn.net/weixin_59216829/article/details/134592899)
2. [Spring Cloud LoadBalancer 负载均衡策略与缓存机制](https://blog.csdn.net/xaiobit_hl/article/details/134283093)：默认的loadbalancer的配置来自于LoadBalancerClientConfiguration
3. [Spring Cloud LoadBalancer 详解](https://blog.csdn.net/ZhShH0413/article/details/149432502)：没怎么看，还是重复的内容
4. https://docs.spring.io/spring-cloud-commons/docs/current/reference/html/#zone-based-load-balancing
5. 