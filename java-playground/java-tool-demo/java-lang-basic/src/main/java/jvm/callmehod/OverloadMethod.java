package jvm.callmehod;

/**
 * @author dominiczhu
 * @version 1.0
 * @title OverloadMethod
 * @date 2021/8/13 下午3:10
 */
public class OverloadMethod {

    static void invoke(Object obj, Object... args) {
        System.out.println("1");

    }

    static void invoke(String s, Object obj, Object... args) {
        System.out.println("2");
    }


    public static void main(String[] args) {

        invoke(null, 1);    // 调用第二个invoke方法
        invoke(null, 1, 2); // 调用第二个invoke方法
        invoke(null, new Object[]{1}); // 只有手动绕开可变长参数的语法糖，
        // 才能调用第一个invoke方法
    }

}
