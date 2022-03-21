# 样例
```shell
docker rmi my-busybox:v1
docker build -t my-busybox:v1 .


# 加it是为了启动的时候能读取到各个指令的执行输出
docker run --name my-busybox --rm -it my-busybox:v1

# 通过-a覆盖cmd，从而与entrypoint拼成 ls -l . -a
docker run --name my-busybox --rm -it my-busybox:v1 -a
```

1. 首先要COPY，把各个shell拷贝进镜像容器里，才能使用shell，因为后续的shell脚本都是在docker-server的容器里跑的；
2. `ENTRYPOINT`与`CMD`总是在一起执行的，举个例子，下面这段代码，在`run`的时候，会执行一个指令，叫做`ls -l . ./shells/cmd_shell.sh 
   input-arg1`，会报错的，因为input-arg1，并不会像想想的，先执行ls，再执行cmd
```shell
ENTRYPOINT [ "ls", "-l", "." ]
CMD ["./shells/cmd_shell.sh", "input-arg1"]
```

3. 当执行`docker run --name my-busybox --rm -it my-busybox:v1 -a`的时候，-a会覆盖cmd里的参数，从而指令变成`ls -l . -a`