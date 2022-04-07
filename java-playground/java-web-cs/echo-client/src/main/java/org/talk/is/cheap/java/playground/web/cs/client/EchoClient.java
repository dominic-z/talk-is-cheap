package org.talk.is.cheap.java.playground.web.cs.client;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.talk.is.cheap.java.playground.web.cs.util.ByteBufferUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author dominiczhu
 * @version 1.0
 * @title EchoClient
 * @date 2022/4/6 10:58 上午
 */
@Slf4j
public class EchoClient {

    private final int port;
    private final ExecutorService executorService;

    public EchoClient(int port) {
        this.port = port;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public void start() throws IOException {

        try (final SocketChannel sc = SocketChannel.open()) {
            sc.connect(new InetSocketAddress(port));

            executorService.submit(() -> {

                final ByteBuffer echoByteBuffer = ByteBuffer.allocate(3);
                byte[] incompleteEcho = new byte[0];
                try {
                    while (sc.read(echoByteBuffer) != -1) {
                        echoByteBuffer.flip();
                        for (int i = echoByteBuffer.position(); i < echoByteBuffer.limit(); i++) {
                            if (echoByteBuffer.get(i) == '\n') {
                                final int echoLength = i + 1 - echoByteBuffer.position();

                                final byte[] completeEcho = ByteBufferUtil.addAllByte(incompleteEcho, echoByteBuffer,
                                        echoLength);
                                log.info("echo is {}", new String(completeEcho, StandardCharsets.UTF_8));
                                incompleteEcho = new byte[0];

                            }
                        }

                        incompleteEcho = ByteBufferUtil.addAllByte(incompleteEcho, echoByteBuffer,
                                echoByteBuffer.remaining());

                        echoByteBuffer.clear();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    log.error("error when read", e);
                }

            });


            Scanner scan = new Scanner(System.in);
            while (true) {
                final String nextLine = scan.nextLine();
                log.info("[input] {}", nextLine);
                if (StringUtils.isBlank(nextLine)) {
                    break;
                }

                sc.write(StandardCharsets.UTF_8.encode(nextLine));
                sc.write(StandardCharsets.UTF_8.encode("\n"));

            }

        }
    }

}
