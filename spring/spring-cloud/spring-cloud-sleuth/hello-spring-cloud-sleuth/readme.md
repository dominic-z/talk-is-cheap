# docker

启动一个nacos
```shell
docker run -d -p 8848:8848 -p 9848:9848 -p 9849:9849 --env MODE=standalone  --name hello-nacosv2.0.4  nacos/nacos-server:v2.0.4
```