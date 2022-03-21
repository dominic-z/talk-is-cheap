package com.zpc.myrabbit.listener;

/**
 * 消费者
 *
 * @author Evan
 */
public class Listener {

    //具体执行业务的方法
    public void listen(String msg) {
        System.out.println("\n消费者C开始处理消息： " + msg + "\n");
    }
}