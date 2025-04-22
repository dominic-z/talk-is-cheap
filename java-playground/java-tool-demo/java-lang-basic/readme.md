# JDK

## JPS
查看java虚拟id的工具，是后续各种工具的基础

## jstat
可以用来查看java堆的情况、gc的情况，很适合用来监控内存使用情况和gc的整体概览。`jstat -gc -h3 31736 1000 10`表示分析进程 id 为 31736 的 gc 情况，每隔 1000ms 打印一次记录，打印 10 次停止，每 3 行后打印指标头部。
输出内容格式直接问豆包：“解释一下jstat的输出”

## jstack
这绝对是最有用的工具，他可以看到当前jvm正在执行什么方法，当出现java卡顿的时候，使用jstack可以直接看到java卡在哪里了。

直接问豆包：“解释一下jstack的输出”，可以结合WhileTureTask/DeaLockTest两个代码的方法来观察

## visual VM

### linux中的使用方法
直接去[官网](https://visualvm.github.io/index.html)下载，他们把windows的exe和linux的运行程序打包在一起了，下载解压后在bin目录里，linux下直接执行`./visualvm`就可以启动了。


linux需要`sudo ./visualvm --jdkhome /home/dominiczhu/Programs/jdk-17.0.6+10 `


### 链接远程jvm

java运行参数中新增
```shell
-Djava.rmi.server.hostname=外网访问 ip 地址
-Dcom.sun.management.jmxremote.port=60001   //监控的端口号
-Dcom.sun.management.jmxremote.authenticate=false   //关闭认证
-Dcom.sun.management.jmxremote.ssl=false
```

以GCDemo为例，增加jvm参数
```shell
-Djava.rmi.server.hostname=192.168.1.1
-Dcom.sun.management.jmxremote.port=60001
-Dcom.sun.management.jmxremote.ssl=false
-Dcom.sun.management.jmxremote.authenticate=true
-Dcom.sun.management.jmxremote.password.file=/home/dominiczhu/Coding/talk-is-cheap/java-playground/java-tool-demo/java-lang-basic/jmxremote.password 
-Dcom.sun.management.jmxremote.access.file=/home/dominiczhu/Coding/talk-is-cheap/java-playground/java-tool-demo/java-lang-basic/jmxremote.access
```

## jmap

启动GCDemp

```shell
jps
jmap -dump:format=b,file=./git_ignore/heap.hprof 10227
```

然后可以使用visual vm去load这个文件。

