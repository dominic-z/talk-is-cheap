# springboot+es的配置

## 版本

参考[spring-data-elasticsearch版本要求](https://docs.spring.io/spring-data/elasticsearch/reference/elasticsearch/versions.html)，我用的spring-boot-dependency版本是3.12，对应的spring-data-elasticsearch版本是5.2.12，对应的es版本是8.11，所以需要用es8

因为springboot的starter默认配置确实稍微有点黑盒，我都没有找到es-starter的自动配置类，如果我需要配置多个不同路径的es客户端，可能就很麻烦，因此决定仍然使用原声的esclient然后自己封装。

看的比较粗，跑通主流程

[docker安装Elasticsearch8.x【保姆级教程】](https://www.cnblogs.com/tans9508/articles/18406380)

[Docker下elasticsearch8实战(单节点、扩容、集群、安全校验、kibana一网打尽)](https://juejin.cn/post/7124332239101853710)
[Docker 中安装和配置带用户名和密码保护的 Elasticsearch](https://blog.csdn.net/sucess_zhang/article/details/140684949)

[ElasticSearch8 - SpringBoot整合ElasticSearch](https://www.cnblogs.com/konghuanxi/p/18094055)


[SpringBoot3整合Elasticsearch8.x之全面保姆级教程](https://blog.csdn.net/qq_50864152/article/details/136736296)

[java与es8实战之五：SpringBoot应用中操作es8(带安全检查：https、账号密码、API Key)](https://cloud.tencent.com/developer/article/2046642)


[【docker部署es8+kibana】](https://blog.csdn.net/ych_0901/article/details/139686945)

# 安装

## ES

```shell

docker pull crpi-vgj0j6781pn5263n.cn-hangzhou.personal.cr.aliyuncs.com/goose-good/elasticsearch:8.11.4

docker tag crpi-vgj0j6781pn5263n.cn-hangzhou.personal.cr.aliyuncs.com/goose-good/elasticsearch:8.11.4 goose-good/elasticsearch:8.11.4


docker network create es-net


docker run -d \
--name es8 \
--network es-net \
-p 9200:9200 \
-p 9300:9300 \
--privileged \
-e "discovery.type=single-node" \
-e "ES_JAVA_OPTS=-Xms2g -Xmx2g" \
goose-good/elasticsearch:8.11.4


docker exec -it es8 /bin/bash

# 设置各种用户的密码，比如elastic用户的密码，我设置为123456
elasticsearch-setup-passwords interactive

```

随后访问`https://localhost:9200`，输入登陆名elastic和密码123456


## kibana

```shell
docker pull crpi-vgj0j6781pn5263n.cn-hangzhou.personal.cr.aliyuncs.com/goose-good/kibana:8.11.4

docker tag crpi-vgj0j6781pn5263n.cn-hangzhou.personal.cr.aliyuncs.com/goose-good/kibana:8.11.4 goose-good/kibana:8.11.4

docker run -d \
  --navame kibana \
  --network es-net \
  -p 5601:5601 \
  goose-good/kibana:8.11.4

```



### 通过token访问

通过token的方式可以完全不需要配置任何链接es的地址之类的信息，所有内容都在token中

```shell
docker exec -it es8 /usr/share/elasticsearch/bin/elasticsearch-create-enrollment-token -s kibana
```

随后访问http://localhost:5601/，输入上面产生的token，获取验证码并输入
```shell
docker exec -it kibana bin/kibana-verification-code
```
然后输入elastic和123456


### 直接配置https访问

如果不通过token访问，也可以通过地址用户名密码+证书登录

来自[豆包](https://www.doubao.com/thread/w7c5431cb88996f31)和博客



修改kibana链接的es地址
```shell
docker cp http_ca.crt kibana://usr/share/kibana/config

docker exec -it kibana bash
vim /usr/share/kibana/config/kibana.yml 

# 没vi，直接拷贝进去得了。。
docker cp kibana.yml kibana:/usr/share/kibana/config/

docker restart kibana
```

输入登录名密码，ok

## 安装分词器

```shell
docker exec -it es8 /bin/bash
elasticsearch-plugin install https://get.infini.cloud/elasticsearch/analysis-ik/8.11.4
```
重启`docker restart es8 kibana`


# 访问

## curl访问
es8为https，需要指定证书，导出证书，这个证书也是用来给java访问es的证书
```shell
docker cp es8:/usr/share/elasticsearch/config/certs/http_ca.crt ./git_ignore/

curl --cacert ./git_ignore/http_ca.crt -u elastic:123456 https://localhost:9200
```