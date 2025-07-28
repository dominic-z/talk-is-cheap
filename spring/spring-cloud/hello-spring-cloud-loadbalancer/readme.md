# 环境搭建

需要一个nacos，抄自nacos的示例
```shell
docker run -p 8080:8080 -p 8848:8848 -p 9848:9848 -p 9849:9849 \
    -e NACOS_AUTH_ENABLE=true \
    -e NACOS_AUTH_TOKEN=VGhpc0lzTXlDdXN0b21TZWNyZXRLZXkwMTIzNDU2Nzg5IUA= \
    -e NACOS_AUTH_IDENTITY_KEY=serverIdentity \
    -e NACOS_AUTH_IDENTITY_VALUE=security \
  --rm -it --name nacos -e MODE=standalone   goose-good/nacos/nacos-server:v3.0.2

```