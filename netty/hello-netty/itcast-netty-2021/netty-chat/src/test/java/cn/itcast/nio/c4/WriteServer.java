package cn.itcast.nio.c4;

import cn.itcast.nio.c2.ByteBufferUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Iterator;


/**
 * 对应4.5
 * <p>
 * 突然发现一个问题，至今的例子都在依赖客户端断开连接，而没有服务端主动断开链接的例子。
 */
@Slf4j
public class WriteServer {
    /**
     * 问题背景：当server向client写数据的时候，有时当前channel还没有准备好写，那么强行往里写会导致write的数据量为0
     * 这样会造成程序空转
     * 解决：将当前channel交给selector处理并标记当前channel也需要关注写事件，当ready to write，就进行写入
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        Selector selector = Selector.open();
        ssc.register(selector, SelectionKey.OP_ACCEPT);
        ssc.bind(new InetSocketAddress(8080));
        while (true) {
            selector.select();
            Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                iter.remove();
                if (key.isAcceptable()) {
                    SocketChannel sc = ssc.accept();
                    sc.configureBlocking(false);
                    SelectionKey sckey = sc.register(selector, 0, null);
                    sckey.interestOps(SelectionKey.OP_READ);
                    // 1. 向客户端发送大量数据
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < 5000000; i++) {
                        sb.append("a");
                    }
                    ByteBuffer buffer = Charset.defaultCharset().encode(sb.toString());

                    // 2. 返回值代表实际写入的字节数
                    int write = sc.write(buffer);
                    log.info("本次写入： {}, 累计写入： {}", write, buffer.position());

                    // 3. 判断是否有剩余内容
                    if (buffer.hasRemaining()) {
                        // 如果有剩余的内容，说明还没写完，那就需要监听这个客户端的channel是否可写，等可写的时候再把剩余的信息写回客户端。
                        // 4. 关注可写事件   1                     4
                        sckey.interestOps(sckey.interestOps() + SelectionKey.OP_WRITE);
                        // sckey.interestOps(sckey.interestOps() | SelectionKey.OP_WRITE);
                        // 5. 把未写完的数据挂到 sckey 上
                        sckey.attach(buffer);
                    }
                } else {
                    SocketChannel sc = (SocketChannel) key.channel();

                    if (key.isWritable()) {
                        // 说明这个channel已经准备好被写入了。
                        ByteBuffer buffer = (ByteBuffer) key.attachment();
                        /**
                         * todo: 这块需要优化，因为可能读取sc的时候，client断开，导致write会抛出ioexception异常。
                         *      可以通过将客户端变成blocking的模式进行模拟,就是将客户端的sc.configureBlocking(false)注释掉来模拟客户端断开
                         *
                         */
                        //
                        int write = sc.write(buffer);
                        log.info("本次写入： {}, 累计写入： {}", write, buffer.position());

                        // 6. 清理操作
                        if (!buffer.hasRemaining()) {
                            // 为何要取消，不取消的话channel一直可以写，一直会导致selector被触发可写事件
                            key.attach(null); // 需要清除buffer
                            key.interestOps(key.interestOps() - SelectionKey.OP_WRITE);//不需关注可写事件
//                            小实验，这里如果close的话，后面在key.isReadable()会抛出异常，并且如果sc已经close了，那么key也是会cancel的
//                            key.cancel();
//                            sc.close();
                        }
                    }
                    if (key.isReadable()) {
                        // 这是我补充的，用于检测客户端是否已经断开链接，如果断开的话，服务端也要及时释放资源，断开连接等。
                        log.info("key is readable: {}", key);
                        ByteBuffer allocate = ByteBuffer.allocate(16);
                        if (sc.read(allocate) == -1) {
                            key.cancel();
                            sc.close();
                            log.info("close: {}", sc);
                        }
                        ByteBufferUtil.debugAll(allocate);
                    }
                }
            }
        }
    }
}
