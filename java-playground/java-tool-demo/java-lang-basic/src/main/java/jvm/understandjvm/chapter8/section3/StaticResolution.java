package jvm.understandjvm.chapter8.section3;

/**
 * @author dominiczhu
 * @version 1.0
 * @title StaticResolution
 * @date 2021/10/21 上午9:54
 */
public class StaticResolution {
    public static void sayHello(int i) {
        System.out.println("hello world");
    }
    public static void main(String[] args) {
        StaticResolution.sayHello(1);
    }
}