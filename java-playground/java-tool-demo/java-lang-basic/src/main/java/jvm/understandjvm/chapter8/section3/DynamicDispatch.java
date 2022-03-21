package jvm.understandjvm.chapter8.section3;

/**
 * @author dominiczhu
 * @version 1.0
 * @title DynamicDispatch
 * @date 2021/10/21 下午1:27
 */
public class DynamicDispatch {

    static abstract class Human {
        protected abstract void sayHello();
    }
    static class Man extends Human {
        @Override
        protected void sayHello() {
            System.out.println("man say hello");
        }
    }
    static class Woman extends Human {
        @Override
        protected void sayHello() {
            System.out.println("woman say hello");
        }
    }
    public static void main(String[] args) {
        Human man = new Man();
        Human woman = new Woman();
        man.sayHello();
        woman.sayHello();
        man = new Woman();
        man.sayHello();
    }

}
