package jdk.jstat;

import jvm.domain.Apple;
import org.junit.Test;

import java.util.LinkedList;
import java.util.Random;

public class GCDemo {


    /**
     * 使用jps查看java进程
     * 使用jstat -gc java进程id -1000  查看gc情况
     *
     */
    @Test
    public void testGC() throws InterruptedException {
        LinkedList<byte[]> list = new LinkedList<>();
        Random random = new Random();
        for(int i=0;i<1000;i++){
            // 每次创建一个 1MB 的字节数组
            byte[] largeObject = new byte[1024 * 1024];
            if(i%2==0){
                list.add(largeObject);
            }
        }
        Thread.sleep(200000);
    }
}
