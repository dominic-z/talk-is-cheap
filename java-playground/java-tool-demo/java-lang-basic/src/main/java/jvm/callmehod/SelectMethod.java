package jvm.callmehod;

import jvm.domain.Apple;
import jvm.domain.Fruit;

/**
 * @author dominiczhu
 * @version 1.0
 * @title SelectMethod
 * @date 2021/8/13 下午3:19
 */
public class SelectMethod {

    void show(Integer integer, Apple apple) { // 1
        System.out.println("Integer integer, Apple apple");
    }

    void show(Integer integer, Fruit fruit) { //2
        System.out.println("Integer integer, Fruit fruit");
    }

    void show(int intValue, Fruit fruit) { //3
        System.out.println("int intValue, Fruit fruit");
    }

    void show(float floatValue, Fruit apple) { //4
        System.out.println("float floatValue, Fruit apple");
    }

    void show(float floatValue, Fruit fruit, int... values) { //5
        System.out.println("float floatValue, Fruit fruit, int... values");
    }

    void show(int intValue, Apple apple, int... values) {  //6
        System.out.println("int intValue, apple apple, int... values");
    }

    void show(Integer integer, Fruit fruit, int... values) { //7
        System.out.println("Integer integer, Fruit apple, int... values");
    }

    void show(int intValue, Fruit fruit, int... values) { //8
        System.out.println("int intValue, Fruit fruit, int... values");
    }


    public static void main(String[] args) {
        SelectMethod selectMethod = new SelectMethod();

        Fruit fruit = new Fruit();
        Apple apple = new Apple();
        Integer i = 10000;
        Float f = 10000f;
        int intValue = 10;

        // 最开始找的是不考虑装箱拆箱、变长参数的方法；如果 Java 编译器在同一个阶段中找到了多个适配的方法，那么它会在其中选择一个最为贴切的，而决定贴切程度的一个关键就是形式参数类型的继承关系。
        selectMethod.show(i, apple); // 可以找到 1和2 但是1更贴切
        selectMethod.show(intValue, apple);

        // 如果上一步没有找到，那么就考虑装箱拆箱、但是不考虑变长
        selectMethod.show(f, apple);

        // 最后考虑装箱拆箱+变长
//        selectMethod.show(intValue, fruit, intValue); // 由于同时考虑了拆箱装箱与变长，编译器无法识别执行7还是8
//        selectMethod.show(intValue, apple, intValue);
        selectMethod.show(f, fruit, intValue);
    }

}
