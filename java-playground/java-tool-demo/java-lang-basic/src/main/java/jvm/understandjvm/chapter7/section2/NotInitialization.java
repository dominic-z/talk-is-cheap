package jvm.understandjvm.chapter7.section2;

/**
 * @author dominiczhu
 * @version 1.0
 * @title NotInitialization
 * @date 2021/10/19 下午11:27
 */
public class NotInitialization {
    public static void main(String[] args) {
        System.out.println(SubClass.value);
        System.out.println(new SubClass().value1);
    }
}


/**
 * 被动使用类字段演示一：
 * 通过子类引用父类的静态字段，不会导致子类初始化
 **/
class SuperClass {
    static {
        System.out.println("SuperClass init!");
    }
    public static int value = 123;
    public int value1;
}
class SubClass extends SuperClass {
    static {
        System.out.println("SubClass init!");
    }
}