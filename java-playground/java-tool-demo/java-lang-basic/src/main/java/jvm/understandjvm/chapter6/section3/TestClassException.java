package jvm.understandjvm.chapter6.section3;

/**
 * @author dominiczhu
 * @version 1.0
 * @title TestClassException
 * @date 2021/10/19 下午8:40
 */
public class TestClassException {
    public int inc() throws IllegalArgumentException{
        int x;
        try {
            x = 1;
            return x;
        } catch (Exception e) {
            x = 2;
            return x;
        } finally {
            x = 3;
        }
    }

}
