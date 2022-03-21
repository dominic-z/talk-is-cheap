package demo;

import org.junit.Test;

import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * @author dominiczhu
 * @version 1.0
 * @title StringDemo
 * @date 2021/8/5 下午6:22
 */
public class StringDemo {

    @Test
    public void testCharset() {
        String s = "你好";
        System.out.println("系统默认编码：" + System.getProperty("file.encoding"));
        System.out.println(Arrays.toString(s.getBytes(Charset.forName("unicode"))));
        System.out.println(Arrays.toString(s.getBytes(Charset.forName("utf8"))));
        System.out.println(Arrays.toString(s.getBytes(Charset.forName("utf16"))));
        System.out.println(Arrays.toString(s.getBytes()));

        showString(s, "gbk");
        showString(s, "utf8");
        showString(s, "utf16");
        showString(s, "unicode");
        showString(s, "ascii");
        showString(s, "iso-8859-1");
    }

    private void showString(String s, String charSet) {
        System.out.println("==========" + charSet + "========");
        byte[] bytes = s.getBytes();
        String ss = new String(bytes, 0, bytes.length, Charset.forName(charSet));

        System.out.println(ss);
        System.out.println(Arrays.toString(ss.toCharArray()));
        System.out.println(Arrays.toString(ss.getBytes()));
    }

    @Test
    public void convertMessyCode(){

        String s = "你好";
        byte[] utf8Bytes = s.getBytes();
        System.out.println(Arrays.toString(utf8Bytes));


        System.out.println("messy");
        String messyS = new String(utf8Bytes, Charset.forName("iso-8859-1")); // 将utf8错误地按照iso去解码，于是解码出来了一坨乱码。
        byte[] messyBytes = messyS.getBytes(); // 再使用utf8去编码，将这个字符串转成字节数组
        System.out.println(messyS);
        System.out.println(Arrays.toString(messyBytes));


        System.out.println("convert");
        byte[] convertBytes = messyS.getBytes(Charset.forName("iso-8859-1")); // 将这个messyString按照iso编码
        String convertString = new String(convertBytes,Charset.forName("utf8")); // 再将字节数组反过来按照utf8解码再转回utf8
        System.out.println(convertString);
        System.out.println(Arrays.toString(convertBytes));

        // s--> utf8编码--> 使用iso解码（出现乱字符串） --> 再使用utf8编码（编码出不知道什么玩意的）
        // s--> utf8编码--> 使用iso解码（出现乱字符串） --> 使用iso编码（相当于使用iso解码的逆操作，为的是获取iso解码前的utf8编码） --> 使用utf8解码（获取原始s）

        // 无论s还是messyString，概念上来说，他们都是同一个东西（一块数据d），这个数据有两种表现形式，字符串和字节数组；
        // 所谓编码，就是将这个字符串 按照某种方式 转换成字节数组
        // 所谓解码，就是将这个字节数组 按照某种方式 转换成字符串
        // 这个某种方式，指的就是字符集；（可以将这个过程理解为查字典，不同的字符集，就是查不同的字典）
        // 上面的例子里，出现乱码的过程如下，可以看到，解码的方式出错了
        // s --> 按照utf8的方式进行编码 --> 字节数组utf8Bytes --> 按照iso的方式进行解码 --> 字符串messyS
        // 恢复的过程如下 可以看到，就是上面过程的逆过程
        // 字符串messyS --> 按照iso的方式解码 --> 字节数组convertBytes --> 按照utf8的方式进行解码 --> convertString

    }

}
