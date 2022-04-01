package org.talk.is.cheap.maven.plugin.assembly.service;

import com.google.common.collect.Lists;

import java.util.ArrayList;

/**
 * @author dominiczhu
 * @version 1.0
 * @title HelloService
 * @date 2022/4/1 2:47 下午
 */
public class HelloService {

    public void sayHello(String name) {

        final ArrayList<String> single = Lists.newArrayList("single");
        System.out.println(single);
        System.out.println("hello " + name);
    }

    public static void main(String[] args) {
        new HelloService().sayHello("hi!");
    }

}
