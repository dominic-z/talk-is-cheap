package cn.itcast.advance.c1;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Server1 {
    void start() {
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.channel(NioServerSocketChannel.class);
            // 调整系统的接收缓冲器（滑动窗口），视频教程中用于模拟粘包，但是无法复现，弹幕中也多个无法复现，猜测与电脑性能有关
//            serverBootstrap.option(ChannelOption.SO_RCVBUF, 10);

            /**
             * 在Netty中，ChannelOption.RCVBUF_ALLOCATOR 是一个用于配置接收缓冲区分配器的参数。它的默认值是 AdaptiveRecvByteBufAllocator
             * .DEFAULT，这是一个自适应的接收缓冲区分配器，能够根据接收到的数据自动调整缓冲区的大小
             */
            // boss对应的是建立链接的eventLoopGroup，而child对应的就是处理通道数据的数据的eventLoopGroup
            // 可以通过配置child的ByteBuf的Allocator来模拟半包，用来给byteBuf分配空间并生成ByteBuf
            // 这个生成器生成的buf用来承载client提交的数据，FixedRecvByteBufAllocator生成器配置成了每次生成的ByteBuf固定长度为15
//            serverBootstrap.childOption(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(15));
            /**
             * 也可以继续使用AdaptiveRecvByteBufAllocator，但是要注意，构造函数的参数不能乱填，
             * 比如说下面例子，虽然入参(min/initial/max)对应的是2/4/16，即希望其生成的byteBuf最小为2，初始为4，最大为16
             * 但实际上这几个值只是参考值，AdaptiveRecvByteBufAllocator在创建的时候实际的(min/initial/max)会根据入参进行对齐
             * 而2/4/16对齐后，initialIndex算出来是-1，这会导致AdaptiveRecvByteBufAllocator.newHandle发生
             * 这会导致读取数据时在io.netty.channel.nio.AbstractNioByteChannel.NioByteUnsafe#read()方法中在recvBufAllocHandle()发生异常
             * 用于读的bytebuf无法被分配出来，所以无法执行数据读取，实测initial最小不能小于16。具体为啥这么设计，没研究。
             * 但这个异常并没有抛出来而是被吞掉了，所以就像看起来没有发生读取数据一样
             * （至于我怎么知道是这个方法异常的，是从下面channelread方法里debug step out上去的）
             */
//            serverBootstrap.childOption(ChannelOption.RCVBUF_ALLOCATOR,
//                    new AdaptiveRecvByteBufAllocator(2, 4, 16));
            serverBootstrap.group(boss, worker);
            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline()
                            .addLast(new ChannelInboundHandlerAdapter(){
                                // 通道建立时触发的一次
                                @Override
                                public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                    log.info("channel active: {}",ctx.channel());
                                    super.channelActive(ctx);
                                }

                                // 通道关闭时触发一次，例如客户端断开链接
                                @Override
                                public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                                    log.info("channel inactive: {}",ctx.channel());
                                    super.channelInactive(ctx);
                                }

                                @Override
                                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                    log.info("channel read");
                                    super.channelRead(ctx, msg);
                                }
                            })
                            // 用来打印内容，省着自己写log了
                            .addLast(new LoggingHandler(LogLevel.INFO));
                }
            });
            ChannelFuture channelFuture = serverBootstrap.bind(8080).sync();
            channelFuture.channel().closeFuture().sync(); // 代码会在这里卡住等待关闭的执行
        } catch (InterruptedException e) {
            log.error("server error", e);
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        new Server1().start();
    }
}