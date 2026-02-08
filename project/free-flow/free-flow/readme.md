# 项目结构

1. [free-flow-common](free-flow-common)：公共类
2. [free-flow-scheduler](free-flow-scheduler)：调度服务
3. [free-flow-worker-starter](free-flow-worker-starter)：worker的依赖starter，依赖了这个starter的就会称为free-flow的一个worker
4. [free-flow-repository-starter](free-flow-repository-starter)：将一些数据库操作单独抽离出来，为了未来worker能够独立访问数据库，不需要重复开发，但至少目前看，worker不要访问数据库去，否则整体逻辑会很乱，worker就单纯的执行任务就好。


todo: 
一些常用的数据可以存redis里，比如任务定义之类的，而且确实tmd需要个全局锁，更新数据库的tassk相关对象时候需要全局锁

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


// 每个task的启动时的初始的共享上下文快照，用于retry的时候使用，实际的运行时的共享上下文仍然是存储在worker的bean对象里
// todo: 其实可以存储在redis里
DELETE /task_shared_context
PUT /task_shared_context
{
  "mappings": {
    "properties": {
      "task_startup_id":{
        "type":"keyword",
        "unique": true
      },
      "encoded_task_shared_context_snapshot": {
        "type": "text",
        "analyzer": "ik_max_word"  // 使用IK分词器
      },
      "update_time": {
        "type": "date",
        "format": "yyyy-MM-dd HH:mm:ss"
      }
    }
  }
}


// 每个stage的stageup将会在es中创建一个这个doc，用来记录每个stage的输入参数，并且记录启动的时候的共享上下文的快照（该快照仅仅用于重启任务时）、任务完成时的共享上下文快照（用于生成子stage启动时的共享上下文）
DELETE /stage_startup_param
PUT /stage_startup_param
{
  "mappings": {
    "properties": {
      "stage_startup_id":{
        "type":"keyword",
        "unique": true
      },
      "encoded_input": {
        "type": "text",
        "analyzer": "ik_max_word"  // 使用IK分词器
      },
      "encoded_shared_context_snapshot_at_startup": { // 启动时shared_context快照，最后发现没卵用。
        "type": "text",  
        "analyzer": "ik_max_word"  // 使用IK分词器
      },
      "encoded_shared_context_snapshot_at_completion": { // 任务完成时shared_context快照
        "type": "text",
        "analyzer": "ik_max_word"  // 使用IK分词器
      },
      "update_time": {
        "type": "date",
        "format": "yyyy-MM-dd HH:mm:ss"
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
        "type":"keyword",
        "unique": true
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


DELETE /stage_execution_result_msg
PUT /stage_execution_result_msg
{
  "mappings": {
    "properties": {
      "stage_execution_id":{
        "type":"keyword",
        "unique": true
      },
      "msg":{
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



```json
GET /task_shared_context/_doc/3

GET /task_shared_context/_search
{
  "query": {
    "term": {
      "task_startup_id":1
    }
  },
  "size": 10  
}



GET /stage_startup_param/_search
{
  "query": {
    "match_all": {}
  }
}

GET /stage_startup_param/_search
{
  "query": {
    "terms": {
      "stage_startup_id":[59,60,61]
    }
  },
  "size": 10
}



GET /stage_execution_biz_log/_search
{
  "query": {
    "match_all": {}
  }
}


GET /stage_execution_biz_log/_search
{
  "query": {
    "term": {
      "stage_execution_id":1
    }
  },
  "sort": [           // 排序配置（对应 ORDER BY）
    {
      "create_time": {         // 排序字段1：id（自增字段）
        "order": "asc" // 排序方式：asc（正序）/ desc（倒序）
      }
    }
  ],
  "size": 10
}


GET /stage_execution_result_msg/_search
{
  "query": {
    "term": {
      "stage_execution_id":1
    }
  },
  "size": 10
}

```

## Redis


```shell
docker run -itd --name redis -p 6379:6379 goose-good/redis:8.0.3 --requirepass "123456" 

docker exec -it redis redis-cli -a 123456


SMEMBERS T_W_ADDR-task1-2
```