package cn.itcast.nio.c5;

import cn.itcast.nio.c2.ByteBufferUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static cn.itcast.nio.c2.ByteBufferUtil.debugAll;

/**
 * 对应5.3
 */
@Slf4j
public class AioFileChannel {
    @Test
    public void AsyncReadFile() {
        try (AsynchronousFileChannel channel = AsynchronousFileChannel.open(Paths.get("data.txt"),
                StandardOpenOption.READ)) {
            // 参数1 ByteBuffer
            // 参数2 读取的起始位置
            // 参数3 附件
            // 参数4 回调对象 CompletionHandler
            ByteBuffer buffer = ByteBuffer.allocate(16);
            log.debug("read begin...");
            channel.read(buffer, 0, buffer, new CompletionHandler<Integer, ByteBuffer>() {
                @Override // read 成功
                public void completed(Integer result, ByteBuffer attachment) {
                    log.debug("read completed...{}", result);
                    attachment.flip();
                    debugAll(attachment);
                }

                @Override // read 失败
                public void failed(Throwable exc, ByteBuffer attachment) {
                    exc.printStackTrace();
                }
            });

            // 防止异步回调没执行完，就被close了；
            System.in.read();
            log.debug("read end...");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void AioServer() {
        try {
            AsynchronousServerSocketChannel assc = AsynchronousServerSocketChannel.open();
            assc.bind(new InetSocketAddress(8080));
            assc.accept("this is an attachment", new AcceptHandler(assc));
            // 防止主线程终结
            System.in.read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @AllArgsConstructor
    static class AcceptHandler implements CompletionHandler<AsynchronousSocketChannel, Object> {

        private AsynchronousServerSocketChannel ssc;

        private static int count = 0;

        @Override
        public void completed(AsynchronousSocketChannel sc, Object attachment) {
            try {
                count += 1;
                log.info("current thread: {}, remote address:{} ", Thread.currentThread().getName(),
                        sc.getRemoteAddress());
                ByteBuffer readBuffer = ByteBuffer.allocate(16);
                sc.read(readBuffer, attachment, new ReadHandler(readBuffer, sc));
                ByteBuffer writeBuffer = StandardCharsets.UTF_8.encode("write something and something");
                sc.write(writeBuffer, attachment, new WriteHandler(writeBuffer, sc));

                // 处理完第一个 accpet 时，需要再次调用 accept 方法来处理下一个 accept 事件
                ssc.accept(String.format("%s %d", attachment, count), this);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

        @Override
        public void failed(Throwable exc, Object attachment) {
            log.error("error accept: ", exc);
        }
    }

    @AllArgsConstructor
    static class ReadHandler implements CompletionHandler<Integer, Object> {

        private ByteBuffer buffer;
        private AsynchronousSocketChannel sc;


        @Override
        public void completed(Integer result, Object attachment) {
            if (result == -1) {
                log.info("done read, close channel: {}", this.sc);
                try {
                    sc.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return;
            }
            log.info("read: {}", result);
            log.info("attachment: {}", attachment);
            buffer.flip();
            ByteBufferUtil.debugAll(this.buffer);
            buffer.clear();
            this.sc.read(buffer, attachment, this);
        }

        @Override
        public void failed(Throwable exc, Object attachment) {
            // 与bio或者selector不同，如果客户端关闭了channel，selector的read方法会返回-1，
            // 而在aio中，客户端关闭channel，这个failed方法会触发，而不是completed方法触发
            log.error("error read: ", exc);
        }
    }

    @AllArgsConstructor
    static class WriteHandler implements CompletionHandler<Integer, Object> {

        private ByteBuffer buffer;
        private AsynchronousSocketChannel sc;

        @Override
        public void completed(Integer result, Object attachment) {
            log.info("buffer position: {}, result: {}", buffer.position(),result);
            if (result == 0) {
                log.info("done write {}", this.sc);
            }else{
                this.sc.write(this.buffer, attachment, this);
            }
        }

        @Override
        public void failed(Throwable exc, Object attachment) {
            log.error("error write: ", exc);

        }
    }


    @Test
    public void readClient() {
        try (SocketChannel sc = SocketChannel.open(new InetSocketAddress("localhost", 8080))) {

            ByteBuffer writeBuffer = StandardCharsets.UTF_8.encode("write something to server, hello async server");
            while (true) {
                int writeCnt = sc.write(writeBuffer);
                log.info("write done: {}", writeCnt);
                if (!writeBuffer.hasRemaining()) {
                    break;
                }
            }
            // 关闭输出，这会导致服务端的read操作返回-1，从而触发服务端关闭channel，而如果服务端触发了关闭channel。
            // 下面的sc.read操作仍能正常读取数据，在读取完了数据后，因此服务端关闭了channel，那么客户端的最后一次sc.read才会返回-1
            // 从而跳出循环，当然服务端也可以调用shutdownOutput操作；
            // 我发现shutdownOutput和shutdownInput可以用于服务端和客户端告知对方数据已经发完了。
            sc.shutdownOutput();

            while (true) {
                ByteBuffer readBuffer = ByteBuffer.allocate(10);
                int readCnt = sc.read(readBuffer);
                log.info("read count: {}", readCnt);
                debugAll(readBuffer);
                if (readCnt == -1) {
                    break;
                }
            }




        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
