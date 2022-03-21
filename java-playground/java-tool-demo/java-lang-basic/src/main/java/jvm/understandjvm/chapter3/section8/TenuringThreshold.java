package jvm.understandjvm.chapter3.section8;

/**
 * VM参数：
-verbose:gc -Xms20M -Xmx20M -Xmn10M -XX:+PrintGCDetails -XX:Survivor-Ratio=8 -XX:MaxTenuringThreshold=1
-XX:+PrintTenuringDistribution


 -XX:+UseSerialGC -verbose:gc -XX:+PrintGC -Xms20M -Xmx20M -Xmn10M -XX:+PrintGCDetails -XX:SurvivorRatio=8
 -XX:MaxTenuringThreshold=1
 -XX:+PrintTenuringDistribution
 */

import java.util.Arrays;

/**
 * @author dominiczhu
 * @version 1.0
 * @title TenuringThreshold
 * @date 2021/10/16 下午4:28
 */
public class TenuringThreshold {
    private static final int _1MB = 1024 * 1024;

    public static void testTenuringThreshold() {
        byte[] allocation1, allocation2, allocation3;
        allocation1 = new byte[_1MB / 10]; // 什么时候进入老年代决定于XX:MaxTenuring-Threshold设置
        allocation2 = new byte[4 * _1MB];
        allocation3 = new byte[4 * _1MB];
        allocation3 = null;
        allocation3 = new byte[4 * _1MB];

//        System.out.println(Arrays.toString(allocation1));
//        System.out.println(Arrays.toString(allocation2));
//        System.out.println(Arrays.toString(allocation3));
    }

    public static void main(String[] args) {
        testTenuringThreshold();
    }

}
