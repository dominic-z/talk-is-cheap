package jvm.understandjvm.chapter8.section3;

import java.util.Random;

/**
 * @author dominiczhu
 * @version 1.0
 * @title StaticDispatch
 * @date 2021/10/21 下午1:11
 */
public class StaticDispatch {

    static abstract class Human {
    }
    static class Man extends Human {
    }
    static class Woman extends Human {
    }
    public void sayHello(Human guy) {
        System.out.println("hello,guy!");
    }
    public void sayHello(Man guy) {
        System.out.println("hello,gentleman!");
    }
    public void sayHello(Woman guy) {
        System.out.println("hello,lady!");
    }
    public static void main(String[] args) {
        Human man = new Man();
        Human woman = new Woman();
        StaticDispatch sr = new StaticDispatch();
        sr.sayHello(man);
        sr.sayHello(woman);

        // 实际类型变化
//        Human human = (new Random()).nextBoolean() ? new Man() : new Woman();
//// 静态类型变化
//        sr.sayHello((Man) human);
//        sr.sayHello((Woman) human);
    }

}
