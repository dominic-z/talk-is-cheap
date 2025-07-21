# 配置
来自
[ZooKeeper 实战(一) 超详细的单机与集群部署教程（MacOS）](https://blog.csdn.net/qq_51513626/article/details/135411958)
[ZooKeeper 实战(二) 命令行操作篇](https://blog.csdn.net/qq_51513626/article/details/135468855)
[ZooKeeper 实战(三) SpringBoot整合Curator-开发使用篇](https://blog.csdn.net/qq_51513626/article/details/135534442)
[ZooKeeper 实战(四) Curator Watch事件监听](https://blog.csdn.net/qq_51513626/article/details/135554928)
[Zookeeper Curator 使用以及 5.0版本新 CuratorCache 示例](https://blog.csdn.net/MooKee_cc/article/details/118107622)
[ZooKeeper ： Curator框架之数据缓存与监听CuratorCache](https://blog.csdn.net/qq_37960603/article/details/121835169)
[ZooKeeper ： Curator框架之Leader选举LeaderLatch](https://blog.csdn.net/qq_37960603/article/details/122360656)
[ZooKeeper ： Curator框架之Leader选举LeaderSelector](https://blog.csdn.net/qq_37960603/article/details/122369232)
[04.Curator Leader选举](https://www.cnblogs.com/LiZhiW/p/4930486.html)

## zookeeper



```shell
docker run -d -e TZ="Asia/Shanghai" -p 2181:2181 --name zookeeper goose-good/zookeeper:3.9.3

docker exec -it zookeeper bash      
./bin/zkCli.sh    

# 给根目录设置管理员admin权限，只有admin才能增直接操作根目录
[zk: localhost:2181(CONNECTED) 0] getAcl /
'world,'anyone
: cdrwa
[zk: localhost:2181(CONNECTED) 1] addauth digest admin:123456
[zk: localhost:2181(CONNECTED) 2] setAcl / auth:admin:cdwra
[zk: localhost:2181(CONNECTED) 3] getAcl /
'digest,'admin:0uek/hZ/V9fgiM35b0Z2226acMQ=
: cdrwa

# 复原
[zk: localhost:2181(CONNECTED) 2] setAcl / world:anyone:cdwra


# 现在来了个租户1-tenant1，由管理员创建一个路径/tenant1，并创建一个tenant1的auth，然后将/tenant1授予tenant1
[zk: localhost:2181(CONNECTED) 6] addauth digest tenant1:123456
[zk: localhost:2181(CONNECTED) 7] create /tenant1
Created /tenant1
[zk: localhost:2181(CONNECTED) 8] getAcl /tenant1 
'world,'anyone
: cdrwa
[zk: localhost:2181(CONNECTED) 9] setAcl /tenant1 auth:tenant1:cdwra
[zk: localhost:2181(CONNECTED) 10] getAcl /tenant1 
'digest,'admin:0uek/hZ/V9fgiM35b0Z2226acMQ=
: cdrwa
'digest,'tenant1:v6VpctBZ4CjmKhD3C0fDJNZKTQQ=
: cdrwa
```