package jvm.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author dominiczhu
 * @version 1.0
 * @title GetMethodAccessor
 * @date 2021/8/17 下午8:11
 */
public class GetMethodAccessor {

    private void show1() {
        System.out.println("show");
    }

    private void show2() {
        System.out.println("show");
    }

    public static void main(String[] args) throws Exception {
        GetMethodAccessor mainObj = new GetMethodAccessor();

        Class<?> mainClass = Class.forName("jvm.reflection.GetMethodAccessor");

        Field methodAccessor = Method.class.getDeclaredField("methodAccessor");
        methodAccessor.setAccessible(true);


        System.out.println("============method 1=============");
        Method show1 = mainClass.getDeclaredMethod("show1");
        show1.setAccessible(true);
        System.out.println("first get");
        System.out.println(methodAccessor.get(show1));
        show1.invoke(mainObj);
        System.out.println("second get");
        System.out.println(methodAccessor.get(show1));

        System.out.println("another show1");
        Method anotherShow1 = mainClass.getDeclaredMethod("show1");
        anotherShow1.setAccessible(true);
        System.out.println(methodAccessor.get(anotherShow1));
        // 同一个method对象的accessor是相同的
        System.out.println(methodAccessor.get(anotherShow1) == methodAccessor.get(show1));


        System.out.println("============method 2=============");
        Method show2 = mainClass.getDeclaredMethod("show2");
        show2.setAccessible(true);
        System.out.println("first get");
        System.out.println(methodAccessor.get(show2));
        show2.invoke(mainObj);
        System.out.println("second get");
        System.out.println(methodAccessor.get(show2));

    }

}
