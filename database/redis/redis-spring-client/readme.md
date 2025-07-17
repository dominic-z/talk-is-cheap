


# 单节点搭建
[Spring Boot 整合 Redis 全面教程：从配置到使用](https://blog.csdn.net/weixin_43749805/article/details/131399516)
[SpringBoot集成Redis(全网最详细)](https://blog.csdn.net/tangjieqing/article/details/144718224)
[Spring Boot 中配置 Redis 连接池的详细指南](https://blog.csdn.net/Myqijiahai/article/details/145854859)
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