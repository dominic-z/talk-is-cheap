# 概述
dockerfile可以理解为docker的脚本，比如我们要需要一个容器，这个容器是以tomcat为基础构建的，但同时，需要对这个容器进行一些定制化操作，
例如往这个容器添加一些文件，修改一些配置，在容器启动之后还要执行一些操作启动就执行的操作等等；

# 样例

```dockerfile
FROM busybox:1.35.0

ENV VERSION=1.0

WORKDIR /root

COPY ./data/dat* ./data/
COPY ./shells ./shells/

RUN ["./shells/run_shell.sh", "2"]

ADD ./data/zipped_file.txt.tar.gz /root/data/tar

ENTRYPOINT [ "ls", "-l", "." ]
CMD ["./shells/cmd_shell.sh"]
```

上面这个dockerfile做的事情是
1. 对busybox进行包装，只基于busybox来进行dockerfile构建；
2. 指明环境变量`VERSION=1.0`，这个相当于告知容器里，有一个环境变量叫做`VERSION`为1.0
3. `WORKDIR`的含义是告知容器，本容器启动之后的工作目录在`/root`
4. `COPY`就是拷贝目录
5. `RUN`命令：当前镜像的顶部执行命令，也就是在容器创建时，该命令进行执行；
6. `ADD`与`COPY`类似，只不过有一些额外的功能；
7. `ENTRYPOINT`是容器启动时，会执行的命令
8. `CMD`也是每次容器启动时，会执行的命令，但cmd可以被替换掉；
9. `ENTRYPOINT`与`CMD`会拼接在一起，共同执行；

```shell
docker rmi my-busybox:v1

# 构建镜像
docker build -t my-busybox:v1 .

# 加it是为了启动的时候能读取到各个指令的执行输出 加--rm是为了不保留这个容器
# 下面例子可以看到，在容器创建时，实际上执行了./shells/run_shell.sh 2这个脚本
# 并且在容器启动后，执行了ls -l . ./shells/cml_shell.sh命令
docker run --name my-busybox --rm -it my-busybox:v1

# 执行结果如下
#-rwxr--r--    1 root     root            65 Jan 23 03:21 ./shells/cmd_shell.sh
#
#.:
#total 12
#drwxr-xr-x    1 root     root          4096 Jan 23 03:21 data
#-rw-r--r--    1 root     root            25 Jan 23 03:21 shell.html
#drwxr-xr-x    2 root     root          4096 Jan 23 03:21 shells


# 通过-a覆盖cmd，从而与entrypoint拼成 ls -l . -a
docker run --name my-busybox --rm -it my-busybox:v1 -a

docker run --name my-busybox -it my-busybox:v1
```

注意：
1. 首先要COPY，把各个shell拷贝进镜像容器里，才能使用shell，因为后续的shell脚本都是在docker-server的容器里跑的；
2. `ENTRYPOINT`与`CMD`是拼在一起执行的，举个例子，下面这段代码，在`run`的时候，会执行一个指令，叫做`ls -l . ./shells/cmd_shell.sh 
   input-arg1`，会报错的，因为input-arg1不存在
```shell
ENTRYPOINT [ "ls", "-l", "." ]
CMD ["./shells/cmd_shell.sh", "input-arg1"]
```

# cmd与run的执行时机
上面那个dockerfile产出的容器，在运行启动之后，会自动关闭，这是因为，这个容器创建一次后，没有任何在工作的任务了，而docker与虚拟机不同的点是，每个容器实际上都是一个线程，如果这个线程没事情可做，那么这个线程就会被关闭掉，因此上面那个容器没有在启动之后会自动关闭，并且也无法让这个容器处于up状态；

参照dockerfile，可以改成下面这样
```dockerfile
FROM busybox:1.35.0

ENV VERSION=1.0

WORKDIR /root

COPY ./data/dat* ./data/
COPY ./shells ./shells/

RUN ["./shells/run_shell.sh", "2"]

ADD ./data/zipped_file.txt.tar.gz /root/data/tar


ENTRYPOINT [ "sh" ]
```
