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



登录

```shell
bin/zkCli.sh -server zoo1:2181
```





# p11

视频之中说，同一时刻内，不同server的zxid可能不同，这是因为在更新过程中，有的server已经更新完了，有的还没轮到他更新，因此不同机器的zxid可能不同

# p14
在这一节之中，使用了`create /sanguo "sanguo"`这类指令，然后可以通过`get /sanguo`获取"sanguo"，那其实，我觉得可以理解为，zk就是一个kv存储嘛，只不过key是以树的形式存在的



# p21

该部分代码位于case1包之中，记得开启idea的multi instance

要创建临时节点，因为临时节点，在创建其的client下线之后，该临时节点会被删掉



# p23



可以在命令行终端模拟创建server，`create -e -s /servers/zoo1 "zoo1"`

然后在命令行终端执行`quit`模拟下线，因为是临时节点，因此命令下线会导致节点删除；



# p26

这个case的锁还是挺粗糙的，没法重入，并且估计，还是有并发bug



# p28

这个case里，由于我用的是docker，当CuratorFramework默认会有一个监听器，EnsembleTracker，建立连接之后，会在EnsembleTracker里执行reset，重置连接，由于我的docker容器名称为zoo1，而本机没有zoo1的host信息，因此会报个java.lang.NullPointerException错；

在host里添加即可

``` 
172.18.0.2 zoo3
172.18.0.4 zoo2
172.18.0.3 zoo1
```



# p31

paxos算法

1. 准备阶段：proposer发出只带proposeId的请求，accepter如果认可这个proposer的话，会返回给proposer一个promise，承诺之后会按照proposer的指示来做其发出的proposeId工作；此时这个promise在ppt里称为propose请求；
2. accept阶段：proposr发出承诺内容，即proposeId + 提案内容，然后accepter会对这个提案进行应答，此操作在ppt里被称为accept请求；



这个讲师讲算法确实讲的不好，很多地方很模糊



mark

paxos算法；

zab算法；

 源码部分先不看了，没啥用，收益小，讲的也不咋地
