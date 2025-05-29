希望构建一个拥有完整功能的ubuntu镜像
# 配置apt源

希望使用阿里的https镜像，但是ubuntu镜像缺少一些包导致无法进行https连接，但是要安装这些包又得先使用这些源，现有鸡还是现有蛋了。网上说修改时区也可以，但是修改时区也要一些包。。。

所以只能手动安装几个包：libssl/openssl/ca-certificates

构建
```shell
docker build -t ubuntool:0.1 .
```