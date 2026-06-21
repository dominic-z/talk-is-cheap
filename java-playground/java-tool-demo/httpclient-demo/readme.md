# 在等待http的io返回的时候，java线程的状态和内核线程的状态是什么样的？

## 环境准备
通过[hello-spring-boot-starter-web](..%2F..%2F..%2Fspring%2Fspring-boot%2Fhello-spring-boot%2Fhello-spring-boot-starter-web)这个工程提供了一个耗时的接口，接口调用指定为40s返回

## 查看线程状态



```shell
# 查看java的pid
jps
# 22034 JUnitStarter

# 查看所有相关的线程
ps -T -p 20893
#    PID    SPID TTY          TIME CMD
#  20893   20893 ?        00:00:00 java
#  20893   20895 ?        00:00:36 java
#  20893   20896 ?        00:00:00 GC Thread#0
#  20893   20897 ?        00:00:00 G1 Main Marker
# 你问的 ps -T 命令中输出的 SPID 是 Thread ID（线程 ID），也叫 Light Weight Process ID（LWP，轻量级进程 ID），它是操作系统给每个线程分配的唯一标识，用于区分同一个进程下的不同线程。


# 查看线程的状态 
# 带上-H参数之后，除了20893这个进程，还会有相关的SPID
top -H -p 22034
```

查看java线程状态
```shell
# 找到目标spid，将其转化为16进制
printf "%x\n" 20895

# 查看线程状态
jstack 20893

# 在结果中找到nid=上述16进制输出的项目，即为状态

```

结论，当等待io的时候，线程的状态为S
状态码	含义	内核线程常见场景
R	Running/Runnable	正在占用 CPU 或等待 CPU 调度
S	Interruptible Sleep	等待事件（如 IO 完成、信号），可被唤醒
D	Uninterruptible Sleep	等待磁盘 IO，不可被信号打断（如 [jbd2/sda1-8] 磁盘日志线程）
I	Idle	内核线程特有的空闲状态（等价于用户态的 S）
Z	Zombie	极少出现，内核线程终止后会被内核直接回收

而java线程的状态为runnable

[linux线程状态和java线程状态的映射关系](https://www.doubao.com/thread/w3a436b8386f3d099)
