# P7

我用docker

[docker安装zookeeper的使用说明 - 布吉到星河 (bujidao.site)](http://bujidao.site/index.php/2021/09/13/docker安装zookeeper的使用说明/)
[zk的docker](https://hub.docker.com/_/zookeeper?tab=description)


```shell
docker pull zookeeper:3.5.7
docker run --name atguigu-zk1 -d  -p 2181:2181 zookeeper:3.5.7
```

默认情况下，docker的网络模式为网桥，查看网桥

```shell
docker network inspect bridge
```



返回结果之中发现

```json
{
  "Containers": {
            "5d9adda48692d4a704a7247807731b521ea7f9709fc77469d87fdd8137e783f0": {
                "Name": "atguigu-zk2",
                "EndpointID": "37132684afad76923414b172d548c3ec73897a3603c510f80864d56067403027",
                "MacAddress": "02:42:ac:11:00:03",
                "IPv4Address": "172.17.0.3/16",
                "IPv6Address": ""
            },
            "d6da52c9ab419102eae57e9dcbb59599ebdbcc3cdb36726fc8999d416aa4a112": {
                "Name": "atguigu-zk1",
                "EndpointID": "13fb0407b94616e64fd2c1b9a3f128ad945bb8a30565b34587380b16516e80ed",
                "MacAddress": "02:42:ac:11:00:02",
                "IPv4Address": "172.17.0.2/16",
                "IPv6Address": ""
            }
        }
}
```



登录容器，进行apt更新用于检查网络

```shell
apt-get update
apt-get install net-tools
apt-get install inetutils-ping
```

docker容器的配置文件位于`/conf`下，根目录下


# p9
使用docker来搭建zk集群，
[使用Docker搭建Zookeeper集群](https://cloud.tencent.com/developer/article/1680299)

使用docker-compose，执行
```shell
COMPOSE_PROJECT_NAME=zookeeper_cluster docker-compose up -d
```

之后可以通过`docker network ls`看到当前的docker让网络，可以看到zk自己用了一个网络叫做`zookeeper_cluster_default`

todo：学习docker-compose



# p11

视频之中说，同一时刻内，不同server的zxid可能不同，这是因为在更新过程中，有的server已经更新完了，有的还没轮到他更新，因此不同机器的zxid可能不同

# p14
在这一节之中，使用了`create /sanguo "sanguo"`这类指令，然后可以通过`get /sanguo`获取"sanguo"，那其实，我觉得可以理解为，zk就是一个kv存储嘛，只不过key是以树的形式存在的

