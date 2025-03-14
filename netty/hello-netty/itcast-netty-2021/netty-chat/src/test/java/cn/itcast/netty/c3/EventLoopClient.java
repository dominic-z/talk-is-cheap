package cn.itcast.netty.c3;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Random;
import java.util.UUID;


/**
 * 对应3.1
 */
@Slf4j
public class EventLoopClient {
    public static void main(String[] args) throws InterruptedException {
        // 2. 带有 Future，Promise 的类型都是和异步方法配套使用，用来处理结果
        ChannelFuture channelFuture = new Bootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override // 在连接建立后被调用
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new StringEncoder());
                    }
                })
                // 1. 连接到服务器
                // 异步非阻塞, main 发起了调用，真正执行 connect 是 nio 线程
                .connect(new InetSocketAddress("localhost", 8080)); // 1s 秒后

        // 2.1 使用 sync 方法同步处理结果
//        channelFuture.sync(); // 阻塞住当前线程，直到nio线程连接建立完毕
//        Channel channel = channelFuture.channel();
////        log.debug("{}", channel); // 这一步log的时长足够连接建立了，为了突出实验效果，将这个log注释，让主线程在连接未建立的时候进行数据发送
//        channel.writeAndFlush("hello, world");
//        log.debug("{}", channel);

        // 2.2 使用 addListener(回调对象) 方法异步处理结果
        UUID uuid = UUID.randomUUID();
        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            // 在 nio 线程连接建立好之后，会调用 operationComplete
            public void operationComplete(ChannelFuture future) throws Exception {

                Channel channel = future.channel();
                log.debug("{}", channel);
                channel.writeAndFlush("hello, world"+uuid);
                // 中间加一段sleep，然后启动多个client，模拟多个客户端链接服务端，然后看服务端的代码可以发现，
                // 客户端与服务端的会话会被服务端分配到对应的线程（或者说EventLoop）身上，并且会话和对应的eventloop是绑定的
                Thread.sleep(new Random().nextInt(10000)+10000);
                channel.writeAndFlush("hello, world"+uuid);
            }
        });
    }
}
