package cn.itcast.netty.c4;

import io.netty.buffer.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static io.netty.buffer.ByteBufUtil.appendPrettyHexDump;
import static io.netty.util.internal.StringUtil.NEWLINE;


/**
 * 对应3.5 release以及创建
 */
@Slf4j
public class TestByteBuf {

    @Test
    public void writeByte() {

        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
        System.out.println(buf.getClass());
        System.out.println(buf.maxCapacity());
        log(buf);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 32; i++) {
            sb.append("a");
        }
        buf.writeBytes(sb.toString().getBytes());
        log(buf);
    }

    public static void log(ByteBuf buffer) {
        int length = buffer.readableBytes();
        int rows = length / 16 + (length % 15 == 0 ? 0 : 1) + 4;
        StringBuilder buf = new StringBuilder(rows * 80 * 2)
                .append("read index:").append(buffer.readerIndex())
                .append(" write index:").append(buffer.writerIndex())
                .append(" capacity:").append(buffer.capacity())
                .append(NEWLINE);
        appendPrettyHexDump(buf, buffer);
        System.out.println(buf.toString());
    }

    @Test
    public void testRelease(){
        ByteBuf buffer = ByteBufAllocator.DEFAULT.heapBuffer();

        log.info("buf class: {}",buffer.getClass());
        log.info("buffer ref count: {}",buffer.refCnt());
        buffer.writeBytes("hello release".getBytes());
        buffer.release();
        // 将会报错
        buffer.writeBytes("hello release".getBytes());


    }

    /**
     * 对应3.5 unpooled
     */
    @Test
    public void testUnpooled(){
        ByteBuf unpooledBuf = Unpooled.wrappedBuffer(new byte[]{1, 2, 3});
        System.out.println(unpooledBuf.getClass());
        System.out.println(ByteBufUtil.prettyHexDump(unpooledBuf));

        ByteBuf compositeByteBuf = Unpooled.wrappedBuffer(new byte[]{1, 2, 3}, new byte[]{4, 5, 6});
        System.out.println(compositeByteBuf.getClass());
        System.out.println(ByteBufUtil.prettyHexDump(compositeByteBuf));

        ByteBuf bufAlloc = UnpooledByteBufAllocator.DEFAULT.heapBuffer();
        bufAlloc.writeBytes("mouse dictionary".getBytes());
        log.info("bufAlloc class: {}",bufAlloc.getClass());
        StringBuilder sb = new StringBuilder();
        ByteBufUtil.appendPrettyHexDump(sb,bufAlloc);
        System.out.println(sb);
    }



}
