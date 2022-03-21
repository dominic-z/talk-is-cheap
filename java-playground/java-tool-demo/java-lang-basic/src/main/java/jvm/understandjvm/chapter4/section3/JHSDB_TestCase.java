package jvm.understandjvm.chapter4.section3;


/**jvm/understandjvm/chapter4/section3/JHSDB_TestCase$ObjectHolder
 -Xmx10m -XX:+UseSerialGC -XX:-UseCompressedOops -Dcom.sun.management.jmxremote

 通过sudo java -cp $JAVA_HOME/lib/sa-jdi.jar sun.jvm.hotspot.HSDB启动
 * @author dominiczhu
 * @version 1.0
 * @title JHSDB_TestCase
 * @date 2021/10/17 下午10:28
 */
public class JHSDB_TestCase {

    static class Test {
        static ObjectHolder staticObj = new ObjectHolder();
        ObjectHolder instanceObj = new ObjectHolder();

        void foo() {
            ObjectHolder localObj = new ObjectHolder();
            System.out.println("done"); // 这里设一个断点
        }
    }

    private static class ObjectHolder {
    }

    public static void main(String[] args) throws InterruptedException {
        Test test = new JHSDB_TestCase.Test();
        test.foo();
    }

}
