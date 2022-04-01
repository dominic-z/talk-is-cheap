package org.talk.is.cheap.maven.plugin.shade.dependency;

import com.google.common.base.Strings;

/**
 * @author dominiczhu
 * @version 1.0
 * @title StudentA
 * @date 2022/4/1 11:38 上午
 */
public class StudentA {
    public Integer age;
    public String name;

    @Override
    public String toString() {

        // guava26有的方法，但是guava19没有
        final String lenientFormat = Strings.lenientFormat("[{}]", "lenientFormat");
        System.out.println(lenientFormat);
        return "StudentA{" +
                "age=" + age +
                ", name='" + name + '\'' +
                '}';
    }
}
