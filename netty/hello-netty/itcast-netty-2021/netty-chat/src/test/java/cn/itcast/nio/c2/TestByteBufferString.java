package cn.itcast.nio.c2;


import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static cn.itcast.nio.c2.ByteBufferUtil.debugAll;

public class TestByteBufferString {
    public static void main(String[] args) {
        // 1. 字符串转为 ByteBuffer
        ByteBuffer buffer1 = ByteBuffer.allocate(16);
        // 如果剩余的空间不够用的话就会报错
        buffer1.put("hello".getBytes());
        debugAll(buffer1);

        // 2. Charset
        ByteBuffer buffer2 = StandardCharsets.UTF_8.encode("hello");
        debugAll(buffer2);

        // 3. wrap
        ByteBuffer buffer3 = ByteBuffer.wrap("hello".getBytes());
        debugAll(buffer3);

        // 4. 转为字符串
        System.out.println("buffer2 position before read: " + buffer2.position());
        String str1 = StandardCharsets.UTF_8.decode(buffer2).toString();
        System.out.println(str1);
        System.out.println("buffer2 position: " + buffer2.position());

        buffer1.flip();
        String str2 = StandardCharsets.UTF_8.decode(buffer1).toString();
        System.out.println(str2);

    }
}
