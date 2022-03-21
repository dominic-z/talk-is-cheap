package jvm.understandjvm.chapter7.section3;

/**
 * @author dominiczhu
 * @version 1.0
 * @title ParentSubStaticField
 * @date 2021/10/20 下午2:04
 */
public class ParentSubStaticField {
    static class Parent {
        public static int A = 1;
        static {
            A = 2;
        }
    }
    static class Sub extends Parent {
        public static int B = A;
    }

    public static void main(String[] args) {
        System.out.println(Sub.B);
        System.out.println(Sub.A);
    }
}
