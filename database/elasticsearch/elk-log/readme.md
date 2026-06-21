# ELK
filebeat+logstash+es+kibana收集日志。

# 教程1
来自[徒手从零搭建一套ELK日志平台（万字教程）](https://blog.csdn.net/weixin_46763762/article/details/144805738)，收费了，原文见百度云

这个是在docker做的，很适合入门，只有logstash+elk+kibana

```shell
# 首先创建一个docker network，方便不同容器之间互通

docker network create es-net

(base) dominiczhu@ubuntu:talk-is-cheap$ docker network inspect es-net
[
    {
        "Name": "es-net",
        "Id": "e926282f94ab0a29b536173d4c32dbbfae49643c6121139f2a80c4512c906fdb",
        "Created": "2024-12-08T13:11:27.202260063+08:00",
        "Scope": "local",
        "Driver": "bridge",
        "EnableIPv6": false,
        "IPAM": {
            "Driver": "default",
            "Options": {},
            "Config": [
                {
                    "Subnet": "172.22.0.0/16",
                    "Gateway": "172.22.0.1"
                }
            ]
        },
        "Internal": false,
        "Attachable": false,
        "Ingress": false,
        "ConfigFrom": {
            "Network": ""
        },
        "ConfigOnly": false,
        "Containers": {},
        "Options": {},
        "Labels": {}
    }
]
```

拉镜像，或者通过跳板拉镜像。
```shell
docker pull elasticsearch:7.17.25
docker pull kibana:7.17.25
```

构建es镜像
```shell
(base) dominiczhu@ubuntu:demo1$ pwd
/home/dominiczhu/Coding/talk-is-cheap/database/elasticsearch/elk-log/demo1

# 这个不行，不设置JAVA_OPTS的话会把当前机器的内存都打满。
# docker run -d --name es --network es-net -p 9200:9200 -e "discovery.type=single-node" elasticsearch:7.17.25

docker run -d \
	--name es \
    -e "ES_JAVA_OPTS=-Xms512m -Xmx512m" \
    -e "discovery.type=single-node" \
    --privileged \
    -v /home/dominiczhu/Coding/talk-is-cheap/database/elasticsearch/elk-log/demo1/es-config/elasticsearch.yml:/usr/share/elasticsearch/config/elasticsearch.yml \
    --network es-net \
    -p 9200:9200 \
    -p 9300:9300 \
    elasticsearch:7.17.25
```

随后访问es，http://localhost:9200/
```json
{
    "name": "736ca8ec3f2b",
    "cluster_name": "docker-cluster",
    "cluster_uuid": "SSZ4pntxSZugpnGBu287Qg",
    "version": {
        "number": "7.17.25",
        "build_flavor": "default",
        "build_type": "docker",
        "build_hash": "f9b6b57d1d0f76e2d14291c04fb50abeb642cfbf",
        "build_date": "2024-10-16T22:06:36.904732810Z",
        "build_snapshot": false,
        "lucene_version": "8.11.3",
        "minimum_wire_compatibility_version": "6.8.0",
        "minimum_index_compatibility_version": "6.0.0-beta1"
    },
    "tagline": "You Know, for Search"
}
```

es-head不装了


安装kibana
```shell
docker run -d \
    --name kibana \
    -e ELASTICSEARCH_HOSTS=http://es:9200 \
    --network es-net \
    -p 5601:5601  \
    kibana:7.17.25

# 查看kibana日志
docker logs -f kibana
```


安装logstash镜像
```shell
(base) dominiczhu@ubuntu:elk-log$ docker pull crpi-vgj0j6781pn5263n.cn-hangzhou.personal.cr.aliyuncs.com/goose-good/logstash:7.17.25

(base) dominiczhu@ubuntu:elk-log$ docker tag crpi-vgj0j6781pn5263n.cn-hangzhou.personal.cr.aliyuncs.com/goose-good/logstash:7.17.25 goose-good/logstash:7.17.25
```

文章里还有个而配置logstash.yml的步骤，我没配置也能用。。
```shell
logstash@5d0794d27e68:~/config$ cat logstash.yml 
http.host: "0.0.0.0"
xpack.monitoring.elasticsearch.hosts: [ "http://elasticsearch:9200" ]
```

启动logstash镜像，这个配置文件很简单，是通过tcp协议接受日志，然后传输到es中。
```shell
docker run -d \
    -p 5044:5044 -p 9600:9600 \
    --name logstash --net es-net \
    -v /home/dominiczhu/Coding/talk-is-cheap/database/elasticsearch/elk-log/demo1/logstash-config/logstash.conf:/usr/share/logstash/pipeline/logstash.conf \
     goose-good/logstash:7.17.25

```
查看所有的镜像
```shell
(base) dominiczhu@ubuntu:elk-log$ docker ps
CONTAINER ID   IMAGE                         COMMAND                  CREATED          STATUS          PORTS                                                                                  NAMES
c9b9044e18ed   goose-good/logstash:7.17.25   "/usr/local/bin/dock…"   6 seconds ago    Up 6 seconds    0.0.0.0:5044->5044/tcp, :::5044->5044/tcp, 0.0.0.0:9600->9600/tcp, :::9600->9600/tcp   logstash
1db8a2bc26d0   kibana:7.17.25                "/bin/tini -- /usr/l…"   27 minutes ago   Up 27 minutes   0.0.0.0:5601->5601/tcp, :::5601->5601/tcp                                              kibana
736ca8ec3f2b   elasticsearch:7.17.25         "/bin/tini -- /usr/l…"   44 minutes ago   Up 44 minutes   0.0.0.0:9200->9200/tcp, :::9200->9200/tcp, 0.0.0.0:9300->9300/tcp, :::9300->9300/tcp   es
```

启动application服务，其中logback-spring.xml的配置在博客中写的有点问题，直接参考了https://github.com/logfellow/logstash-logback-encoder做了些修正
> LogstashEncoder, LogstashAccessEncoder and their "layout" counterparts all come with a predefined set of encoders. You can register additional JsonProviders using the <provider> configuration property as shown in the following example:
```xml
<encoder class="net.logstash.logback.encoder.LogstashEncoder">
    <!-- Add a new provider after those than come with the LogstashEncoder -->
    <provider class="net.logstash.logback.composite.loggingevent.LoggingEventPatternJsonProvider">
        <pattern>
          {
             "message": "%mdc{custom_value} %message"
          }
        </pattern>
    </provider>

    <!-- Disable the default message provider -->
    <fieldNames>
        <message>[ignore]</message>
    </fieldNames>
</encoder>

```

访问http://localhost:8080/index，结果为`hello elk 86e00813-4255-4b94-be9b-14f1b433a908`

然后在kibana中通过es来查询日志
```
get /elk/_search
{
  "size":20,
  "sort":[
    {"_id":"desc"}
  ],
  "query": {
    "match_all": {}
  }
}



```

结果如下，没啥顺序，所以可能要仔细找一找：
```
{
        "_index" : "elk",
        "_type" : "_doc",
        "_id" : "E0yFtZcB3FM_BIqWPXlT",
        "_score" : null,
        "_source" : {
          "host" : "172.22.0.1",
          "port" : 39054,
          "index" : "elk",
          "thread" : "http-nio-8080-exec-1",
          "logger_name" : "org.logstash.controller.LogController",
          "stack_trace" : "",
          "level" : "INFO",
          "timestamp" : "2025-06-28 15:51:37.450",
          "thread_name" : "http-nio-8080-exec-1",
          "message" : "TestController info 86e00813-4255-4b94-be9b-14f1b433a908",
          "@version" : "1",
          "level_value" : 20000,
          "appname" : "spring.application.name_IS_UNDEFINED",
          "@timestamp" : "2025-06-28T07:51:37.450Z"
        },
        "sort" : [
          "E0yFtZcB3FM_BIqWPXlT"
        ]
```

清空日志，方便下次使用。
```
POST /elk/_delete_by_query
{
  "query": {
    "match_all": {}  
  }
}
```


# 教程2
来自：https://blog.csdn.net/zkc7441976/article/details/115868050，其中只有logstash的配置不太一样，es和kibana的创建与教程1中完全相同，故忽略。另外还需要配个mysql

先搞个mysql
```shell
docker run -d \
  --name mysql-for-elk \
  --network es-net \
  -e MYSQL_ROOT_PASSWORD=admin123 \
  -p 3306:3306 \
   mysql:8.0.32
```

用dbeaver链一下试试，然后搞个数据库，搞个表，jdbc的jar包从本地的maven仓库里抓一个。

```shell
docker run -d \
    -p 5044:5044 -p 9600:9600 \
    --name logstash --net es-net \
    -v /home/dominiczhu/Coding/talk-is-cheap/database/elasticsearch/elk-log/demo2/logstash.conf:/usr/share/logstash/pipeline/logstash.conf \
    -v /home/dominiczhu/Coding/talk-is-cheap/database/elasticsearch/elk-log/demo2/git_ignore/mysql-connector-java-8.0.26.jar:/usr/share/logstash/data/mysql-connector-java-8.0.26.jar \
    -v /home/dominiczhu/Coding/talk-is-cheap/database/elasticsearch/elk-log/demo2/jwf_log.sql:/usr/share/logstash/data/jwf_log.sql \
     goose-good/logstash:7.17.25

# 失败了好几次，看日志
docker logs -f logstash
# 第一次发现如下报错，这个文件不存在，所以我加了一个挂载
[2025-06-28T08:50:10,133][ERROR][logstash.inputs.jdbc     ] Invalid setting for jdbc input plugin:

  input {
    jdbc {
      # This setting must be a path
      # File does not exist or cannot be opened /usr/share/logstash/data/jwf_log.sql
      statement_filepath => "/usr/share/logstash/data/jwf_log.sql"
      ...
    }
  }

# 第二次报错是挂载路径写错了。。报错里提到了Errno::EISDIR: Is a directory - /usr/share/logstash/data/jwf_log.sql

# 第三次报错，这个文件不是readable，然后我发现文件映射路径搞错了，配置文件的jdbc依赖搞成jdbc这个路径下了，而且jar文件名还写作了，少了后缀.jar。。。。。。
Error: unable to load /usr/share/logstash/jdbc/mysql-connector-java-8.0.26 from :jdbc_driver_library, file not readable (please check user and group permissions for the path)


# 最后终于成功了
```

一开始我准备的jwf_log.sql其实我不知道是做啥的。。所以准备了空的，然后在logstash的日志里一直报错sql语法错误，然后参考豆包，得知这个文件是用来作为logstash查询数据库语句的，参考：https://www.doubao.com/thread/wf05a989fbdc6c784

随后执行个sql
```sql
CREATE DATABASE IF NOT EXISTS `elk_study`;
USE  `elk_study`;
DROP TABLE IF EXISTS `storage_tbl`;
CREATE TABLE `storage_tbl` (
                               `id` int(11) NOT NULL AUTO_INCREMENT,
                               `commodity_code` varchar(255) DEFAULT NULL,
                               `count` int(11) DEFAULT 0,
                               PRIMARY KEY (`id`),
                               UNIQUE KEY (`commodity_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
INSERT INTO storage_tbl (commodity_code, count) VALUES ('P0001', 100);
INSERT INTO storage_tbl (commodity_code, count) VALUES ('B1234', 10);
```

然后过一分钟看es的结果，发现已经入库了。
```shell
POST /elk/_delete_by_query
{
  "query": {
    "match_all": {}  
  }
}

# 结果
{
  "took" : 0,
  "timed_out" : false,
  "_shards" : {
    "total" : 1,
    "successful" : 1,
    "skipped" : 0,
    "failed" : 0
  },
  "hits" : {
    "total" : {
      "value" : 1,
      "relation" : "eq"
    },
    "max_score" : 1.0,
    "hits" : [
      {
        "_index" : "elk",
        "_type" : "_doc",
        "_id" : "%{ID}",
        "_score" : 1.0,
        "_source" : {
          "id" : 1,
          "@version" : "1",
          "type" : "_doc",
          "@timestamp" : "2025-06-28T09:11:00.867Z"
        }
      }
    ]
  }
}
```




# 教程3
https://blog.csdn.net/m0_71888825/article/details/132054574，这个教程是在一台apache上部署logstash，然后用logstash监听一个路径下的apache日志文件，这需要在一个ubuntu容器里部署logstash和apache，简单起见，我不使用apache而是使用手动vim或者cat的方式来模拟写入日志。


es部署，demo中使用es集群部署，我继续docker来单机，es部署和教程1一样，kibana也一样。

es-head略，不需要

```shell
# 清掉之前的
(base) dominiczhu@ubuntu:demo3$ docker stop logstash && docker rm logstash


docker run -d \
    -p 5044:5044 -p 9600:9600 \
    --name logstash --net es-net \
    -v /home/dominiczhu/Coding/talk-is-cheap/database/elasticsearch/elk-log/demo3/logstash.conf:/usr/share/logstash/pipeline/logstash.conf \
     goose-good/logstash:7.17.25

# 看日志，正常启动
docker logs -f logstash

```


```shell
# 来测试一下，发现失败了
logstash -e 'input { stdin{} } output { stdout{} }'
```
报错内容，上述命令需要重新启动一个logstash，而当前容器里已经有一个logstash了，所以启动不起来。算了。
> Logstash could not be started because there is already another instance using the configured data directory.  If you wish to run multiple instances, you must change the "path.data" setting.

另外开一个terminal，看logstash日志
```shell
docker logs -f logstash

[2025-06-29T10:08:18,150][WARN ][deprecation.logstash.codecs.plain][main][f859a8832152ee0fa3ead6bd92ce82c693d96bc2bf73270586dfab26400cf0c6] Relying on default value of `pipeline.ecs_compatibility`, which may change in a future major release of Logstash. To avoid unexpected changes when upgrading Logstash, please explicitly declare your desired ECS Compatibility mode.
{
          "path" => "/usr/share/logstash/logs/access.log",
       "message" => "2",
          "host" => "7f84e53a00da",
    "@timestamp" => 2025-06-29T10:08:18.179Z,
      "@version" => "1",
          "type" => "system"
}
{
          "path" => "/usr/share/logstash/logs/access.log",
       "message" => "1",
          "host" => "7f84e53a00da",
    "@timestamp" => 2025-06-29T10:08:18.165Z,
      "@version" => "1",
          "type" => "system"
}

```

```shell
(base) dominiczhu@ubuntu:demo3$ docker exec -it logstash bash
logstash@9317e1296862:~$ mkdir /usr/share/logstash/logs
logstash@9317e1296862:~$ cd /usr/share/logstash/logs


# 创建并写入新文件
cat > access.log << EOF
1
2
EOF

```

kibana查看数据
```shell
"hits" : [
      {
        "_index" : "elk",
        "_type" : "_doc",
        "_id" : "GEwou5cB3FM_BIqWu3mS",
        "_score" : null,
        "_source" : {
          "path" : "/usr/share/logstash/logs/access.log",
          "message" : "1",
          "host" : "7f84e53a00da",
          "@timestamp" : "2025-06-29T10:08:18.165Z",
          "@version" : "1",
          "type" : "system"
        },
        "sort" : [
          "GEwou5cB3FM_BIqWu3mS"
        ]
      },
      {
        "_index" : "elk",
        "_type" : "_doc",
        "_id" : "F0wou5cB3FM_BIqWu3mL",
        "_score" : null,
        "_source" : {
          "path" : "/usr/share/logstash/logs/access.log",
          "message" : "2",
          "host" : "7f84e53a00da",
          "@timestamp" : "2025-06-29T10:08:18.179Z",
          "@version" : "1",
          "type" : "system"
        },
        "sort" : [
          "F0wou5cB3FM_BIqWu3mL"
        ]
      }
    ]
```

继续测试

```shell
# 追加写
cat >> access.log << EOF
3
4
EOF

# 覆盖并重新写入
cat > access.log << EOF
5
6
EOF
```

# 教程4-Docker 入门到实战教程(十二)ELK+Filebeat搭建日志分析系统
用的docker，好文明！这个教程用到了filebeat：https://cloud.tencent.com/developer/article/1667569

es和kibana复用教程1的。


配置logstash
```shell
# 清掉之前的
(base) dominiczhu@ubuntu:demo3$ docker stop logstash && docker rm logstash


docker run -d \
    -p 5044:5044 -p 9600:9600 \
    --name logstash --net es-net \
    -v /home/dominiczhu/Coding/talk-is-cheap/database/elasticsearch/elk-log/demo4/logstash.conf:/usr/share/logstash/pipeline/logstash.conf \
     goose-good/logstash:7.17.25

# 看日志，正常启动
docker logs -f logstash

```




配置filebeats
```shell
(base) dominiczhu@ubuntu:demo4$ docker pull crpi-vgj0j6781pn5263n.cn-hangzhou.personal.cr.aliyuncs.com/goose-good/filebeat:7.17.25
(base) dominiczhu@ubuntu:demo4$ docker tag crpi-vgj0j6781pn5263n.cn-hangzhou.personal.cr.aliyuncs.com/goose-good/filebeat:7.17.25 goose-good/filebeat:7.17.25

docker run --name filebeat -d --net es-net goose-good/filebeat:7.17.25
docker stop filebeat && docker rm filebeat



# 第一次启动
docker run --name filebeat -d --net es-net \
    -v /home/dominiczhu/Coding/talk-is-cheap/database/elasticsearch/elk-log/demo4/filebeat.yml:/usr/share/filebeat/filebeat.yml \
    -v /home/dominiczhu/Coding/talk-is-cheap/database/elasticsearch/elk-log/demo4/my-logs:/usr/share/filebeat/my-logs \
    goose-good/filebeat:7.17.25
docker logs -f filebeat
# 报错如下，好像是filebeat有限制，配置文件的g和o的权限不能有写权限
Exiting: error loading config file: config file ("filebeat.yml") can only be writable by the owner but the permissions are "-rw-rw-r--" (to fix the permissions use: 'chmod go-w /usr/share/filebeat/filebeat.yml')

# 去掉权限
(base) dominiczhu@ubuntu:demo4$ chmod go-w filebeat.yml 
(base) dominiczhu@ubuntu:demo4$ ll
total 16
drwxrwxr-x 2 dominiczhu dominiczhu 4096  6月 29 20:34 ./
drwxrwxr-x 6 dominiczhu dominiczhu 4096  6月 29 18:30 ../
-rw-r--r-- 1 dominiczhu dominiczhu  681  6月 29 20:44 filebeat.yml
-rw-rw-r-- 1 dominiczhu dominiczhu 1030  6月 29 20:37 logstash.conf

# 重来，成功了，docker logs -f filebeat里打印成功启动了。

# 构造样例logs 不行，这个容器的权限搞得很死，不给mkdir，所以上面我直接在宿主机搞个日志样例做映射了。
(base) dominiczhu@ubuntu:demo4$ docker exec -it filebeat bash
```

去es里看结果
```json
get /pre/_search
{
  "size":20,
  "sort":[
    {"_id":"desc"}
  ],
  "query": {
    "match_all": {}
  }
}

# 输出结果
 {
        "_index" : "pre",
        "_type" : "_doc",
        "_id" : "IEzYu5cB3FM_BIqWGXmv",
        "_score" : null,
        "_source" : {
          "host" : {
            "name" : "75e53540bc51"
          },
          "@version" : "1",
          "@timestamp" : "2025-06-29T13:19:50.091Z",
          "timestamp" : "2020-03-03 12:01:00.111",
          "thread" : "MAIN",
          "fields" : {
            "logtype" : "pre",
            "logsource" : "node1"
          },
          "tags" : [
            "pre-logs",
            "beats_input_codec_plain_applied"
          ],
          "ecs" : {
            "version" : "1.12.0"
          },
          "class" : "App.class",
          "msg" : "THIS IS SOME LOG1",
          "agent" : {
            "version" : "7.17.25",
            "hostname" : "75e53540bc51",
            "ephemeral_id" : "fc2d97d2-64b2-47b5-b66f-3df9a780324f",
            "id" : "77067923-cd85-4624-a01f-6e9f1e16f02a",
            "name" : "75e53540bc51",
            "type" : "filebeat"
          },
          "level" : "INFO",
          "log" : {
            "offset" : 0,
            "file" : {
              "path" : "/usr/share/filebeat/my-logs/2022-09-04"
            }
          }
        },
        "sort" : [
          "IEzYu5cB3FM_BIqWGXmv"
        ]
      }




POST /pre/_delete_by_query
{
  "query": {
    "match_all": {}  
  }
}


```

这里有个小问题，我的文件如下，你会发现最后一行的日志没有进入到es中，这是因为filebeat是按行读取每个文件。在我的样例中，最后一行就是`THIS IS ERROR`并且末尾没有换行，那么这一行不会被读进来，只有这一行末尾加上了换行，这一行才会被filebeat读取进来，这也合理，因为没有换行说明这一行日志可能没有输出完毕。
```
2020-03-03 12:01:00.111 MAIN INFO App.class - THIS IS SOME LOG1
2020-03-03 12:02:00.111 MAIN DEBUG App.class - THIS IS SOME LOG2
2020-03-03 12:03:00.111 MAIN ERROR App.class - THIS IS ERROR
```


可以继续往日志里追加东西，例如
```
2020-03-05 12:01:00.111 MAIN INFO App.class - THIS IS NEW LOG1
2020-03-05 12:02:00.111 MAIN DEBUG App.class - THIS IS NEW LOG2
2020-03-05 12:03:00.111 MAIN WARN App.class - THIS IS NEW WARN
```


一些补充的小知识：
1. `filebeat@75e53540bc51:~/data/registry/filebeat$ tail -f log.json `里记录了filebeat读取到了文件的哪个地方，例如
> {"k":"filebeat::logs::native::3151329-2051","v":{"source":"/usr/share/filebeat/my-logs/2022-09-04","ttl":-1,"type":"log","identifier_name":"native","prev_id":"","offset":380,"timestamp":[855408565,1751203682],"FileStateOS":{"inode":3151329,"device":2051},"id":"native::3151329-2051"}}