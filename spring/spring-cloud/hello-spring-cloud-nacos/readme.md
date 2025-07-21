
# 文档
https://nacos.io/docs/latest/ecology/use-nacos-with-spring-cloud
nacos功能的统一文档

[github](https://github.com/alibaba/spring-cloud-alibaba)，spring-cloud-alibaba-dependencies是一个统一的版本控制mvn依赖，此项目基于spring-cloud-alibaba的使用，包括nacos+loadbalancer+sleuth+feign等

# nacos
## 部署

## docker standalone模式

```shell
docker run -p 8848:8848 -p 9848:9848 -p 9849:9849 --rm -it --name nacos -e MODE=standalone   goose-good/nacos/nacos-server:v2.4.3
```



# Feign

## 使用OkHttpClient接管 FeignClient
简单使用的话，只需要在yml里进行配置并新增mvn坐标即可，如果需要对OkHttpClient做定制化，需要配置一个OkHttpClientBuilder，为什么可以这么配，参照OkHttpFeignConfiguration