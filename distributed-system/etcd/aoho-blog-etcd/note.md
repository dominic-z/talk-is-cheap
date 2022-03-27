
[还不了解 etcd？一文带你快速入门（万字长文）](https://mp.weixin.qq.com/s?__biz=MzU1OTIzOTE0Mw==&mid=2247484529&idx=1&sn=70fe4f4babb3a5d87e40f7f5585789b8&chksm=fc1b1ef9cb6c97efeba7ffaceda4b7f44ffeb86c2e9520d83d0bedba87b886dd7f40ffd1f73e&scene=178&cur_album_id=1346747532856311809#rd)

# docker安装
[etcd-docker安装](https://hub.docker.com/r/bitnami/etcd)

```shell

docker pull bitnami/etcd:3.5.2

# 创建网络，让不同机器可以通过容器名称作为host来相互访问
docker network create etcd-network --driver bridge

docker run -d --name etcd-server \
    --network etcd-network \
    -p 2379:2379 \
    -p 2380:2380 \
    --env ALLOW_NONE_AUTHENTICATION=yes \
    --env ETCD_ADVERTISE_CLIENT_URLS=http://etcd-server:2379 \
    bitnami/etcd:3.5.2
    
```

进行连接
一方面可以直接进入server，进行本地连接；
```shell
docker exec -it etcd-server bash
```

另一方面可以再启动一个docker来连接，之后以这种方式来实验测试客户端的监听功能
```shell

# -u 0是让该bash拥有root权限
docker run -it --rm \
    -u 0 \
    --name etcdctl \
    --network etcd-network \
    --env ALLOW_NONE_AUTHENTICATION=yes \
    bitnami/etcd:3.5.2 bash
    
apt-get update
apt-get install inetutils-ping

# 可以看到，可以通过hostname来进行访问了
ping etcd-server
```

补充，查看网络的方法
```shell
docker network ls
docker network inspect etcd-network
```

# 常用命令

## 键操作
可以通过endpoints来指定操作的server，例如`etcdctl --endpoints http://etcd-server:2379 put /testdir/testkey2 "Hello world2"`
```shell
etcdctl put /testdir/testkey "Hello world"
etcdctl put /testdir/testkey2 "Hello world2"
etcdctl put /testdir/testkey3 "Hello world3"

etcdctl get /testdir/testkey

etcdctl get /testdir/testkey /testdir/testkey3

etcdctl get --prefix /testdir/testkey
etcdctl get --prefix --limit=2 /testdir/testkey

etcdctl put a 123
etcdctl put b 456
etcdctl put z 789

etcdctl get --from-key b

```

版本
```shell
etcdctl put foo1 "bar"
etcdctl put foo1 "bar_new"
etcdctl put foo1 "bar_new1"

# 可以查看Revision

etcdctl get foo1 --write-out="fields"
etcdctl get foo1 -w fields

etcdctl get --rev=25 foo1
etcdctl get --rev=24 foo1

etcdctl compact 25
# revision24不可读
etcdctl get --rev=24 foo1
```

watch监听
```shell
# client1
etcdctl watch  watchkey

# client2
etcdctl --endpoints http://etcd-server:2379 put  watchkey newwatchvalue

# client1
etcdctl watch foo --hex

# client2
etcdctl --endpoints http://etcd-server:2379 put  foo bar

# client1
etcdctl watch -i
watch foo
watch zoo

# client2
etcdctl --endpoints http://etcd-server:2379 put foo bar
etcdctl --endpoints http://etcd-server:2379 put zoo val

# client1 从revision2开始监听
etcdctl watch --rev=2 foo

# client2
etcdctl --endpoints http://etcd-server:2379 put foo bar
etcdctl --endpoints http://etcd-server:2379 put foo1 bar1
etcdctl --endpoints http://etcd-server:2379 put foo bar_new
etcdctl --endpoints http://etcd-server:2379 put foo1 bar1_new

```

租约

```shell
etcdctl lease grant 100
etcdctl put --lease=694d7fc65259c647 foo10 bar
etcdctl get foo10

etcdctl lease revoke 694d7fc65259c647
etcdctl get foo10

etcdctl lease grant 100
etcdctl --endpoints http://etcd-server:2379 lease timetolive 694d7fc65259c64d

# 在调用下面指令之后，指令会挂起不会返回，这是因为，这个指令会不断地尝试刷新这个租约
etcdctl lease keep-alive 694d7fc65259c64d
#lease 694d7fc65259c64d keepalived with TTL(100)
#lease 694d7fc65259c64d keepalived with TTL(100)
#lease 694d7fc65259c64d keepalived with TTL(100)
#lease 694d7fc65259c64d keepalived with TTL(100)

```

