package handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;


/**
 *
 * LoggingHandler是个双向的handler，即是ChannelInboundHandlerAdapter，又是ChannelOutboundHandler=
 * 在输入和输出时都会被触发
 */
@Slf4j
public class Debugger extends LoggingHandler {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

//        log.info("read msg: {}", msg);
        super.channelRead(ctx, msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
//        log.info("write msg: {}", msg);

        super.write(ctx, msg, promise);
    }
}
