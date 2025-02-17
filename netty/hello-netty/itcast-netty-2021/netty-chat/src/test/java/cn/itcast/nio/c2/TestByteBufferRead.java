package cn.itcast.nio.c2;

import java.nio.ByteBuffer;

import static cn.itcast.nio.c2.ByteBufferUtil.debugAll;

public class TestByteBufferRead {

    public static void main(String[] args) {
//        ByteBuffer里有三个关键的参数，position用于指明当前要写入或者要读取的位置，limit用于指明最远能读到哪里，capacity用于指明当前buffer有多大
//        还有mark，见下文
        ByteBuffer buffer = ByteBuffer.allocate(10);
        buffer.put(new byte[]{'a', 'b', 'c', 'd'});
//        切换成读模式，position变为0，limit变为4
        buffer.flip();

        // rewind 从头开始读或者从头开始写，position设置为0
        final byte[] dst = new byte[4];
        buffer.get(dst);
        debugAll(buffer);
        buffer.rewind();
        System.out.println((char)buffer.get());

        // mark & reset
        // mark 做一个标记，记录 position 位置， reset 是将 position 重置到 mark 的位置
//        System.out.println((char) buffer.get());
//        System.out.println((char) buffer.get());
//        buffer.mark(); // 加标记，索引2 的位置
//        System.out.println((char) buffer.get());
//        System.out.println((char) buffer.get());
//        buffer.reset(); // 将 position 重置到索引 2
//        System.out.println((char) buffer.get());
//        System.out.println((char) buffer.get());

        // get(i) 不会改变读索引的位置
        System.out.println((char) buffer.get(3));
        debugAll(buffer);
    }
}
