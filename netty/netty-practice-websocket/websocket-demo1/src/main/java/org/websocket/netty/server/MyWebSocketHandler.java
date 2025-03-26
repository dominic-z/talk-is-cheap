package org.websocket.netty.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * MyWebSocketHandler
 * WebSocket处理器，处理websocket连接相关
 *
 * @author zhengkai.blog.csdn.net
 * @date 2019-06-12
 */
@Slf4j
public class MyWebSocketHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("与客户端建立连接，通道开启！");

        //添加到channelGroup通道组
        MyChannelHandlerPool.channelGroup.add(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("与客户端断开连接，通道关闭！");
        //添加到channelGroup 通道组
        MyChannelHandlerPool.channelGroup.remove(ctx.channel());
    }

    /*
    正常继承SimpleChannelInboundHandler只需要重写channelRead0就行
    但是这个handler其实既要处理Http又要处理websocket的message，所以直接覆盖了channelRead
    那tm干啥不直接继承adapter
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        log.info("channel id: {}",ctx.channel().id());
        ObjectMapper objectMapper = new ObjectMapper();
        log.info("msg class: {},msg: {}", msg.getClass(), msg);
        if (msg instanceof FullHttpRequest request) {
            //首次连接是FullHttpRequest，处理参数 by zhengkai.blog.csdn.net
            // 但实际折断代码没锤子用，因为这个handler在WebSocketServerProtocolHandler之后，建立ws链接的http请求会被WebSocketServerProtocolHandler直接处理掉，
            // 只有将这个handler和WebSocketServerProtocolHandler调换位置才有效
            // 那其实应该有更优雅的办法，比如用channelid维护一个map
            String uri = request.uri();
            Map paramMap = getUrlParams(uri);
            log.info("接收到的参数是：{}", objectMapper.writeValueAsString(paramMap));
            //如果url包含参数，需要处理
            if (uri.contains("?")) {
                String newUri = uri.substring(0, uri.indexOf("?"));
                log.info(newUri);
                request.setUri(newUri);
            }

        } else if (msg instanceof TextWebSocketFrame frame) {
            //正常的TEXT消息类型
            log.info("收到客户端数据： {} ", frame.text());
            sendAllMessage(new TextWebSocketFrame(frame.text()));
        } else if (msg instanceof BinaryWebSocketFrame) {


            log.info("收到客户端二进制数据：", msg);
            sendAllMessage(((BinaryWebSocketFrame) msg).copy());
        }
        super.channelRead(ctx, msg);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, WebSocketFrame webSocketFrame) throws Exception {

    }

    private void sendAllMessage(Object message) {
        //收到信息后，群发给所有channel
        MyChannelHandlerPool.channelGroup.writeAndFlush(message);
    }

    private static Map getUrlParams(String url) {
        Map<String, String> map = new HashMap<>();
        url = url.replace("?", ";");
        if (!url.contains(";")) {
            return map;
        }
        if (url.split(";").length > 0) {
            String[] arr = url.split(";")[1].split("&");
            for (String s : arr) {
                String key = s.split("=")[0];
                String value = s.split("=")[1];
                map.put(key, value);
            }
            return map;

        } else {
            return map;
        }
    }
}