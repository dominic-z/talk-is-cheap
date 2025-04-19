# jstack


## 豆包解释
`jstack` 是 Java 开发工具包（JDK）中的一个命令行工具，主要用于生成 Java 虚拟机（JVM）中线程的堆栈跟踪信息。通过这些信息，你可以分析线程的状态、查找死锁、排查性能问题等。以下为你详细介绍 `jstack` 的使用方法：

### 1. 前提条件
要使用 `jstack`，你需要安装 Java 开发工具包（JDK），并且确保 `jstack` 命令所在的目录已经添加到系统的环境变量 `PATH` 中。一般来说，`jstack` 位于 JDK 的 `bin` 目录下。

### 2. 基本语法
```plaintext
jstack [option] <pid>
```
- `option`：可选参数，用于指定不同的输出选项。
- `<pid>`：Java 进程的进程 ID。

### 3. 获取 Java 进程的 PID
在使用 `jstack` 之前，你需要先获取目标 Java 进程的 PID。可以使用以下方法：
- **`jps` 命令**：`jps` 是 JDK 提供的另一个命令行工具，用于列出当前系统中所有的 Java 进程及其 PID。
```bash
jps
```
运行该命令后，会输出类似如下信息：
```plaintext
1234 MainClass
5678 AnotherClass
```
其中，`1234` 和 `5678` 就是 Java 进程的 PID。

- **系统自带的进程管理工具**：在 Linux 系统中，可以使用 `ps` 命令来查找 Java 进程的 PID：
```bash
ps -ef | grep java
```
在 Windows 系统中，可以使用任务管理器来查看 Java 进程的 PID。

### 4. 使用 `jstack` 生成线程堆栈信息
获取到 Java 进程的 PID 后，就可以使用 `jstack` 命令来生成线程堆栈信息了。

#### 简单示例
```bash
jstack 1234
```
上述命令会将进程 ID 为 `1234` 的 Java 进程的线程堆栈信息输出到控制台。

#### 保存输出到文件
如果你想将线程堆栈信息保存到文件中，可以使用重定向符号 `>`：
```bash
jstack 1234 > thread_dump.txt
```
上述命令会将进程 ID 为 `1234` 的 Java 进程的线程堆栈信息保存到 `thread_dump.txt` 文件中。

### 5. 常用选项
- **`-l`**：除了输出线程的堆栈信息外，还会输出锁的附加信息，例如锁的持有者、等待锁的线程等。这对于查找死锁非常有用。
```bash
jstack -l 1234
```
- **`-F`**：当 `jstack` 无法正常连接到 Java 进程时，可以使用 `-F` 选项强制输出线程堆栈信息。
```bash
jstack -F 1234
```

### 6. 分析线程堆栈信息
生成的线程堆栈信息包含了每个线程的状态、调用栈等信息。以下是一些常见的线程状态及其含义：
- **RUNNABLE**：线程正在运行或准备运行。
- **WAITING**：线程正在等待其他线程的通知。
- **TIMED_WAITING**：线程正在等待其他线程的通知，但有一个超时时间。
- **BLOCKED**：线程正在等待获取锁。

通过分析线程的状态和调用栈，你可以找出线程的阻塞点、死锁等问题。例如，如果发现多个线程都处于 `BLOCKED` 状态，并且它们在等待同一个锁，那么很可能发生了死锁。

### 示例：查找死锁
下面是一个简单的 Java 代码示例，用于模拟死锁：
```java
public class DeadlockExample {
    private static final Object lock1 = new Object();
    private static final Object lock2 = new Object();

    public static void main(String[] args) {
        Thread thread1 = new Thread(() -> {
            synchronized (lock1) {
                System.out.println("Thread 1 acquired lock 1");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                synchronized (lock2) {
                    System.out.println("Thread 1 acquired lock 2");
                }
            }
        });

        Thread thread2 = new Thread(() -> {
            synchronized (lock2) {
                System.out.println("Thread 2 acquired lock 2");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                synchronized (lock1) {
                    System.out.println("Thread 2 acquired lock 1");
                }
            }
        });

        thread1.start();
        thread2.start();
    }
}
```
编译并运行上述代码后，使用 `jps` 命令获取 Java 进程的 PID，然后使用 `jstack -l <pid>` 命令生成线程堆栈信息。在输出信息中，你会看到类似如下的死锁信息：
```plaintext
Found one Java-level deadlock:
=============================
"Thread-1":
  waiting to lock monitor 0x0000000012345678 (object 0x00000000abcdef12, a java.lang.Object),
  which is held by "Thread-0"
"Thread-0":
  waiting to lock monitor 0x0000000087654321 (object 0x00000000fedcba98, a java.lang.Object),
  which is held by "Thread-1"

Java stack information for the threads listed above:
===================================================
"Thread-1":
        at DeadlockExample.lambda$main$1(DeadlockExample.java:25)
        - waiting to lock <0x00000000abcdef12> (a java.lang.Object)
        - locked <0x00000000fedcba98> (a java.lang.Object)
"Thread-0":
        at DeadlockExample.lambda$main$0(DeadlockExample.java:12)
        - waiting to lock <0x00000000fedcba98> (a java.lang.Object)
        - locked <0x00000000abcdef12> (a java.lang.Object)

Found 1 deadlock.
```
通过这些信息，你可以清楚地看到哪个线程在等待哪个锁，以及哪个锁被哪个线程持有，从而找出死锁的原因。

## 说明
jstack是用来查看java进程各个线程执行情况的，包括主线程、用户创建的线程以及一些内置的线程，以下面为例，这是一个main线程和一个死循环线程thread-1，thread-1一直处于runnable状态，他在调用，SingleThread.java:14这行代码，他占用了cpu 193秒，elapsed指的是这个线程的存活时间。
```shell

"main" #1 prio=5 os_prio=0 cpu=38.15ms elapsed=193.62s tid=0x00007facf8027350 nid=0x2b944 in Object.wait()  [0x00007facffa04000]
   java.lang.Thread.State: WAITING (on object monitor)
        at java.lang.Object.wait(java.base@17.0.6/Native Method)
        - waiting on <0x0000000715ffc540> (a java.lang.Thread)
        at java.lang.Thread.join(java.base@17.0.6/Thread.java:1304)
        - locked <0x0000000715ffc540> (a java.lang.Thread)
        at java.lang.Thread.join(java.base@17.0.6/Thread.java:1372)
        at concurrent.jstack.SingleThread.jstackThread(SingleThread.java:24)
        at concurrent.jstack.SingleThread.main(SingleThread.java:32)


"Thread-0" #15 prio=5 os_prio=0 cpu=193334.07ms elapsed=193.58s tid=0x00007facf8252ff0 nid=0x2b95c runnable  [0x00007facd88fc000]
   java.lang.Thread.State: RUNNABLE
        at concurrent.jstack.SingleThread$ThreadRunner.run(SingleThread.java:14)
        at java.lang.Thread.run(java.base@17.0.6/Thread.java:833)

```