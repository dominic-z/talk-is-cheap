package jvm.understandjvm.chapter4.section3;


/**
 -Xmx10m -XX:+UseSerialGC -XX:-UseCompressedOops -Dcom.sun.management.jmxremote

 通过jvisualvm启动
 * @author dominiczhu
 * @version 1.0
 * @title JHSDB_TestCase
 * @date 2021/10/17 下午10:28
 */
public class VMVisual_TestCase {

    static class Test {
        static ObjectHolder staticObj = new ObjectHolder();
        static ObjectHolder staticObj2 = staticObj;
        ObjectHolder instanceObj = new ObjectHolder();

        void foo() throws InterruptedException {
            ObjectHolder localObj = new ObjectHolder();
            System.out.println("done"); // 这里设一个断点
            Thread.sleep(600000);

        }
    }

    private static class ObjectHolder {
    }

    public static void main(String[] args) throws InterruptedException {
        Test test = new VMVisual_TestCase.Test();
        test.foo();
    }

}
