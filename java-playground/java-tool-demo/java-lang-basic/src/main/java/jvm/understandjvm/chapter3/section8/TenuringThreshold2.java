package jvm.understandjvm.chapter3.section8;

/**
 *
 -XX:+UseSerialGC -verbose:gc -Xms20M -Xmx20M -Xmn10M -XX:+PrintGCDetails -XX:SurvivorRatio=8
 -XX:MaxTenuringThreshold=15
 -XX:+PrintTenuringDistribution
 * @author dominiczhu
 * @version 1.0
 * @title TenuringThreshold2
 * @date 2021/10/16 下午5:09
 */
public class TenuringThreshold2 {
    private static final int _1MB = 1024 * 1024;
    public static void testTenuringThreshold2() {
        byte[] allocation1, allocation2, allocation3, allocation4;
        allocation1 = new byte[_1MB / 8]; // allocation1+allocation2大于survivo空间一半
//        allocation2 = new byte[_1MB / 4];
        allocation3 = new byte[4 * _1MB];
        allocation4 = new byte[4 * _1MB];
        allocation4 = null;
        allocation4 = new byte[4 * _1MB];
    }

    public static void main(String[] args) {
        testTenuringThreshold2();
    }

}
