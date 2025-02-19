package cn.itcast.nio.c4;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 对应4.5
 */
@Slf4j
public class WriteClient {

    /**
     * 一个同步的客户端。
     *
     * @throws IOException
     */
    @Test
    public void bioClient() throws IOException {
        SocketChannel sc = SocketChannel.open();
        sc.connect(new InetSocketAddress("localhost", 8080));

        sc.write(StandardCharsets.UTF_8.encode("hello"));
        // 3. 接收数据
        int count = 0;
        while (true) {
            ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);
            // debug here，模拟客户端每次运行的很慢，每次只能从服务器读取一点点数据
            // 客户端这里做的是同步的。
            int read = sc.read(buffer);
            log.info("current read: {}", read);
            // todo: 如果服务端断开链接，客户端也应该断开，而不是一直read
            count += read;
            log.info("total read count: {}", count);
            buffer.clear();
        }
    }

    @Test
    public void selectorClient() throws IOException {
        SocketChannel sc = SocketChannel.open(new InetSocketAddress("localhost", 8080));
        // 如果不加非阻塞，那么注册selector就会抛出异常
        sc.configureBlocking(false);
        log.info("sc: {}", sc);
        Selector selector = Selector.open();
        sc.register(selector, 0, new ArrayList<ByteBuffer>()).interestOps(SelectionKey.OP_READ);
        while (true) {
            int numKey = selector.select();
            log.info("num key: {}", numKey);
            Iterator<SelectionKey> it = selector.selectedKeys().iterator();
            while (it.hasNext()) {
                SelectionKey key = it.next();
                if (key.isReadable()) {
                    List<ByteBuffer> attachment = (ArrayList<ByteBuffer>) key.attachment();
                    // 这个channel其实 就是上面的sc
                    SocketChannel channel = (SocketChannel) key.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);
                    int read = channel.read(buffer);
                    attachment.add(buffer);
                    log.info("read this time: {}, total read: {}",read,
                            attachment.stream().map(b->b.position()).reduce((i,j)->i+j).get());
                }
            }
        }

    }
}
