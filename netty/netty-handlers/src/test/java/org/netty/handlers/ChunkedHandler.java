package org.netty.handlers;

import handlers.Debugger;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.stream.ChunkedFile;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.InetSocketAddress;


@Slf4j
public class ChunkedHandler {


    @Test
    public void server(){
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            // 在write方法中打断点debug
                            pipeline.addLast(new Debugger());
                            // 添加 ChunkedWriteHandler 用于处理分块数据
                            // 如果要向channel写入大数据块，需要使用ChunkedWriterHandler+ChunkedFile
                            // 本质上相当于将一个大数据块分多次写入，而不是一次写入大量数据
                            // 相当于时间换空间，可以看一下ChunkedWriteHandler的doFlush方法的实现。
                            // 或者说，ChunkedFile负责将大数据块分片，ChunkedWriterHandler是ChunkedFile的coder，负责将Chunkedfile分次写入
                            pipeline.addLast(new ChunkedWriteHandler());
                            pipeline.addLast(new ChannelInboundHandlerAdapter(){
                                @Override
                                public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                    File file = new File("pom.xml");
                                    // 可以看debugger，实际是把chunckedfile文件拆分成每个最大100b大小的文件分多次写入channel里。
                                    ChunkedFile chunkedFile = new ChunkedFile(file,100);
                                    ctx.write(chunkedFile);
                                    ctx.flush();
                                    log.info("write chunkedFile done");
                                    // 看源码应该是触发其他handler的active，如果注释掉，下一个handler的channelActive不会再触发
//                                    super.channelActive(ctx);
                                }
                            });

                            pipeline.addLast(new ChannelInboundHandlerAdapter(){
                                @Override
                                public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                    log.info("triggered");
                                    super.channelActive(ctx);
                                }
                            });
                        }
                    });
            ChannelFuture f = b.bind(3000).sync();
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    @Test
    public void client(){
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(new NioEventLoopGroup())

                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                            // 电脑性能正常或者比较好的时候，会发现client是一次性接收了全部数据，无法看出是将chunkedfile分块了的
                            // 但可以在server端的debug里的write方法打断点，强制让server端以很慢的速度一次写100b
                            // 这样看客户端的接收，每次就是100b大小的文件。
                            ch.pipeline().addLast(new Debugger());
                        }
                    });
            ChannelFuture channelFuture = bootstrap.connect(new InetSocketAddress("localhost", 3000)).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
