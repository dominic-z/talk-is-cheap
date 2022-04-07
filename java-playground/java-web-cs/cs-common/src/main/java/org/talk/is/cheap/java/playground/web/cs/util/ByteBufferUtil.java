package org.talk.is.cheap.java.playground.web.cs.util;

import java.nio.ByteBuffer;

/**
 * @author dominiczhu
 * @version 1.0
 * @title ByteBufferUtil
 * @date 2022/4/7 3:07 下午
 */
public class ByteBufferUtil {

    private ByteBufferUtil() {
    }


    public static byte[] addAllByte(byte[] origin, ByteBuffer byteBuffer, int length) {

        byte[] target = new byte[origin.length + length];
        System.arraycopy(origin, 0, target, 0, origin.length);
        byteBuffer.get(target, origin.length, length);
        return target;
    }

}