package jvm.understandjvm.chapter3.section8;

/**
 VM参数：
 -XX:+UseSerialGC -XX:PretenureSizeThreshold=3145728 -verbose:gc -Xms20M -Xmx20M -Xmn10M  -XX:+PrintGCDetails
 -XX:SurvivorRatio=8
 *
 */

/**
 * @author dominiczhu
 * @version 1.0
 * @title PretenureSizeThreshol
 * @date 2021/10/16 下午4:33
 */
public class PretenureSizeThreshol {
    private static final int _1MB = 1024 * 1024;


    public static void testPretenureSizeThreshold() {
        byte[] allocation;
        allocation = new byte[4 * _1MB]; //直接分配在老年代中
    }

    public static void main(String[] args) {
        testPretenureSizeThreshold();
    }
}
