package org.talk.is.cheap.maven.plugin.shade.pojo;

import lombok.Builder;
import lombok.Data;

/**
 * @author dominiczhu
 * @version 1.0
 * @title Person
 * @date 2022/4/1 11:00 上午
 */
@Data
@Builder
public class Person {
    private String name;
    private int age;
}
