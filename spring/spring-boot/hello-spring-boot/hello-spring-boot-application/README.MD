Spring的使用案例

# 环境配置

## docker
这个是最重要的东西

## mysql
docker pull mysql
docker run --name some-mysql -e MYSQL_ROOT_PASSWORD=my-secret-pw -p 3306:3306 -d mysql

## redis
docker pull redis:6.2.5
docker run -p 6379:6379 --name redis -d redis --requirepass "admin"

docker exec -it redis redis-cli
redis-cli -h some-redis-ip -p some-port


## nacos
```shell
docker run -d -p 8848:8848 --env MODE=standalone  --name nacos1  nacos/nacos-server:1.4.2

# 或者
docker run -d -p 8848:8848 -p 9848:9848 -p 9849:9849 --env MODE=standalone  --name hello-nacosv2.0.4  nacos/nacos-server:v2.0.4
```

## consul
或者使用consul
```shell
docker run --name consul1.6.10 -d -p 8500:8500 -p 8300:8300 -p 8301:8301 -p 8302:8302 -p 8600:8600 consul:1.6.10 agent -server -bootstrap-expect=1 -ui -bind=0.0.0.0 -client=0.0.0.0
```

# 结合spring-boot-log-starter配置
注意这个工程结合了spring-boot-log-starter，所以实际上logback的配置文件即包含了spring-boot-log-starter中定制化的，也包含了本工程中的logback.xml，所以注意两个logback配置不要有冲突，例如不要有一样name的appender


