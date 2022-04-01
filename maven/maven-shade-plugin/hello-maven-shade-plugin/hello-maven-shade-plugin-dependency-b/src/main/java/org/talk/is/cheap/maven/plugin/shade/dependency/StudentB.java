package org.talk.is.cheap.maven.plugin.shade.dependency;

import com.google.common.base.Objects;

/**
 * @author dominiczhu
 * @version 1.0
 * @title StudentB
 * @date 2022/4/1 11:39 上午
 */
public class StudentB {
    public Integer gender;
    public String name;

    @Override
    public String toString() {
        // guava19有的方法，但是guava26没有
        Objects.ToStringHelper toStringHelper = Objects.toStringHelper(new Object());
        return "StudentB{" +
                "gender=" + gender +
                ", name='" + name + '\'' +
                '}';
    }
}
