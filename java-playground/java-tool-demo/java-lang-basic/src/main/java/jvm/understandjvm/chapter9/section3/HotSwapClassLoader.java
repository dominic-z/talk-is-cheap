package jvm.understandjvm.chapter9.section3;

/**
 * @author dominiczhu
 * @version 1.0
 * @title HotSwapClassLoader
 * @date 2021/10/23 上午11:29
 */
public class HotSwapClassLoader extends ClassLoader {
    public HotSwapClassLoader() {
        super(HotSwapClassLoader.class.getClassLoader());
    }

    public Class loadByte(byte[] classByte) {
        return defineClass(null, classByte, 0, classByte.length);
    }
}
