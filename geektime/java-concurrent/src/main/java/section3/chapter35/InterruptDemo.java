package section3.chapter35;

/**
 * @author dominiczhu
 * @version 1.0
 * @title InterruptDemo
 * @date 2021/8/2 上午9:56
 */
public class InterruptDemo {

    public static void main(String[] args) throws InterruptedException {

        Proxy proxy = new Proxy();

        proxy.start();

        Thread.sleep(1500);

        proxy.stop();





    }
}



class Proxy {
    //线程终止标志位
    volatile boolean terminated = false;
    boolean started = false;
    //采集线程
    Thread rptThread;
    //启动采集功能
    synchronized void start(){
        //不允许同时启动多个采集线程
        if (started) {
            return;
        }
        started = true;
        terminated = false;
        rptThread = new Thread(()->{
            while (!terminated){
                //省略采集、回传实现
                System.out.println("abc");

                //每隔两秒钟采集、回传一次数据
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e){
                    //重新设置线程中断状态
                    System.out.println("interrupt sleep");
                    Thread.currentThread().interrupt();
                }
            }
            System.out.println("interrupt sleep out");

            //执行到此处说明线程马上终止
            started = false;
        });
        rptThread.start();
    }
    //终止采集功能
    synchronized void stop() throws InterruptedException {
        //设置中断标志位
        terminated = true;
        //中断线程rptThread
        rptThread.interrupt();

        Thread.sleep(1000);

    }
}
