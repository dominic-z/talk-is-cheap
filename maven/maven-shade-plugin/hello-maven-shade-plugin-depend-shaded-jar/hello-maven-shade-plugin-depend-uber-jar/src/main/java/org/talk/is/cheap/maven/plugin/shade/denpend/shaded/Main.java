package org.talk.is.cheap.maven.plugin.shade.denpend.shaded;

import org.talk.is.cheap.maven.plugin.shade.pojo.Person;

/**
 * @author dominiczhu
 * @version 1.0
 * @title Main
 * @date 2022/4/1 11:40 上午
 */
public class Main {
    public static void main(String[] args) {
        final Person zz = Person.builder().name("zz").build();
        System.out.println(zz);
    }
}
