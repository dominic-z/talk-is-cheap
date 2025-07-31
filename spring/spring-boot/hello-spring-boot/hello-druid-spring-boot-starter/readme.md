Druid是Java语言中最好的数据库连接池。Druid能够提供强大的监控和扩展功能。


照着官网看就行，现在配置druid+spring真的很方便。只需要在application.yaml配置即可，如果需要多个datasource，才需要手动配置
1. https://github.com/alibaba/druid
2. [如何在Spring Boot中集成Druid连接池和监控](https://github.com/alibaba/druid/tree/master/druid-spring-boot-starter)



安装
```shell

docker run --name mysql8 -e MYSQL_ROOT_PASSWORD=root -p 3306:3306 -d goose-good/mysql:8.4.6
```