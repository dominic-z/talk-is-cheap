
# 说明
nacos的使用说明

在[官网](https://nacos.io/docs/latest/ecology/use-nacos-with-spring-cloud)查看最新版本
在[github](https://github.com/alibaba/spring-cloud-alibaba)看版本对应情况，spring-cloud-alibaba与springboot/springcloud有对应的版本要求，spring-cloud-alibaba-dependencies是一个统一的版本控制mvn依赖，此项目基于spring-cloud-alibaba的使用，包括nacos+loadbalancer+sleuth+feign等

nacos功能的统一文档

# nacos
## 部署

## docker standalone模式
参考https://github.com/nacos-group/nacos-docker/tree/master

相关docker的环境变量配置参考了：[权限教研](https://nacos.io/docs/latest/manual/admin/auth/)和[Nacos docker](https://nacos.io/docs/latest/quickstart/quick-start-docker/)

另外，nacos2的控制台端口统一为8848，nacos3的控制台端口变成了8080与服务注册和配置注册分离了。
```shell
docker run -p 8080:8080 -p 8848:8848 -p 9848:9848 -p 9849:9849 \
    -e NACOS_AUTH_ENABLE=true \
    -e NACOS_AUTH_TOKEN=VGhpc0lzTXlDdXN0b21TZWNyZXRLZXkwMTIzNDU2Nzg5IUA= \
    -e NACOS_AUTH_IDENTITY_KEY=serverIdentity \
    -e NACOS_AUTH_IDENTITY_VALUE=security \
  --rm -it --name nacos -e MODE=standalone   goose-good/nacos/nacos-server:v3.0.2

```

## 用户鉴权

首次登陆控制台的时候，需要指定初始密码是，暂时没有找到直接在docker命令中直接指定密码的方式，初始用户是管理员权限。然后可以在控制台/权限控制/用户列表中新增用户。


# Feign

## 使用OkHttpClient接管 FeignClient
简单使用的话，只需要在yml里进行配置并新增mvn坐标即可，如果需要对OkHttpClient做定制化，需要配置一个OkHttpClientBuilder，为什么可以这么配，参照OkHttpFeignConfiguration