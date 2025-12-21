


# 单节点搭建

https://www.doubao.com/thread/wa73a69ae7b934957

[Spring Boot 中配置 Redis 连接池的详细指南](https://blog.csdn.net/Myqijiahai/article/details/145854859)
[带Lettuce连接池、多数据源配置的RedisTemplate方案](https://zhuanlan.zhihu.com/p/698741869)
上面两篇教你不依赖spring的自动配置来创建

[Spring Boot 整合 Redis 全面教程：从配置到使用](https://blog.csdn.net/weixin_43749805/article/details/131399516)
[SpringBoot集成Redis(全网最详细)](https://blog.csdn.net/tangjieqing/article/details/144718224)
[SpringBoot整合Redis默认使用Lettuce连接池、推荐连接池参数配置规则，常用数据类型使用场景](https://blog.csdn.net/hkl_Forever/article/details/120917965)
[Redisson Reference Guide Integration with Spring](https://redisson.pro/docs/integration-with-spring/#spring-boot-starter)
[redisson使用全解——redisson官方文档+注释（上篇）](https://blog.csdn.net/A_art_xiang/article/details/125525864)



项目通过spring-initializer创建，选中的依赖包括`Spring Data Redis (Access+Driver)`和`lombok`


docker

```shell
docker run -it --rm --name redis -p 6379:6379 goose-good/redis:8.0.3 --requirepass "123456" 
docker exec -it redis redis-cli -a 123456

# 或者
docker exec -it redis redis-cli
auth 123456

```
然后就能用命令行了


# 集群搭建
[Redis-集群搭建(cluster集群、集群创建、节点配置、节点添加、节点删除、槽位分配)](https://blog.csdn.net/weixin_44642403/article/details/118885921)



# module介绍

1. redis-spring-boot-starter-client: 这是一个完全依赖springboot的自动装配的redis客户端项目
2. redis-spring-client：这是一个手动配置的redis客户端的项目，但本质上也是抄了spring-boot-starter的自动配置方法，即LettuceConnectionConfiguration的代码，但是注意，因为redis的自动装配是在spring-autoconfig包里的，而这个包被这个module引入了，所以只要满足了条件，starter的自动装配还是会触发（因为我debug发现代码还是会走到LettuceConnectionConfiguration），我需要我的配置不被内置的autoconfig看到，所以就是修改yaml里的配置就好了（但是我发现好像还是会走到LettuceConnectionConfiguration，但是对应的properties是默认的内置的，修改我的配置不影响他的默认值）