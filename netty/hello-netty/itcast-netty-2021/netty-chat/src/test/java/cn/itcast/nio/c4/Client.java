package cn.itcast.nio.c4;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

// 这个类需要edit run configuration，把allow multi instance打开，这样可以运行多个客户端了。
public class Client {
    public static void main(String[] args) throws IOException {
        SocketChannel sc = SocketChannel.open();
        sc.connect(new InetSocketAddress("localhost", 8080));
        SocketAddress address = sc.getLocalAddress();

        sc.write(StandardCharsets.UTF_8.encode("client-abcds"));
        sc.write(StandardCharsets.UTF_8.encode("33\n"));
        System.out.println("waiting...");
//        模拟不关闭连接
        sc.close();
    }
}