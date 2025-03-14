package cn.itcast.advance.c1;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
//import org.junit.Test;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 对应1.1~1.4的客户端
 */
public class Client1 {
    static final Logger log = LoggerFactory.getLogger(Client1.class);

    public static void main(String[] args) {
        for (int i = 0; i < 1; i++) {
            send();
        }
        System.out.println("finish");
    }



    private static void send() {
        NioEventLoopGroup worker = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.group(worker);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) {
                    ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                        // 会在连接 channel 建立成功后，会触发 active 事件
                        @Override
                        public void channelActive(ChannelHandlerContext ctx) {
                            // 发送10个16字节大小的数据，模拟粘包
                            for (int i = 0; i < 10; i++) {
                                ByteBuf buf = ctx.alloc().buffer();
                                buf.writeBytes(new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
                                        11, 12, 13, 14, 15});
                                ctx.writeAndFlush(buf);
                            }

                            // 发送一个160字节大小的数据，模拟半包
//                            ByteBuf buf = ctx.alloc().buffer();
//                            for (int i = 0; i < 10; i++) {
//                                buf.writeBytes(new byte[]{16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26,
//                                        27, 28, 29, 30, 31});
//                            }
//                            ctx.writeAndFlush(buf);
                            // 干完活，关闭通道
                            ctx.channel().close();
                        }
                    });
                }
            });
            ChannelFuture channelFuture = bootstrap.connect("127.0.0.1", 8080).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("client error", e);
        } finally {
            worker.shutdownGracefully();
        }
    }

    /**
     * 短连接的方式来进行发送，即发一次断一次，而接收区又足够大，因此避免了黏包
     */
    @Test
    public void shortConnection() {

        for (int i = 0; i < 10; i++) {
            NioEventLoopGroup worker = new NioEventLoopGroup();
            try {
                Bootstrap bootstrap = new Bootstrap();
                bootstrap.channel(NioSocketChannel.class);
                bootstrap.group(worker);
                bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                            // 会在连接 channel 建立成功后，会触发 active 事件
                            @Override
                            public void channelActive(ChannelHandlerContext ctx) {
                                ByteBuf buf = ctx.alloc().buffer(16);
                                buf.writeBytes(new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
                                        11, 12, 13, 14, 15,16});
                                ctx.writeAndFlush(buf);
                                ctx.channel().close();
                            }
                        });
                    }
                });
                ChannelFuture channelFuture = bootstrap.connect("127.0.0.1", 8080).sync();
                channelFuture.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                log.error("client error", e);
            } finally {
                worker.shutdownGracefully();
            }
        }

    }
}