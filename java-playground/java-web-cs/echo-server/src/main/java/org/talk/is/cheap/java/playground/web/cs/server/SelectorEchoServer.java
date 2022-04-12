package org.talk.is.cheap.java.playground.web.cs.server;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author dominiczhu
 * @version 1.0
 * @title SelectorEchoServer
 * @date 2022/4/8 3:15 下午
 */
@Slf4j
public class SelectorEchoServer {

    private final int port;
    private final int byteBufferCap;
    private final ThreadPoolExecutor executor;
    private final ScheduledThreadPoolExecutor expireExecutor;
    private final ConcurrentHashMap<SelectionKey, Long> selectionKeys = new ConcurrentHashMap<>();

    private final static long TIMEOUT_PERIOD = 10000;

    private final AtomicLong cyclicCounter = new AtomicLong(0);

    private List<SelectorWrapper> selectors;

    public SelectorEchoServer(int port, int byteBufferSize, int poolSize) {
        this.port = port;
        this.byteBufferCap = byteBufferSize;

        // 核心线程池
        this.executor = new ThreadPoolExecutor(poolSize, poolSize, 3, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1), new ThreadPoolExecutor.AbortPolicy());
        this.expireExecutor = new ScheduledThreadPoolExecutor(1, new ThreadPoolExecutor.AbortPolicy());

    }


    public void start() {

        try (ServerSocketChannel ssc = ServerSocketChannel.open();
             Selector acceptSel = Selector.open()) {

            ssc.bind(new InetSocketAddress(port));

            ssc.configureBlocking(false);
            ssc.register(acceptSel, SelectionKey.OP_ACCEPT);

            expireTimeoutChannel();
            prepareSelectors();
            while (true) {
                acceptSel.select();

                final Iterator<SelectionKey> iterator = acceptSel.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    final SelectionKey acceptKey = iterator.next();
                    iterator.remove();

                    register(acceptKey);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void expireTimeoutChannel() {
        log.info("启动超时通道过期检查线程");
        this.expireExecutor
                .scheduleWithFixedDelay(() -> {
                    final Iterator<Map.Entry<SelectionKey, Long>> iterator = this.selectionKeys.entrySet().iterator();
                    while (iterator.hasNext()) {
                        final Map.Entry<SelectionKey, Long> next = iterator.next();
                        final SelectionKey key = next.getKey();
                        final Long value = next.getValue();
                        log.info("key: {}, 上次活跃时间: {}", key, value);
                        if (System.currentTimeMillis() - value > TIMEOUT_PERIOD) {
                            log.info("key: {}, 上次活跃时间距今超过{}毫秒", key, TIMEOUT_PERIOD);
                            try {
                                key.channel().close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            iterator.remove();
                        }

                    }

                }, 3, 5, TimeUnit.SECONDS);
    }

    private void prepareSelectors() throws IOException {
        selectors = new ArrayList<>();
        for (int i = 0; i < this.executor.getCorePoolSize(); i++) {
            final SelectorWrapper selectorWrapper = new SelectorWrapper(Selector.open());
            selectors.add(selectorWrapper);
            // 每个线程拥有自己的selector，线程之间selector独立
            this.executor.execute(new Worker(selectorWrapper));
        }
    }


    /**
     * 用线程池完成echo server的io
     *
     * @param acceptKey
     */
    private void register(SelectionKey acceptKey) {
        final long round = cyclicCounter.getAndIncrement();
        // 分配selector
        final SelectorWrapper selectorWrapper = this.selectors.get((int) (round % this.selectors.size()));

        final ServerSocketChannel ssc = (ServerSocketChannel) acceptKey.channel();

        SocketChannel sc = null;
        try {
            sc = ssc.accept();
            sc.configureBlocking(false);

            final Attachment attachment = new Attachment(this.byteBufferCap);
            final SelectionKey register = selectorWrapper.register(sc, SelectionKey.OP_READ, attachment);
            this.selectionKeys.put(register, System.currentTimeMillis());


        } catch (Exception e) {
            e.printStackTrace();
            if (sc != null) {
                try {
                    sc.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

    }


    private static class SelectorWrapper {

        private final Selector selector;

        private volatile boolean registering = false;

        public SelectorWrapper(Selector selector) {
            this.selector = selector;
        }

        private synchronized int select() throws IOException, InterruptedException {
            if (registering) {
                this.wait();
            }
            return this.selector.select();
        }

        private Set<SelectionKey> selectedKeys() {
            return this.selector.selectedKeys();
        }

        public SelectionKey register(SelectableChannel selectableChannel, int op, Object attachment) throws ClosedChannelException {
            registering = true;
            this.selector.wakeup();

            synchronized (this) {
                final SelectionKey register = selectableChannel.register(selector, op, attachment);

                registering = false;
                this.notifyAll();

                return register;
            }


        }
    }


    /**
     * 执行io的线程，每个worker对象拥有自己的selector
     */
    private class Worker implements Runnable {

        private final SelectorWrapper selectorWrapper;

        public Worker(SelectorWrapper selectorWrapper) {
            this.selectorWrapper = selectorWrapper;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    final int select = selectorWrapper.select();

                    if (select == 0) {
                        continue;
                    }

                    final Iterator<SelectionKey> iterator = selectorWrapper.selectedKeys().iterator();
                    while (iterator.hasNext()) {
                        final SelectionKey selKey = iterator.next();
                        iterator.remove();
                        SelectorEchoServer.this.selectionKeys.put(selKey, System.currentTimeMillis());

                        if (selKey.isReadable()) {
                            read(selKey);
                        }

                        // 防止read过程中关闭了连接
                        if (selKey.isValid() && selKey.isWritable()) {
                            write(selKey);
                        }
                    }


                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }


        private void read(SelectionKey selKey) {
            final Attachment attachment = (Attachment) selKey.attachment();

            ByteBuffer byteBuffer = attachment.readByte;
            final SocketChannel sc = (SocketChannel) selKey.channel();

            try {
                final int read = sc.read(byteBuffer);

                if (read == -1) {
                    log.info("sc: {} closed", sc);

//                    selector关闭连接有两种手段，
//                    1. key.cancel内部调用的是channel的kill方法，通过native方法将描述符给毙掉，但是这种行为无法及时通知到client，表现就是client一直发消息但是得不到任何回应；
//                    2. channel.close() 由于socketChannel实现了AbstractSelectableChannel，因此每个channel会持有被注册selector时的selectionKey，在close的时候，channel不仅会关闭资深，内部还会调用每个key的cancel方法
//                    个人觉得2更优雅一点。
                    sc.close();
                } else {
                    byteBuffer.flip();
                    for (int i = byteBuffer.position(); i < byteBuffer.limit(); i++) {
                        if (byteBuffer.get(i) != '\n') {
                            continue;
                        }

                        byte[] line = new byte[i - byteBuffer.position() + 1];
                        byteBuffer.get(line);
                        log.info("sc: {} line: {}", sc, new String(line, StandardCharsets.UTF_8));

                        final ByteBuffer writeByteBuffer = ByteBuffer.wrap(line);
                        attachment.writeByteDeque.offer(writeByteBuffer);
                        selKey.interestOps(SelectionKey.OP_WRITE | SelectionKey.OP_READ);
                    }

                    // 切换为向buffer写模式
                    byteBuffer.compact();

                    attachment.update();
                }

            } catch (IOException e) {
                e.printStackTrace();
                selKey.cancel();
            }

        }

        private void write(SelectionKey selKey) {

            final Attachment attachment = (Attachment) selKey.attachment();
            final ByteBuffer writeByteBuffer = attachment.writeByteDeque.peek();
            final SocketChannel sc = (SocketChannel) selKey.channel();

            try {
                sc.write(writeByteBuffer);
                // 如果队列头的bytebuffer为空，则出队
                if (!writeByteBuffer.hasRemaining()) {
                    attachment.writeByteDeque.poll();
                }

                if (attachment.writeByteDeque.isEmpty()) {
                    selKey.interestOps(SelectionKey.OP_READ);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }


    }


    private class Attachment {

        // 需要写回client的bytebuffer队列；
        Deque<ByteBuffer> writeByteDeque = new LinkedList<>();

        // 从client读取的byteBuffer
        ByteBuffer readByte;

        public Attachment(int byteBufferCap) {
            this.readByte = ByteBuffer.allocate(byteBufferCap);
        }

        private void update() {

            if (!readByte.hasRemaining()) {
                ByteBuffer expand = ByteBuffer.allocate(readByte.capacity() * 2);
                readByte.flip();
                expand.put(readByte);
                this.readByte = expand;
            } else if (readByte.position() < readByte.capacity() / 4 && readByte.capacity() / 2 > SelectorEchoServer.this.byteBufferCap) {
                // 当有很多空间空闲的时候，收缩
                ByteBuffer shrink = ByteBuffer.allocate(readByte.capacity() / 2);
                readByte.flip();
                shrink.put(readByte);
                this.readByte = shrink;
            }
        }

    }

}
