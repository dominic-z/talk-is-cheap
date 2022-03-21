package jvm.understandjvm.chapter10.section3;

/**
 * @author dominiczhu
 * @version 1.0
 * @title AutoBoxing
 * @date 2021/10/24 下午5:26
 */
public class AutoBoxing {
    public static void main(String[] args) {
        Integer a = 1;
        Integer b = 2;
        Integer c = 3;
        Integer d = 3;
        Integer e = 321;
        Integer f = 321;
        Long g = 3L;
        System.out.println(c == d); // t
        System.out.println(e == f); // f
        System.out.println(c == (a + b)); // t
        System.out.println(c.equals(a + b)); // t
        System.out.println(g == (a + b)); // t
        System.out.println(g.equals(a + b)); // t
    }
}
