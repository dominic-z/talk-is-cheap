# p112
这个客户端的流程是：当客户端连接到了服务端后，在客户端启动一个线程，用来发送消息等操作

# p117

原始代码有点问题，在Message类messageClasses里没有PingMassage的类信息，因此反序列化的时候有可能有问题

# p122
我理解是说，TimeOut异常还是connection异常，要看谁先触发，比如超时时间为0.001秒，那可能此时并没有找到服务器就超时了，就抛出异常了；

# p123
视频说的是promise指的就是ChannelFuture，即connect的返回值

# p129

ALLOCATOR参数是控制`ByteBuf buf = ctx.alloc().buffer();`，例如池化非池化等等；

RESV_ALLOCATOR参数是控制`public void channelRead(ChannelHandlerContext ctx, Object msg)`的msg，
指的是netty的缓冲区，即控制一些默认的handler之间传递的bytebuf的大小

# p135
重新理解Future，Future是一个未来对象；如果一个操作是一个异步操作，那么就会返回一个Future对象，可以通过对这个future对象注册一些listener来实现回调；

例如closeFuture方法返回的是channelFuture，可以向其中注册监听器，指的就是如果未来调用了close的话，那么就就会按照这个future里注册的监听器来执行回调；

write与connect类似，返回的都是channelFuture，可以向其中注册监听器，指的都是当未来真正执行sync方法的时候，会按照当初注册的来进行回调

# p139
监听端口的channel是一个ServerSocketChannel（ssc），并且将NioServerSocketChannel（nssc）作为ssc的一个attachment，老师是说，当ssc监听到连接的时候，后续操作的时候，通过这个attachment来让后续的操作交给NIO的nssc来进行操作；

# p146
为啥有两个selector，unwrappedSelector是原始的selector，只不过用反射替换了其中两个变量的实现；
那个selector是对unwrappedSelector的代理；


# p147
NioEventLoopd的run方法，讲课老师说，该方法是用来不断轮询自旋看是否有需要执行的方法；

# p151
接着P147的来说，这个run方法是干什么的呢？
首先需要记住，这个NioEventLoop不止要处理io，也要处理一些普通任务（例如处理taskQueue里的任务）；且每个NioEventLoop都是单线程的；
可以将EventLoop看做一个普通线程池+处理io的对象；

他就一开始就是一个switch，这个switch的判断条件为`selectStrategy.calculateStrategy(selectNowSupplier, hasTasks())`；
含义可以通过源码看得出，如果
- 当前EventLoop里的任务队列为空 -> 说明当前任务队列里没事儿干；
- 或者说当前任务队列不为空，但selector的selectNow发现当前没有任何io事件 -> 说明当前selector没有什么io事件触发；

在上述情况下，那么就会进入到select分支；

进入这个select分支做的事情，简单来说就是让selector去select等待io事件，
只不过除了通过select等待事件，如果
- 当前hasTask会返回true且wakenUp为false；
- 当前loop被添加了新的任务（执行execute方法）从而调用了wakenUp方法，会尝试跳过此次select，见如下代码；
```java
if (hasTasks() && wakenUp.compareAndSet(false, true)) {
                    selector.selectNow();
                    selectCnt = 1;
                    break;
                }
```
- 等等

不管怎么样，如果select()方法返回了，说明此时要么说明有提交了普通任务，要么就是selector有事件发生了，select方法就会返回；
```java
if (selectedKeys != 0 || oldWakenUp || wakenUp.get() || hasTasks() || hasScheduledTasks()) {
                    // - Selected something,
                    // - waken up by user, or
                    // - the task queue has a pending task.
                    // - a scheduled task is ready for processing
                    break;
                }
```


他就会去往后执行run方法之后的代码，处理连接以及处理io事件等等；其中`processSelectedKeys`就将各种select的key交给NioServerSocketChannel来处理(通过attachment拿到)；
而runAllTasks即去处理各种任务队列里的任务；
```java
try {
                        processSelectedKeys();
                    } finally {
                        // Ensure we always run tasks.
                        runAllTasks();
                    }
```


# p156`
这一节需要启动客户端来调试；

记住，NioEventLoop的主调入口就是run方法，这个方法通过一个死循环来判断当前是否有事件发生，真正处理事件的方法，就是`processSelectedKeys`；
但如果此时没有事件，该方法里面就立刻返回了；

还记得在ServerBootstrap之中，init过程之中，给nssc的流水线注册了一个handler，即ServerBootstrapAcceptor这个内部类；

就是这个handler，在accept之后会被触发（通过AbstractNioMessageChannel的fireRead方法）

具体做事情的地方，就是`ServerBootstrapAcceptor`的`channelRead`方法 该方法里的child，可以理解为，nssc在accept之后返回的那个用于信息通讯的channel，
并会触发register操作，这个操作里做了很多事情，例如：
- 最重要的将accept之后的channel（记为nsc）绑定给childGroup，所以开始的时候，用的一直是childHandler方法来绑定各种handler；
- 对nsc初始化：在`pipeline.invokeHandlerAddedIfNeeded();`这个代码，调用我们传给childHandler的`ChannelInitializer`的对象的初始化方法，从而对这个NioSocketChannel添加各种handler
- 并且再让这个nsc监听read事件

# p157
视频代码位于`NioEventLoop`的`processSelectedKey`方法，即当有可读事件发生时，会走到read方法。