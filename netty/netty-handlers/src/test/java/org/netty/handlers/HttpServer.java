package org.netty.handlers;

import com.google.gson.Gson;
import handlers.Debugger;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedFile;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
public class HttpServer {

    @Test
    public void server() throws InterruptedException {

        // 创建主从线程组
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            // 创建服务器启动引导类
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            // 添加 HttpServerCodec 用于 HTTP 请求和响应的编解码
                            ch.pipeline().addLast(new Debugger()); //   1
                            ch.pipeline().addLast(new HttpServerCodec());
                            ch.pipeline().addLast(new Debugger()); //   2
                            // 添加 HttpObjectAggregator 用于聚合 HTTP 请求的各个部分
                            /* HttpObjectAggregator的备注已经说的很明确了
                            A ChannelHandler that aggregates an HttpMessage and its following HttpContents into a
                            single FullHttpRequest or FullHttpResponse (depending on if it used to handle requests or
                             responses) with no following HttpContents. It is useful when you don't want to take care
                              of HTTP messages whose transfer encoding is 'chunked'. Insert this handler after
                              HttpResponseDecoder in the ChannelPipeline if being used to handle responses, or after
                              HttpRequestDecoder and HttpResponseEncoder in the ChannelPipeline if being used to
                              handle requests.
                              结合一个例子来理解，以post请求为例，位置1的debugger打印出了完整的请求内容，包括请求方法、httpversion、header和body
                              而在位置2的debugger可以看到，debugger打印了两次，说明httpServerCodec会将一个请求的bytebuf拆分为两个不同的message对象，

                              第一个对象是DefaultHttpRequest，这个对象只有post请求的方法、http版本和header部分
                              然后第二个对象是DefaultLastHttpContent，这个对象包含了一个请求的body部分，
                              于是，HttpObjectAggregator的作用就是将这俩东西合并成一个DefaultFullHttpRequest
                             */
                            ch.pipeline().addLast(new HttpObjectAggregator(65536));

                            ch.pipeline().addLast(new ChunkedWriteHandler());
                            // 添加自定义处理器来处理 HTTP 请求
                            ch.pipeline().addLast(new NettyHttpServerHandler());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            // 绑定端口并开始接收连接
            ChannelFuture f = b.bind(8080).sync();

            // 等待服务器关闭
            f.channel().closeFuture().sync();
        } finally {
            // 优雅地关闭线程组
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    class NettyHttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
            // 创建一个简单的 HTTP 响应

//            FullHttpResponse response = new DefaultFullHttpResponse(
//                    HttpVersion.HTTP_1_1,
//                    HttpResponseStatus.OK,
//                    Unpooled.copiedBuffer("Hello, Netty HTTP Server!", CharsetUtil.UTF_8)
//            );
//
//            // 设置响应头
//            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
//            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
//            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);

            // 发送响应，然后关闭这个通道
//            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);


            // 也可以这样写，然后交给HttpObjectAggregator来聚合
//            DefaultHttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1,
//                    HttpResponseStatus.OK);
//            DefaultLastHttpContent httpContent = new DefaultLastHttpContent(Unpooled.copiedBuffer("Hello, Netty HTTP " +
//                    "Server!", CharsetUtil.UTF_8));
//            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
//            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, httpContent.content().readableBytes());
//            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
//            ctx.writeAndFlush(response);
//            ctx.writeAndFlush(httpContent).addListener(ChannelFutureListener.CLOSE);


//            用来测试ChunkedWriteHandler
            File file = new File("pom.xml");
            // 可以看debugger，把chunckedfile文件拆分成每个最大100b大小的文件发回去的。
            ChunkedFile chunkedFile = new ChunkedFile(file,100);
            // 设置响应头信息
            HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
            HttpHeaders headers = response.headers();
            headers.set(HttpHeaderNames.CONTENT_TYPE, "application/octet-stream");
            headers.set(HttpHeaderNames.CONTENT_LENGTH, file.length());
            // 写入响应
            ctx.write(response);
            // 写入文件内容
            ctx.writeAndFlush(chunkedFile).addListener(ChannelFutureListener.CLOSE);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            ctx.close();
        }

    }


}
