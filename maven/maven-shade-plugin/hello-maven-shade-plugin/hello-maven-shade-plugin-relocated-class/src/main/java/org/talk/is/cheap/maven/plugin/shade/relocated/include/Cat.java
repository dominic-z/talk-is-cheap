package org.talk.is.cheap.maven.plugin.shade.relocated.include;

import org.talk.is.cheap.maven.plugin.shade.relocated.exclude.Car;

/**
 * @author dominiczhu
 * @version 1.0
 * @title Cat
 * @date 2022/4/1 1:16 下午
 */
public class Cat implements Animal {
    @Override
    public void eat() {
        System.out.println("cat can eat a lot, " + age);
    }

    public void drive() {
        final Car car = new Car();
        System.out.println("cat can drive a car" + car);
    }
}
