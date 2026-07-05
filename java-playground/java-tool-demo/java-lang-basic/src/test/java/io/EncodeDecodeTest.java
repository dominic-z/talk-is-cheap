package io;

import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class EncodeDecodeTest {

    @Test
    public void decodeBytesWithCharsetDecoder() throws Exception {
        String original = "你好，CharsetDecoder，你好";
        byte[] bytes = original.getBytes(StandardCharsets.UTF_8);

        CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
        CharBuffer charBuffer = CharBuffer.allocate(original.length());

        CoderResult decodeResult = decoder.decode(ByteBuffer.wrap(Arrays.copyOfRange(bytes,0,bytes.length-1)), charBuffer, true);
        // 解析不全，结果会变成malFormat
//        CoderResult decodeResult = decoder.decode(ByteBuffer.wrap(Arrays.copyOfRange(bytes,0,bytes.length)), charBuffer, true);
        CoderResult flushResult = decoder.flush(charBuffer);

        assertTrue(decodeResult.isUnderflow());
        assertTrue(flushResult.isUnderflow());

        charBuffer.flip();
        assertEquals(original, charBuffer.toString());
    }

    @Test
    public void streamDecodeBytesSplitInsideOneCharacter() throws Exception {
        String original = "A你好B";
        byte[] bytes = original.getBytes(StandardCharsets.UTF_8);

        int splitIndex = 2;
        byte[] firstPart = Arrays.copyOfRange(bytes, 0, splitIndex);
        byte[] secondPart = Arrays.copyOfRange(bytes, splitIndex, bytes.length);

        CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
        ByteBuffer byteBuffer = ByteBuffer.allocate(bytes.length);
        CharBuffer charBuffer = CharBuffer.allocate(original.length());

        byteBuffer.put(firstPart);
        byteBuffer.flip();
        CoderResult firstDecodeResult = decoder.decode(byteBuffer, charBuffer, false);

        assertTrue(firstDecodeResult.isUnderflow()); // 说明字符不全，此时byteBuffer.hasRemaining()==true
        assertEquals("A", charBuffer.flip().toString());
        charBuffer.compact();// 切换为写模式，charBuffer.flip().toString()不会移动任何position

        byteBuffer.compact();
        byteBuffer.put(secondPart); // 必须将没读完的补充进byteBuffer中，然后重新解码
        byteBuffer.flip();
        CoderResult secondDecodeResult = decoder.decode(byteBuffer, charBuffer, true);
        CoderResult flushResult = decoder.flush(charBuffer);

        assertTrue(secondDecodeResult.isUnderflow());
        assertTrue(flushResult.isUnderflow());

        charBuffer.flip();
        assertEquals(original, charBuffer.toString());
    }


    /**
     * 对前 count 个分片进行流式解码，自动处理跨分片的截断字符
     *
     * @param chunks      拆分后的字节数组列表
     * @param count       要解码的分片数量
     * @param charset     字符集（如 StandardCharsets.UTF_8）
     * @return 解码后的字符串
     */
     static String decodeChunks(byte[][] chunks, int count, Charset charset) {
        CharsetDecoder decoder = charset.newDecoder();
        // 预分配 CharBuffer，按最大可能长度估算，避免频繁扩容
        // UTF-8 单字节对应最多1个char，这里用总字节数作为上界是安全的
        int totalBytes = 0;
        for (int i = 0; i < count && i < chunks.length; i++) {
            totalBytes += chunks[i].length;
        }
        CharBuffer output = CharBuffer.allocate(totalBytes);

        // 用于暂存上一个chunk尾部不完整的字节（UTF-8最多4字节一个字符）
        byte[] carryOver = new byte[0];

        for (int i = 0; i < count && i < chunks.length; i++) {
            byte[] chunk = chunks[i];

            // === 关键：将 carryOver + 当前chunk 拼成一个 ByteBuffer ===
            // 仅在 carryOver 非空时才产生一次小拷贝
            ByteBuffer input;
            if (carryOver.length > 0) {
                byte[] merged = new byte[carryOver.length + chunk.length];
                System.arraycopy(carryOver, 0, merged, 0, carryOver.length);
                System.arraycopy(chunk, 0, merged, carryOver.length, chunk.length);
                input = ByteBuffer.wrap(merged);
                carryOver = new byte[0]; // 重置
            } else {
                // 零拷贝：直接包装原始数组
                input = ByteBuffer.wrap(chunk);
            }

            // 流式解码，UNDERFLOW 表示输入已读完但可能有不完整字符
            CoderResult result = decoder.decode(input, output, false);

            // 如果还有剩余未消费的字节，说明是不完整字符的前缀
            if (input.hasRemaining()) {
                carryOver = Arrays.copyOfRange(
                        input.array(),
                        input.position(),
                        input.limit()
                );
            }
        }

        // 所有chunk处理完毕，flush 解码器内部状态
        // 注意：最后一个chunk之后调用 flush=true
        // 但如果确实存在不完整字节，flush 会替换为 replacement char
        decoder.decode(ByteBuffer.allocate(0), output, true);
        decoder.flush(output);

        return output.flip().toString();
    }


    // ==================== 测试验证 ====================
    @Test
    public void splitByteArrayDecoder() throws Exception {
        // 构造测试数据："你好世界Hello" 的 UTF-8 编码
        // "你"=E4BDA0, "好"=E5A5BD, "世"=E4B896, "界"=E7958C, "H"=48, "e"=65, "l"=6C, "l"=6C, "o"=6F
        String original = "你好世界Hello";
        byte[] fullBytes = original.getBytes("UTF-8");

        System.out.println("原始字符串: " + original);
        System.out.println("总字节数: " + fullBytes.length);
        printHex("完整字节", fullBytes);

        // 模拟拆分成5个chunk，故意在字符边界中间切割
        // E4BDA0 | E5A5 | BDE4B8 | 96E7958C48 | 656C6C6F
        byte[][] chunks = new byte[][]{
                Arrays.copyOfRange(fullBytes, 0, 3),   // "你" 完整
                Arrays.copyOfRange(fullBytes, 3, 5),   // "好" 被截断: E5A5
                Arrays.copyOfRange(fullBytes, 5, 8),   // BD + "世" 的前两字节 E4B8
                Arrays.copyOfRange(fullBytes, 8, 13),  // 96 + "界" + "H"
                Arrays.copyOfRange(fullBytes, 13, fullBytes.length), // "ello"
        };

        for (int i = 0; i < chunks.length; i++) {
            printHex("chunk[" + i + "]", chunks[i]);
        }

        // 只取前3个chunk解码
        String decoded = decodeChunks(chunks, 3, Charset.forName("UTF-8"));
        System.out.println("\n前3个chunk解码结果: [" + decoded + "]");
        System.out.println("期望结果: [你好] (第三个chunk包含'好'的剩余部分+'世'的不完整前缀)");

        // 取全部5个chunk解码作为对照
        String decodedAll = decodeChunks(chunks, 5, Charset.forName("UTF-8"));
        System.out.println("全部5个chunk解码结果: [" + decodedAll + "]");
        System.out.println("与原始字符串一致: " + original.equals(decodedAll));
    }

    private static void printHex(String label, byte[] bytes) {
        StringBuilder sb = new StringBuilder(label + ": ");
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b & 0xFF));
        }
        System.out.println(sb.toString().trim());
    }
}
