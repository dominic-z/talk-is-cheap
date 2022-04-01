package org.talk.is.cheap.maven.plugin.shade.relocated.exclude;

import org.talk.is.cheap.maven.plugin.shade.relocated.include.Animal;
import org.talk.is.cheap.maven.plugin.shade.relocated.include.Cat;

/**
 * @author dominiczhu
 * @version 1.0
 * @title Car
 * @date 2022/4/1 1:21 下午
 */
public class Car implements Vehicle{
    @Override
    public void run() {
        System.out.println("car run run run !");
    }


    public Animal driver(){
        System.out.println("a cat is driving this car");
        return new Cat();
    }
}
