package cn.itcast.nio.c4;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
//import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

import static cn.itcast.nio.c2.ByteBufferUtil.debugAll;


/**
 * 对应4.6
 */
@Slf4j
public class MultiThreadServer {
    /**
     * 使用一个队列作为通信，并结合wakeup，让select执行通过，从而再遍历队列，完成注册；
     *
     * @throws IOException
     */
    @Test
    public void multiThreadServerWithQueue() throws IOException {
        Thread.currentThread().setName("boss");
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        Selector boss = Selector.open();
        SelectionKey bossKey = ssc.register(boss, 0, null);
        bossKey.interestOps(SelectionKey.OP_ACCEPT);
        ssc.bind(new InetSocketAddress(8080));
        // 1. 创建固定数量的 worker 并初始化
        QueueWorker[] workers = new QueueWorker[Runtime.getRuntime().availableProcessors()];

        for (int i = 0; i < workers.length; i++) {
            workers[i] = new QueueWorker("worker-" + i);
        }
        AtomicInteger index = new AtomicInteger();
        while (true) {
            boss.select();
            Iterator<SelectionKey> iter = boss.selectedKeys().iterator();
            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                iter.remove();
                if (key.isAcceptable()) {
                    SocketChannel sc = ssc.accept();
                    sc.configureBlocking(false);
                    log.debug("connected...{}", sc.getRemoteAddress());
                    // 2. 关联 selector
                    log.debug("before register...{}", sc.getRemoteAddress());
                    // round robin 轮询
                    workers[index.getAndIncrement() % workers.length].register(sc); // boss 调用 初始化 selector , 启动
                    // worker-0
                    log.debug("after register...{}", sc.getRemoteAddress());
                }
            }
        }
    }

    private static class QueueWorker implements Runnable {
        private Thread thread;
        private Selector selector;
        private String name;
        private volatile boolean start = false; // 还未初始化
        private ConcurrentLinkedQueue<Runnable> queue = new ConcurrentLinkedQueue<>();

        public QueueWorker(String name) {
            this.name = name;
        }

        // 初始化线程，和 selector
        public void register(SocketChannel sc) throws IOException {
            if (!start) {
                selector = Selector.open();
                thread = new Thread(this, name);
                thread.start();
                start = true;
            }

            // 这里使用了队列来添加一个runnable任务，然后配合selector的wakeup方法，
            // 确保sc.register(selector, SelectionKey.OP_READ)这个操作在子线程完成
            // 没有选择这个操作放在主线程里是有原因的，可以看下一个不使用Queue的方法的问题。
            queue.add(() -> {
                try {
                    sc.register(selector, SelectionKey.OP_READ);
                } catch (ClosedChannelException e) {
                    e.printStackTrace();
                }
            });
            // Causes the first selection operation that has not yet returned to return immediately.
            // If another thread is currently blocked in a selection operation then that invocation will return
            // immediately.
            selector.wakeup(); // 唤醒 select 方法，也就是说，select的返回值可能是null
        }

        @Override
        public void run() {
            while (true) {
                try {
                    final int select = selector.select();// worker-0  阻塞
                    log.info("select {}", select);
                    // 子线程中执行register中添加给queue的任务，也就是sc.register(selector, SelectionKey.OP_READ)
                    // 之所以使用queue，其实是使用queue当做主线程和子线程信息传递的桥梁，然后在子线程里执行sc.register(selector, SelectionKey.OP_READ)
                    // 为什么要这样做，可以看下面的multiThreadServer
                    while (!queue.isEmpty()) {
                        queue.poll().run();
                    }
                    Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
                    while (iter.hasNext()) {
                        SelectionKey key = iter.next();
                        iter.remove();
                        if (key.isReadable()) {
                            ByteBuffer buffer = ByteBuffer.allocate(16);
                            SocketChannel channel = (SocketChannel) key.channel();
                            log.debug("thread {} read...{}", this.name, channel.getRemoteAddress());
                            int readCnt = channel.read(buffer);
                            if(readCnt!=-1){
                                buffer.flip();
                                debugAll(buffer);
                            }else{
                                key.cancel();
                                channel.close();
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 其实也不用队列，因为只要理解了wakeup方法的机制，就可以知道这个问题怎么解决了；
     * 但是我个人认为此处仍然有问题
     * 2月17日：不用这个方法有问题的。
     *
     * @throws IOException
     */
    @Test
    public void multiThreadServer() throws IOException {
        Thread.currentThread().setName("boss");
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        Selector boss = Selector.open();
        SelectionKey bossKey = ssc.register(boss, 0, null);
        bossKey.interestOps(SelectionKey.OP_ACCEPT);
        ssc.bind(new InetSocketAddress(8080));
        // 1. 创建固定数量的 worker 并初始化
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        log.info("availableProcessors: {}", availableProcessors);
        Worker[] workers = new Worker[availableProcessors];

        for (int i = 0; i < workers.length; i++) {
            workers[i] = new Worker("worker-" + i);
        }
        AtomicInteger index = new AtomicInteger();
        while (true) {
            boss.select();
            Iterator<SelectionKey> iter = boss.selectedKeys().iterator();
            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                iter.remove();
                if (key.isAcceptable()) {
                    SocketChannel sc = ssc.accept();
                    sc.configureBlocking(false);
                    log.debug("connected...{}", sc.getRemoteAddress());
                    // 2. 关联 selector
                    log.debug("before register...{}", sc.getRemoteAddress());
                    // round robin 轮询
                    workers[index.getAndIncrement() % workers.length].register(sc); // boss 调用 初始化 selector , 启动
                    // worker-0
                    log.debug("after register...{}", sc.getRemoteAddress());
                }
            }
        }
    }
    static class Worker implements Runnable {
        private Thread thread;
        private Selector selector;
        private String name;

        private volatile boolean start = false; // 还未初始化

        public Worker(String name) {
            this.name = name;
        }
        // 初始化线程，和 selector

        public void register(SocketChannel sc) throws IOException {
            if (!start) {
                selector = Selector.open();
                thread = new Thread(this, name);
                thread.start();
                start = true;
            }


            // Causes the first selection operation that has not yet returned to return immediately.
            // 这个方法是说，将selector的first selection operation给唤醒，如果当前selector正在select方法中阻塞，那么就放行；
            // 如果当前selector还没有阻塞在select方法上，那么selector的下次select会被放行；
            // 但此处仍然有可能有问题，因为假如，wakeup执行完了，main线程被卡住了
            // worker线程又快速地到达了下一次select方法处，随后sc.register的执行仍然还是会被卡住

            // 20250203更新：没有问题啊，run方法跑在worker线程里，当前方法跑在boss主线程里
            // 20250204更新：相较于使用Queue的例子，本例中没有使用queue，而是直接在这个方法（也就是主线程）里执行register方法
            // 但这样做会有问题。
            // 因为，首先要知道，当selector在执行select()方法（during selection operation）的过程中的时候，
            // 如果另一个线程执行了sc.register(selector, SelectionKey.OP_READ, null);，那么之前的select()操作并不会感知到sc的read的ready-operation
            // 只有在下一次执行selector.select()方法的时候，才会感知到sc的read的ready-operation
            // 也因此，如果出现如下情形
            // 1. 子线程执行selector.select()
            // 2. 主线程线程执行selector.wakeup()
            // 3. 子线程优先执行又执行了到了下一次的selector.select()
            // 4. 主线程执行了sc.register(selector, SelectionKey.OP_READ, null);
            // 结果就是由于selector.select()执行在先，所以此次selection
            // operation并不会对sc这个通道的ready-operation有反应。即使scready了，selector也无法感知。
            // 所以才需要确保sc.register(selector, SelectionKey.OP_READ, null);
            // 和selection不会同时被执行，一个实现方式就是确保两个操作在同一个县城里。或者加锁也可以实现
            selector.wakeup(); // 唤醒 select 方法，也就是说，select的返回值可能是0
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            sc.register(selector, SelectionKey.OP_READ, null); // boss
        }
        @Override
        public void run() {
            while (true) {
                try {
                    log.info("before select");
                    final int select = selector.select();// worker-0  阻塞
                    log.info("select {}", select);
                    Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
                    while (iter.hasNext()) {
                        SelectionKey key = iter.next();
                        iter.remove();
                        if (key.isReadable()) {
                            ByteBuffer buffer = ByteBuffer.allocate(16);
                            SocketChannel channel = (SocketChannel) key.channel();
                            log.debug("thread {} read...{}", this.name, channel.getRemoteAddress());
                            channel.read(buffer);
                            buffer.flip();
                            debugAll(buffer);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }




    /**
     * 下面是一个有问题的代码，问题的原因与之前相同，子线程的selector在selection过程中，register进去的新的operation是无法被此次selection操作感知的
     * 问题描述：
     * 1. 下列方法执行顺序里，主线程将Worker启动，worker的selector.select()先执行了；
     * 2. 客户端链接服务器
     * 3. 随后主线程里执行向Worker的selector里注册了一个channel
     * 4. 客户端写数据
     * 问题发生了，worker一直卡在select()方法这里，因为此次select()方法并不会对第三部的注册的行为有反应，下次select才会有反应。
     *
     * @throws Exception
     */
    @Test
    public void bugMultiThreadServer() throws Exception {

        final ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.bind(new InetSocketAddress(8080));
        ssc.configureBlocking(false);
        Selector selector = Selector.open();
        ssc.register(selector, SelectionKey.OP_ACCEPT);

        BugWorker worker = new BugWorker("bug-worker-0");
        worker.register();

        while (true) {
            selector.select();
            final Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();
                if (key.isAcceptable()) {
                    SocketChannel sc = ssc.accept();
                    sc.configureBlocking(false);
                    log.debug("connected...{}", sc.getRemoteAddress());
                    // 2. 关联 selector
                    log.debug("before register...{}", sc.getRemoteAddress());
                    // round robin 轮询
                    sc.register(worker.selector, SelectionKey.OP_READ);
                    // worker-0
                    log.debug("after register...{}", sc.getRemoteAddress());
                }
            }

        }


    }

    private static class BugWorker implements Runnable {

        private Thread thread;
        private Selector selector;
        private String name;
        private volatile boolean start = false; // 还未初始化

        public BugWorker(String name) {
            this.name = name;
        }

        // 初始化线程，和 selector
        public void register() throws IOException {
            if (!start) {
                selector = Selector.open();
                thread = new Thread(this, name);
                thread.start();
                start = true;
            }
        }

        @Override
        public void run() {
            while (true) {
                try {
                    selector.select(); // worker-0  阻塞
                    Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
                    while (iter.hasNext()) {
                        SelectionKey key = iter.next();
                        iter.remove();
                        if (key.isReadable()) {
                            ByteBuffer buffer = ByteBuffer.allocate(16);
                            SocketChannel channel = (SocketChannel) key.channel();
                            log.debug("thread {} read...{}", this.name, channel.getRemoteAddress());
                            channel.read(buffer);
                            buffer.flip();
                            debugAll(buffer);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
