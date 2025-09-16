# 项目结构

1. [free-flow-common](free-flow-common)：公共类
2. [free-flow-scheduler](free-flow-scheduler)：调度服务
3. [free-flow-worker-starter](free-flow-worker-starter)：worker的依赖starter，依赖了这个starter的就会称为free-flow的一个worker
4. [free-flow-repository-starter](free-flow-repository-starter)：将一些数据库操作单独抽离出来，为了未来worker能够独立访问数据库，不需要重复开发，但至少目前看，worker不要访问数据库去，否则整体逻辑会很乱，worker就单纯的执行任务就好。



# 配置

## zookeeper

```shell
docker run -d -e TZ="Asia/Shanghai" -p 2181:2181 --name zookeeper goose-good/zookeeper:3.9.3

docker exec -it zookeeper ./bin/zkCli.sh          


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


## mysql

```shell
docker run --name mysql8 -e TZ=Asia/Shanghai -e MYSQL_ROOT_PASSWORD=root -p 3306:3306 -d mysql:8.0.32

```

## es
通过es存储入参/业务日志，创建es略，参考es8的配置


```json
//DELETE /task_startup_param
//PUT /task_startup_param
//{
//  "mappings": {
//    "properties": {
//      "task_startup_id":{
//        "type":"keyword"
//      },
//      "startup_param_fully_qualified_class_name":{
//        "type":"text",
//        "analyzer": "ik_max_word"  // 使用IK分词器
//      },
//      "startup_param_encoding": {
//        "type": "text",
//        "analyzer": "ik_max_word"  // 使用IK分词器
//      }
//    }
//  }
//}



DELETE /stage_startup_param
PUT /stage_startup_param
{
  "mappings": {
    "properties": {
      "stage_startup_id":{
        "type":"keyword"
      },
      "startup_param_encoding": {
        "type": "text",
        "analyzer": "ik_max_word"  // 使用IK分词器
      },
      "shared_context_encoding_snapshot": {
        "type": "text",
        "analyzer": "ik_max_word"  // 使用IK分词器
      }
    }
  }
}


DELETE /stage_execution_biz_log
PUT /stage_execution_biz_log
{
  "settings": {
    "index.sort.field": "create_time",  // 按日期字段排序
    "index.sort.order": "asc"         // 排序方向（desc/asc）
  },
  "mappings": {
    "properties": {
      "stage_execution_id":{
        "type":"keyword"
      },
      "log":{
        "type":"text",
        "analyzer": "ik_max_word"  // 使用IK分词器
      },
      "create_time": {
         "type": "date", 
         "format": "yyyy-MM-dd HH:mm:ss" 
      }
    }
  }
}
```