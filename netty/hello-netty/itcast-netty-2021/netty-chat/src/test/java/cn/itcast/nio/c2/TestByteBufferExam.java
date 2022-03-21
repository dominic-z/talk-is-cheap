package cn.itcast.nio.c2;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static cn.itcast.nio.c2.ByteBufferUtil.debugAll;

public class TestByteBufferExam {
    public static void main(String[] args) {
         /*
         网络上有多条数据发送给服务端，数据之间使用 \n 进行分隔
         但由于某种原因这些数据在接收时，被进行了重新组合，例如原始数据有3条为
             Hello,world\n
             I'm zhangsan\n
             How are you?\n
         变成了下面的两个 byteBuffer (黏包，半包)
             Hello,world\nI'm zhangsan\nHo
             w are you?\n
         现在要求你编写程序，将错乱的数据恢复成原始的按 \n 分隔的数据
         */
        ByteBuffer source = ByteBuffer.allocate(32);
        source.put("Hello,world\nI'm zhangsan\nHo".getBytes());
        split(source);
        debugAll(source);
        source.put("w are you?\n".getBytes());
        split(source);
        debugAll(source);


//        source.put("Hello,world\nI'm zhangsan\nHo".getBytes());
//        getLines(source);
//        debugAll(source);
//        source.put("w are you?\n".getBytes());
//        getLines(source);
//        debugAll(source);
//        source.put("how are you?".getBytes());
//        getLines(source);
//        debugAll(source);

    }

    private static void split(ByteBuffer source) {
        source.flip();
        for (int i = 0; i < source.limit(); i++) {
            // 找到一条完整消息
            if (source.get(i) == '\n') {
                int length = i + 1 - source.position();
                // 把这条完整消息存入新的 ByteBuffer
                ByteBuffer target = ByteBuffer.allocate(length);
                // 从 source 读，向 target 写
                for (int j = 0; j < length; j++) {
                    target.put(source.get());
                }

                target.flip();
                System.out.println(StandardCharsets.UTF_8.decode(target));
//                debugAll(target);
            }
        }
        source.compact();
    }


    // 思路同上
    private static List<String> getLines(ByteBuffer source) {
        source.flip();
        // 先mark
        source.mark();
        List<String> lines = new ArrayList<>();
        StringBuilder sb = new StringBuilder();

        while (source.hasRemaining()) {

            final char c = (char)source.get();
            if (c == '\n') {
                lines.add(sb.toString());
                sb.delete(0, sb.length());
                // 此时position刚好指向\n的下一位
                source.mark();
            } else {
                sb.append(c);
            }
        }
        source.reset();
        source.compact();

        System.out.println(lines);
        return lines;

    }
}
