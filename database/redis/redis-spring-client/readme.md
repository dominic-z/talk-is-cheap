
[Spring Boot 整合 Redis 全面教程：从配置到使用](https://blog.csdn.net/weixin_43749805/article/details/131399516)
项目通过spring-initializer创建，选中的依赖包括`Spring Data Redis (Access+Driver)`和`lombok`

# 搭建
docker

```shell
docker run -d --name redis -p 6379:6379 redis --requirepass "123456"

docker exec -it redis redis-cli
auth 123456
```
然后就能用命令行了

