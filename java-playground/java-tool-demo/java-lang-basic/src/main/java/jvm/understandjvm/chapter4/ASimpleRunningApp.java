package jvm.understandjvm.chapter4;

/**
 * -XX:+UseSerialGC -verbose:gc -Xms20M -Xmx20M -Xmn10M -XX:+PrintGCDetails -XX:SurvivorRatio=8
 * @author dominiczhu
 * @version 1.0
 * @title ASimpleRunningApp
 * @date 2021/10/17 下午9:44
 */
public class ASimpleRunningApp {
    private static final int _1MB = 1024 * 1024;

    private byte[] allocation1;


    public static void main(String[] args) throws InterruptedException {

        ASimpleRunningApp aSimpleRunningApp = new ASimpleRunningApp();
        while(true){
            aSimpleRunningApp.allocation1 = new byte[_1MB*5];
            System.out.println("holding");
            Thread.sleep(10000);
        }
    }
}
