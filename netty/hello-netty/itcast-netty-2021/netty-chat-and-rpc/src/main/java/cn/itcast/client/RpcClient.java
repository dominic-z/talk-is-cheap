package cn.itcast.client;

import cn.itcast.message.RpcRequestMessage;
import cn.itcast.protocol.MessageCodecSharable;
import cn.itcast.protocol.ProcotolFrameDecoder;
import cn.itcast.client.handler.RpcResponseMessageHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RpcClient {
    public static void main(String[] args) {
        NioEventLoopGroup group = new NioEventLoopGroup();
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
        MessageCodecSharable MESSAGE_CODEC = new MessageCodecSharable();
        RpcResponseMessageHandler RPC_HANDLER = new RpcResponseMessageHandler();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.group(group);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new ProcotolFrameDecoder());
                    ch.pipeline().addLast(LOGGING_HANDLER);
                    ch.pipeline().addLast(MESSAGE_CODEC);
                    ch.pipeline().addLast(RPC_HANDLER);
                }
            });
            final ChannelFuture channelFuture = bootstrap.connect("localhost", 8080);
            log.info("only channel: {}",channelFuture.channel()); // 强调，如果不sync的话，此时这个bootStrap是未连接上的
            Channel channel = channelFuture.sync().channel();


//            // 老师的写法
//            ChannelFuture future = channel.writeAndFlush(new RpcRequestMessage(
//                    1,
//                    "cn.itcast.server.service.HelloService",
//                    "sayHello",
//                    String.class,
//                    new Class[]{String.class},
//                    new Object[]{"张三"}
//            )).addListener(promise -> {
//                // 排查异常的方式
//                if (!promise.isSuccess()) {
//                    Throwable cause = promise.cause();
//                    log.error("error", cause);
//                }
//            });

            // 我觉得这样写才对，先通过write来获取future，然后注册监听器，然后再flush
            ChannelFuture future = channel.write(new RpcRequestMessage(
                    1,
                    "cn.itcast.server.service.HelloService",
                    "sayHello",
                    String.class,
                    new Class[]{String.class},
                    new Object[]{"张三"}
            )).addListener(promise -> {
                // 排查异常的方式
                if (!promise.isSuccess()) {
                    Throwable cause = promise.cause();
                    log.error("error", cause);
                }
            });
            channel.flush();

            channel.closeFuture().sync();
        } catch (Exception e) {
            log.error("client error", e);
        } finally {
            group.shutdownGracefully();
        }
    }
}
