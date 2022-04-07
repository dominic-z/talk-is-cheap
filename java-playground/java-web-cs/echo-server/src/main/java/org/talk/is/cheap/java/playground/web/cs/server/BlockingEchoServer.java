package org.talk.is.cheap.java.playground.web.cs.server;

import lombok.extern.slf4j.Slf4j;
import org.talk.is.cheap.java.playground.web.cs.util.ByteBufferUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
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
    private final ThreadPoolExecutor threadPoolExecutor;

    public BlockingEchoServer(int port, int size) {
        this.port = port;
        this.size = size;
        this.threadPoolExecutor = new ThreadPoolExecutor(5, 10, 3, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1024), new ThreadPoolExecutor.AbortPolicy());
    }

    public BlockingEchoServer(int port) {
        this(port, 1024);
    }

    public void start() throws IOException {

        try (final ServerSocketChannel ssc = ServerSocketChannel.open().bind(new InetSocketAddress(port))) {

            while (true) {
                final SocketChannel sc = ssc.accept();

                try {
                    threadPoolExecutor.execute(() -> {
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
                } catch (RejectedExecutionException e) {
                    log.error("RejectedExecutionException e", e);
                    sc.close();
                }
            }
        }
    }
}
