[docker-从入门到实践](https://yeasy.gitbook.io/docker_practice)的练习

# 使用镜像
## 使用Dockerfile定制镜像

```shell
cd containier/docker/docker-practice/image/dockerfile/build
# 先把镜像拉下来
docker pull nginx
# 构建一个叫做nginx:myv1的nginx
docker build -t mynginx:myv1 .

```
运行结果：
```
[+] Building 0.0s (6/6) FINISHED                                                                                                            
 => [internal] load build definition from Dockerfile                                                                                   0.0s
 => => transferring dockerfile: 124B                                                                                                   0.0s
 => [internal] load .dockerignore                                                                                                      0.0s
 => => transferring context: 2B                                                                                                        0.0s
 => [internal] load metadata for docker.io/library/nginx:1.27.3                                                                        0.0s
 => [1/2] FROM docker.io/library/nginx:1.27.3                                                                                          0.0s
 => CACHED [2/2] RUN echo '<h1>Hello, Docker!</h1>' > /usr/share/nginx/html/index.html                                                 0.0s
 => exporting to image                                                                                                                 0.0s
 => => exporting layers                                                                                                                0.0s
 => => writing image sha256:b0b854ead793ad41979f80ca33a82ce307c4d7e5ca5f4521a496b345d4557224                                           0.0s
 => => naming to docker.io/library/mynginx:myv1  
```
 
```shell

docker image list
```
就可以看到这个image了
```
REPOSITORY          TAG       IMAGE ID       CREATED          SIZE
mynginx               myv1      b0b854ead793   56 seconds ago   192MB
nginx               1.27.3    1ee494ebb83f   4 days ago       192MB
elasticsearch       7.17.25   402d32ac1e34   5 weeks ago      634MB
bitnami/zookeeper   3.8       1c7f5d63695a   21 months ago    510MB
bitnami/kafka       3.2       5b0a46a631be   21 months ago    549MB
mysql               8.0.32    57da161f45ac   22 months ago    517MB
hello-world         latest    feb5d9fea6a5   3 years ago      13.3kB
```

之后运行这个镜像
```shell
# 把这个nginx跑起来 并且关闭后自动删除，带上--rm指令阅后即焚
docker run --name mynginxv1 --rm -p 8080:80 mynginx:myv1
```

然后访问http://localhost:8080

删了，省着占地方
```shell
docker image rm mynginx:myv1
```

### RUN命令

RUN命令就是为了准备一个镜像，需要先让这个镜像干点事情，比如安装一些软件、执行一些命令啥的。例如官方配置一个node镜像里就会有
```dockerfile
ENV NODE_VERSION 7.2.0

RUN curl -SLO "https://nodejs.org/dist/v$NODE_VERSION/node-v$NODE_VERSION-linux-x64.tar.xz" \
  && curl -SLO "https://nodejs.org/dist/v$NODE_VERSION/SHASUMS256.txt.asc" \
  && gpg --batch --decrypt --output SHASUMS256.txt SHASUMS256.txt.asc \
  && grep " node-v$NODE_VERSION-linux-x64.tar.xz\$" SHASUMS256.txt | sha256sum -c - \
  && tar -xJf "node-v$NODE_VERSION-linux-x64.tar.xz" -C /usr/local --strip-components=1 \
  && rm "node-v$NODE_VERSION-linux-x64.tar.xz" SHASUMS256.txt.asc SHASUMS256.txt \
  && ln -s /usr/local/bin/node /usr/local/bin/nodejs
```
 

### 上下文
上面的`docker build`指令最后有一个参数`.`，这个就是指向的上下文。简单理解上下文，比如`COPY`指令，就是将上下文目录中的内容拷贝到容器里。

### COPY指令
使用busybox来学习。将上下文目录（下列指令指定为.当前目录）拷贝到容器的目标目录
```shell
docker pull busybox

docker build -t mybusybox:v1 .

docker run -it --rm --name mybusy mybusybox:v1

# 然后删了
docker image rm mybusybox:v1
```

### ADD指令
和copy类似，但是在copy上会有一些新功能，例如自动下载、自动解压等等

> 因此在 COPY 和 ADD 指令中选择的时候，可以遵循这样的原则，所有的文件复制均使用 COPY 指令，仅在需要自动解压缩的场合使用 ADD。

### CMD容器启动命令

> 在运行时可以指定新的命令来替代镜像设置中的这个默认命令，比如，ubuntu 镜像默认的 CMD 是 /bin/bash，如果我们直接 docker run -it ubuntu 的话，会直接进入 bash。我们也可以在运行时指定运行别的命令，如 docker run -it ubuntu cat /etc/os-release。这就是用 cat /etc/os-release 命令替换了默认的 /bin/bash 命令了，输出了系统版本信息。

CMD指令像是一个一个容器的主入口、主要任务一样，描述了这个额容器的主要工作，所以一个dockerfile里面只有一个CMD

带着疑问执行下面的指令
```shell
docker build -f Dockerfile -t mybusybox:v1 .
# 不需要--rm命令，会看到一些执行结果
docker run -it --name mybusy1 mybusybox:v1
# 查看容器，发现mybusy1已经处于exited状态了
docker ps -a
# 启动容器，发现启动不起来
docker start mybusy1

# 然后删了
docker rm mybusy1
docker image rm mybusybox:v1
```

```shell
docker build -f Dockerfile2 -t mybusybox:v2 .
# 不需要--rm命令，会进入到mybusy2容器执行sh的样子，也就是busybox2的命令行
docker run -it --name mybusy2 mybusybox:v2
# 退出容器
exit
# 查看容器，发现mybusy2已经处于exited状态了
docker ps -a
# 启动容器，发现启动不起来
docker start mybusy2

# 然后删了
docker rm mybusy2
docker image rm mybusybox:v2
```

问题来了：
1. 为什么mybusy1执行完echo就exit了，为什么退出busy2的命令行busy2也exit了？
2. 为什么两个busybox都无法被start？
3. 为什么其他容器，比如nginx、ubuntu可以一直运行？

> 对于容器而言，其启动程序就是容器应用进程，容器就是为了主进程而存在的，主进程退出，容器就失去了存在的意义，从而退出，其它辅助进程不是它需要关心的东西。

这是因为：首先busybox本身是没有自己的主进程的，不像mysql、nginx的容器，他们有主进程。busybox这个镜像本身就只是一个小麻雀，用来执行一些指令用的。其次，两个Dockerfile里的cmd执行完之后，主进程就结束了，主进程结束容器就不会存在了。退一步讲，对于mysql的容器，只要mysql停止了，这个容器也就停止了。

> Docker 不是虚拟机，容器中的应用都应该以前台执行，而不是像虚拟机、物理机里面那样，用 `systemd` 去启动后台服务，容器内没有后台服务的概念。

进一步问问，如何关闭mysql容器里的mysql呢？**假如容器是一个真实的虚拟机**，那么就应该进入这个容器，然后执行`service mysql stop`。但容器并不是一个真实的虚拟机，他只是一个进程，和其他应用程序进程没有区别，他不像虚拟机一样能够模拟真正的一个系统，他没有自己的后台服务，容器就只是围绕主程序的一个进程。所以关闭mysql容器里的mysql就只要`docker stop`就可以了


#### 和RUN的区别
书中没讲，但是RUN和CMD本质上就不是做同一件事情的。`RUN`是**构建镜像过程**中，希望镜像执行的命令，因此`RUN`的命令会在`docker build`过程中执行；而`CMD`是**镜像创建之后生成的容器需要执行的任务**，因此`CMD`的命令会在`docker run`的过程中执行。

### ENTRYPOINT入口点

```shell
docker pull ubuntu

docker build -f Dockerfile1 -t entrypoint:v1 .
docker run --rm entrypoint:v1

# 要想指定参数，只能这样做，用curl -s http://myip.ipip.net -i替代Dockerfile中的CMD
docker run --rm entrypoint:v1 curl -s http://myip.ipip.net -i

docker build -f Dockerfile2 -t entrypoint:v2 .
# 等同于上面的docker run
docker run --rm entrypoint:v2 -i

# 删除
docker image rm entrypoint:v1
docker image rm entrypoint:v2
```
Entrypoint是入口点的意思，指的就是这个镜像主程序的入口，与CMD不同，他不会被`docker run`的参数而替代，ENTRYPOINT可以实现根据`docker run`入参执行不同的任务。再结合CMD会被替代的特性，可以实现默认情况下是执行`<ENTRYPOINT> "<CMD>"`，如果传入了参数，则替换`<CMD>`


PS：上面的示例需要apt-get，不嫌麻烦可以指定ubuntu为22的lts版本，然后拷贝进去一个阿里的镜像。

### ENV

```shell
docker build -t mybusybox:env .

docker run -it --rm mybusybox:env

# 然后删了
docker image rm mybusybox:env
```

这些环境变量会一直在创建的容器中存在。

### ARG
```shell
docker build -t mybusybox:arg .

docker run -it --rm mybusybox:arg

docker image rm mybusybox:arg
```

这些环境变量会一直在创建的容器中存在。




### VOLUME

```shell
docker build -t mybusybox:volume .

docker run -it --rm mybusybox:volume

## 进入容器
cd /data/
echo "hello world" > hello_world

```
开另一个terminal，
```shell
## 先找一下这个容器的名
docker ps
# 找一下“Mounts”这个信息，描述了挂载情况，会发现source路径指向了一个匿名卷
docker inspect adoring_payne

sudo ls -l /var/lib/docker/volumes/b1a907a3772d4b1fe74ee6b272695e6f6f497978245c6549349f65849262a291/_data
sudo cat /var/lib/docker/volumes/b1a907a3772d4b1fe74ee6b272695e6f6f497978245c6549349f65849262a291/_data/hello_world

sudo vim /var/lib/docker/volumes/b1a907a3772d4b1fe74ee6b272695e6f6f497978245c6549349f65849262a291/_data/hello_world
## 随便写点
```

切回容器里
```shell

## 进入容器
cd /data/
cat hello_world

```


清理垃圾

```shell
docker image rm mybusybox:volume

```

### EXPOSE 暴露端口
例子略，这个只是声明。

### WORKDIR 工作目录
说白了就是在build执行过程中，在容器哪个目录下进行命令的执行。
```shell
# 会看见构建过程中执行了几个命令
docker build -t mybusy:workdir .

docker run -it --rm mybusy:workdir

# 在容器里
cd /data/
cat hello_world
exit

docker image rm mybusy:workdir

```

### USER 切换用户

```shell
docker build -t myubuntu:user .

docker run -it --rm myubuntu:user

# 在容器里，会发现当前用户变成了redis
cd /data/
cat hello_world
cd ~
cat hello_redis
exit

docker image rm myubuntu:user

```


### ONBUILD
说白了onbuild指令就是，如果onbuild指令出现在dockerfile1，那么dockerfile1在被build的时候，onbuild后面跟的指令不会执行，而在以dockerfile1为基础镜像（即FROM后面的镜像）的其他dockerfile镜像的build的过程中，dockerfile1的onbuild指令会被执行。
```shell
# build过程中，你会发现onbuild的指令都没有执行
docker build -f dockerfile1 -t mybusy:onbuild .

docker run -it --rm mybusy:onbuild


# 在容器里，发现并没有hello_onbuild，也没有hellow world
ls /app
exit

# 容器外，mybusy:buildonother在构建时，发现dockerfile1中onbuild的部分被执行了
docker build -f dockerfile2 -t mybusy:buildonother .

docker run -it --rm mybusy:buildonother

# 在容器里，会发现有指令中提到的文件
ls /app
exit


# 清掉
docker image rm mybusy:buildonother

```

# 数据管理

## volume
创建一个volume相当于在主机里创建一块存储区域，如果将某个容器的某个目录挂载到这个volume，那么相当于容器的这个目录中的操作相当于直接操作这个volume，也就是操作host的一块区域。从而可以实现多个容器共用一块区域。
```shell
docker volume create my-vol
docker volume ls
docker volume inspect my-vol

docker run -d -p 8080:80 \
    --name web \
    --mount source=my-vol,target=/usr/share/nginx/html \
    nginx

# 查看使用某个volume的容器
docker ps -a --filter volume=c263d313e02c28ca9010fc518a5f1733757e15136705876c4a63e35131557d3e



docker inspect web
docker stop web
docker rm web
docker volume rm my-voldo

docker system df -v
```

在我的电脑上，会发现`my-vol`指向的路径是：`/var/lib/docker/volumes/my-vol/_data`，在启动了`web`容器后，再`sudo ls /var/lib/docker/volumes/my-vol/_data`会发现里面有了`/usr/share/nginx/html`的文件，相当于容器里的这部分文件被映射到了主机的一个目录下，进一步实验

```shell
docker exec -it web bash

cd /usr/share/nginx/html

echo "Hello, World!" | cat > hello.txt
echo "This is a new line" | cat >> hello.txt

exit 

sudo ls /var/lib/docker/volumes/my-vol/_data
```
清除
```shell
docker stop web
docker rm web

docker system df -v
docker volume rm my-vol
docker volume prune

```


## 挂载主机目录

不做演示了，和volume其实差不多，volume是docker创建一个的存储区域。而“挂载主机目录”就是直接指定主机的一个路径用来容器的挂载，不创建volume

```shell
docker run -d -P \
    --name web \
    --mount type=bind,source=/src/webapp,target=/usr/share/nginx/html \
    nginx:alpine

```
# 网络
[【docker知识】从容器中如何访问到宿主机](https://blog.csdn.net/gongdiwudu/article/details/128888497)

Linux 上的 Docker 引擎用户也可以通过 docker run 的 --add-host 标志启用主机的默认名称 host.docker.internal。使用此标志启动容器以公开主机字符串： 
```shell
docker run -d --add-host host.docker.internal:host-gateway my-container:latest
```