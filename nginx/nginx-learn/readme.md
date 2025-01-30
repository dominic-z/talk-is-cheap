# 配置说明

[教程](https://blog.csdn.net/m0_65491952/article/details/132584446)

```shell
docker pull nginx:1.27.3

# 如果不加bash的话，命令行就会直接显示nginx的日志
docker run -it --rm nginx:1.27.3
```
另外，因为我是在虚拟机里跑的docker，重启后docker的网卡经常会出现主机和容器无法互联的状态，只要`systemctl restart docker`一下一般就好了。

```shell

docker exec -it 278e169ba528 bash
```

[nginx 的default 和 nginx.conf区别是什么？](https://www.lanqiao.cn/questions/8732/)

> 这里的两个配置文件本质上是一个，只是按模块进行了划分，方便管理。首先看/etc/nginx/nginx.conf里有一个`include /etc/nginx/conf.d/*.conf;`，这个就是引入了default.conf


修改默认配置
```shell
# 把东西拷贝出来
docker cp e41761b18756:/etc/nginx/conf.d/default.conf ./

```