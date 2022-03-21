package com.zpc.rabbitmq.spring;

/**
 * 消费者
 *
 * @author Evan
 */
public class Foo {

    //具体执行业务的方法
    public void listen(String foo) {
        System.out.println("\n消费者： " + foo + "\n");
    }
}