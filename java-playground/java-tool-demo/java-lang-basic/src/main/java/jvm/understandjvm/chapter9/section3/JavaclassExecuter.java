package jvm.understandjvm.chapter9.section3;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

/**
 * @author dominiczhu
 * @version 1.0
 * @title JavaclassExecuter
 * @date 2021/10/23 上午11:59
 */
public class JavaclassExecuter {

    /**
     * 执行外部传过来的代表一个Java类的Byte数组<br>
     * 将输入类的byte数组中代表java.lang.System的CONSTANT_Utf8_info常量修改为劫持后的HackSystem类
     * 执行方法为该类的static main(String[] args)方法，输出结果为该类向System.out/err输出的信息
     *
     * @param classByte 代表一个Java类的Byte数组
     * @return 执行结果
     */
    public static String execute(byte[] classByte) {
        HackSystem.clearBuffer();
        ClassModifier cm = new ClassModifier(classByte);
        byte[] modiBytes = cm.modifyUTF8Constant("java/lang/System", "jvm/understandjvm/chapter9/section3/HackSystem");
        HotSwapClassLoader loader = new HotSwapClassLoader();
        Class<?> clazz = loader.loadByte(modiBytes);
        try {
            Method method = clazz.getMethod("main", new Class[]{String[].class});
            method.invoke(null, new String[]{null});
        } catch (Throwable e) {
            e.printStackTrace(HackSystem.out);
        }
        return HackSystem.getBufferString();
    }

    public static void main(String[] args) throws Exception {

        InputStream is = new FileInputStream("/Users/dominiczhu/work/learn/java/java-tool-demo/java-lang-basic/target" +
                "/classes/jvm/understandjvm/chapter9/section3/TestClass.class");
        byte[] b = new byte[is.available()];
        System.out.println("length" + is.available());
        is.read(b);
        is.close();


        final String hackOut = execute(b);

        System.out.println(hackOut);
    }
}
