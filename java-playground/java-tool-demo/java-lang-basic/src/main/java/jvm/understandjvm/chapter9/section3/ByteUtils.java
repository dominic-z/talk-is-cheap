package jvm.understandjvm.chapter9.section3;

/**
 * @author dominiczhu
 * @version 1.0
 * @title ByteUtils
 * @date 2021/10/23 上午11:30
 */
public class ByteUtils {

    /**
     * 将btye数组转化为int
     * 例如btye数组是[1,2,3]，那么转化为int的方式就是，每个byte都是一个8位的二进制码，那么123连在一起组成了24位二进制码，这个二进制码的int就是输出结果
     * 另外 & 0xff的作用是负数转正数
     * @param b
     * @param start
     * @param len
     * @return
     */
    public static int bytes2Int(byte[] b, int start, int len) {
        int sum = 0;
        int end = start + len;
        for (int i = start; i < end; i++) {
            int n = ((int) b[i]) & 0xff;
            n <<= (--len) * 8;
            sum = n + sum;
        }
        return sum;
    }

    public static byte[] int2Bytes(int value, int len) {
        byte[] b = new byte[len];
        for (int i = 0; i < len; i++) {
            b[len - i - 1] = (byte) ((value >> 8 * i) & 0xff);
        }
        return b;
    }

    public static String bytes2String(byte[] b, int start, int len) {
        return new String(b, start, len);
    }

    public static byte[] string2Bytes(String str) {
        return str.getBytes();
    }

    public static byte[] bytesReplace(byte[] originalBytes, int offset, int len, byte[] replaceBytes) {
        byte[] newBytes = new byte[originalBytes.length + (replaceBytes.length - len)];
        System.arraycopy(originalBytes, 0, newBytes, 0, offset);
        System.arraycopy(replaceBytes, 0, newBytes, offset, replaceBytes.length);
        System.arraycopy(originalBytes, offset + len, newBytes, offset + replaceBytes.length,
                originalBytes.length-offset-len);
        return newBytes;
    }

    public static void main(String[] args) {
        final int i = bytes2Int(new byte[]{-128}, 0, 1);
        System.out.println(i);
    }
}
