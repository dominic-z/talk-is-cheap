package cn.itcast.advance.c1;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class TestLengthFieldDecoder {
    public static void main(String[] args) {
        EmbeddedChannel channel = new EmbeddedChannel(
                // 代表每次分配的buf大小最大为1024，每个消息的消息长度从第0位写起，消息长度信息占用4个字节（以int作为长度为例）
                // 同时长度指的是从[自length信息起再偏移一个字节的位置起（本例中的4+1）]往后的消息长度。自此，整条消息的长度就有了。
                // 最后一个参数指的是构建的bytebuf里的内容是从哪里开始算起，举例而言，假设一条信息长度为10，即长度信息4+一个偏移位1，剩余的6就是消息本体
                // 但最终构建的bytebuf就是从第二位开始的后8个字节的信息，包含了一半的长度
                new LengthFieldBasedFrameDecoder(
                        1024, 0, 4, 1,2),
                new LoggingHandler(LogLevel.DEBUG)
        );

        //  4 个字节的内容长度， 实际内容
        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
        buffer.markReaderIndex();
        send(buffer, "Hello, world");
        send(buffer, "Hi!");
        channel.writeInbound(buffer);


        // 查看buffer内容
        buffer.resetReaderIndex();
        System.out.println(ByteBufUtil.prettyHexDump(buffer));
    }

    private static void send(ByteBuf buffer, String content) {
        byte[] bytes = content.getBytes(); // 实际内容
        int length = bytes.length; // 实际内容长度
        buffer.writeInt(length);
        buffer.writeByte(1);
        buffer.writeBytes(bytes);
    }
}

