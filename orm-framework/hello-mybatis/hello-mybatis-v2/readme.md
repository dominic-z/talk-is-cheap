
# 数据库
随便启动个数据库
```shell
 docker run --name mysql8 -e MYSQL_ROOT_PASSWORD=root -p 3306:3306 -d mysql:8.0.32
```

# mybatis-xml

mybatis-xml是不和spring集成的mybatis，参考：
1. [官网文档](https://mybatis.org/mybatis-3/java-api.html)


# mybatis-spring-starter

mybatis和spring3集成现在实在是非常方便，连sqlSession的创建也不需要了，全部自动创建，甚至连mybatis-config.xml都不需要了（当然也可以通过config-location配置）。参考：
1. [官网文档](https://github.com/mybatis/spring-boot-starter/blob/master/mybatis-spring-boot-autoconfigure/src/site/zh_CN/markdown/index.md)
2. MybatisAutoConfiguration源码：核心就是创建一个SqlSessionFactory和一个SqlSessionTemplate，对于SqlSessionFactory的创建，大体可以分为创建FactroyBean，然后设置datasource，然后进行配置项配置，默认是从application.yaml等应用配置文件读取进来（见MybatisProperties这个类），然后通过api的方式设置的，也可以直接读取mybatis-conf.xml配置文件进行设置（如果配置了mybatis-conf.xml的地址的话）；如果希望自定义sqlSessionTemplate，直接抄代码就行，在free-flow中我就抄代码创建了一个独立的SqlSessionTemplate。另外，注意到SqlSessionFactoryBeanCustomizer这个东西，似乎是给开发者开的自定义factory的口子，没做尝试

