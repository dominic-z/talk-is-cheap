package org.talk.is.cheap.maven.plugin.shade.deal.with.confict;

import org.talk.is.cheap.maven.plugin.shade.dependency.StudentA;
import org.talk.is.cheap.maven.plugin.shade.dependency.StudentB;

/**
 * @author dominiczhu
 * @version 1.0
 * @title Main
 * @date 2022/4/1 1:40 下午
 */
public class Main {
    public static void main(String[] args) {
        final StudentB studentB = new StudentB();
        System.out.println(studentB.toString());
        final StudentA studentA = new StudentA();
        System.out.println(studentA.toString());

    }
}
