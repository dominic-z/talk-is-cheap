package org.talk.is.cheap.java.playground.web.cs.server;

import lombok.extern.slf4j.Slf4j;
import org.talk.is.cheap.java.playground.web.cs.util.ByteBufferUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author dominiczhu
 * @version 1.0
 * @title EchoServer
 * @date 2022/4/6 10:44 上午
 */
@Slf4j
public class BlockingEchoServer {

    private final int port;
    private final int size;
    private final ThreadPoolExecutor executor;


    private final ScheduledThreadPoolExecutor scheduleExecutor = new ScheduledThreadPoolExecutor(1);
    private final ConcurrentHashMap<Future<?>, Long> futures = new ConcurrentHashMap<>();

    public BlockingEchoServer(int port, int size) {
        this.port = port;
        this.size = size;
        this.executor = new ThreadPoolExecutor(1, 3, 3, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(2), new ThreadPoolExecutor.AbortPolicy());
    }

    public BlockingEchoServer(int port) {
        this(port, 1024);
    }

    public void start() {

        try (final ServerSocketChannel ssc = ServerSocketChannel.open().bind(new InetSocketAddress(port))) {
            expire();
            while (true) {
                final SocketChannel sc = ssc.accept();
                read(sc);
            }
        } catch (IOException acceptE) {
            log.error("error when accept", acceptE);
        }
    }

    private void read(SocketChannel sc) throws IOException {
        try {
            final Future<?> future = executor.submit(() -> {
                final ByteBuffer byteBuffer = ByteBuffer.allocate(size);
                byte[] incompleteLine = new byte[0];

                try {
                    while (sc.read(byteBuffer) != -1) {
                        byteBuffer.flip();

                        for (int i = byteBuffer.position(); i < byteBuffer.limit(); i++) {
                            if (byteBuffer.get(i) == '\n') {
                                int echoLength = i - byteBuffer.position() + 1;

                                final byte[] completeLine = ByteBufferUtil.addAllByte(incompleteLine,
                                        byteBuffer, echoLength);
                                sc.write(ByteBuffer.wrap(completeLine));

                                log.info("echo is {}", new String(completeLine, StandardCharsets.UTF_8));

                                incompleteLine = new byte[0];

                            }
                        }

                        incompleteLine = ByteBufferUtil.addAllByte(incompleteLine, byteBuffer,
                                byteBuffer.remaining());

                        byteBuffer.clear();

                    }
                } catch (Exception e) {
                    log.info("error e", e);
                }


            });

            futures.put(future, System.currentTimeMillis());
        } catch (RejectedExecutionException e) {
            log.error("RejectedExecutionException e", e);
            sc.close();
        }
    }


    private void expire() {
        scheduleExecutor.scheduleWithFixedDelay(() -> {
            log.info("try to purge expired task");

            final Iterator<Map.Entry<Future<?>, Long>> iterator = futures.entrySet().iterator();
            while (iterator.hasNext()) {
                final Map.Entry<Future<?>, Long> entry = iterator.next();
                if (System.currentTimeMillis() - entry.getValue() > 3000) {
                    log.info("expire future: {}, start timestamp: {}", entry.getKey(), entry.getValue());
                    entry.getKey().cancel(false);
                    iterator.remove();
                }
            }

            executor.purge();
        }, 2, 8, TimeUnit.SECONDS);

    }

}
