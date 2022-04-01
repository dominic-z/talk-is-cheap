package org.talk.is.cheap.maven.plugin.shade;

import org.talk.is.cheap.maven.plugin.shade.pojo.Person;

/**
 * @author dominiczhu
 * @version 1.0
 * @title Main
 * @date 2022/4/1 11:00 上午
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("start main");
        System.out.println(Person.builder().age(20).name("dom").build());
    }
}
