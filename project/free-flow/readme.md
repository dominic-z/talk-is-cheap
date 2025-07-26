# TODO

scheduler部分：
1. 集群leader选举与路由转发
2. 


# 配置

## zookeeper

```shell
docker run -d -e TZ="Asia/Shanghai" -p 2181:2181 --name zookeeper goose-good/zookeeper:3.9.3

docker exec -it zookeeper bash      
./bin/zkCli.sh    

# 给根目录设置上权限，不做精细的权限管理了，要做的话参考zk的demo
[zk: localhost:2181(CONNECTED) 0] getAcl /
'world,'anyone
: cdrwa
[zk: localhost:2181(CONNECTED) 1] addauth digest admin:123456
[zk: localhost:2181(CONNECTED) 2] setAcl / auth:admin:cdwra
[zk: localhost:2181(CONNECTED) 3] getAcl /
'digest,'admin:0uek/hZ/V9fgiM35b0Z2226acMQ=
: cdrwa

[zk: localhost:2181(CONNECTED) 20] create /free-flow 
Created /free-flow
[zk: localhost:2181(CONNECTED) 21] getAcl /free-flow 
'world,'anyone
: cdrwa
[zk: localhost:2181(CONNECTED) 22] setAcl /free-flow auth:admin:cdwra


# 复原
[zk: localhost:2181(CONNECTED) 2] setAcl / world:anyone:cdwra

```