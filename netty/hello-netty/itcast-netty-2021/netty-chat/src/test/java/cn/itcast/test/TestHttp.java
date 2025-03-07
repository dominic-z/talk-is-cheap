package cn.itcast.test;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.List;


/**
 * 对应2.3
 * <p>
 * 然后浏览器或者postman访问http://localhost:8080/hello?name=1
 */
@Slf4j
public class TestHttp {
    public static void main(String[] args) {
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.group(boss, worker);
            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                    ch.pipeline().addLast(new HttpServerCodec());
                    ch.pipeline().addLast(new SimpleChannelInboundHandler<DefaultHttpRequest>() {
                        @Override
                        protected void channelRead0(ChannelHandlerContext ctx, DefaultHttpRequest msg) throws Exception {

                            try {
                                log.debug("{}", msg.uri());
                                QueryStringDecoder decoder = new QueryStringDecoder(msg.uri());
                                List<String> name = decoder.parameters().get("name");
                                DefaultFullHttpResponse response = new DefaultFullHttpResponse(msg.protocolVersion(),
                                        HttpResponseStatus.OK);
                            /*
                            第一次访问测试的时候一直访问的是http://localhost:8080，然后浏览器一直拿不到结果，debug才发现在这一步报错，因为name是null
                            所以是因为这里的异常会被吞掉，不会暴露出来，所以这里应该有个try catch
                             */
                                byte[] bytes = ("<h1>hello!" + name.get(0) + "</h1>").getBytes();
                                response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html");
                                response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, bytes.length);
                                response.content().writeBytes(bytes);
//                            response.
                                ctx.writeAndFlush(response);
                            } catch (Exception e) {

                                log.error("error ", e);
                            }

                        }
                    });
                }
            });
            ChannelFuture channelFuture = serverBootstrap.bind(8080).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("server error", e);
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}
