package jvm.understandjvm.chapter4.section3;

import java.util.ArrayList;
import java.util.List;

/**
 * -Xms100m -Xmx100m -XX:+UseSerialGC
 * 然后直接jconsole，不要sudo
 * @author dominiczhu
 * @version 1.0
 * @title OOMObject
 * @date 2021/10/18 下午4:17
 */
public class JConsoleHeap {

    /**
     * 内存占位符对象，一个OOMObject大约占64KB
     */
    static class OOMObject {
        public byte[] placeholder = new byte[64 * 1024];
    }

    public static void fillHeap(int num) throws InterruptedException {
        List<OOMObject> list = new ArrayList<OOMObject>();
        for (int i = 0; i < num; i++) {
// 稍作延时，令监视曲线的变化更加明显
            Thread.sleep(100);
            list.add(new OOMObject());
        }
        System.gc();
    }

    public static void main(String[] args) throws Exception {
        fillHeap(1000);
    }

}
