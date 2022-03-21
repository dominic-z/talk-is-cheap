package jvm.understandjvm.chapter10.section3;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dominiczhu
 * @version 1.0
 * @title GenericTypes
 * @date 2021/10/24 下午5:15
 */
public class GenericTypes {
//    public static String method(List<String> list) {
//        System.out.println("invoke method(List<String> list)");
//        return "";
//    }
//    public static int method(List<Integer> list) {
//        System.out.println("invoke method(List<Integer> list)");
//        return 1;
//    }
//    public static void main(String[] args) {
//        method(new ArrayList<String>());
//        method(new ArrayList<Integer>());
//    }

    public <T> void show(T t){
        System.out.println("abc");
    }
}
