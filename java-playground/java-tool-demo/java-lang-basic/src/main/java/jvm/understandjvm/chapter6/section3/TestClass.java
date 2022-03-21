package jvm.understandjvm.chapter6.section3;

/**
 * javap -verbose TestClass
 *
 * @author dominiczhu
 * @version 1.0
 * @title TestClass
 * @date 2021/10/19 下午1:15
 */
public class TestClass {

    private class Inner {
    }

    public <T> T generic(T t) {
        return t;
    }

    private static final int i = 3;
    private int m;

    public int inc() {
        return m + 1;
    }

}
