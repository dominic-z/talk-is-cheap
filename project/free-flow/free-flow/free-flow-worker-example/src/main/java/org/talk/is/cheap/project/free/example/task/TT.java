package org.talk.is.cheap.project.free.example.task;


import org.talk.is.cheap.project.free.flow.starter.worker.task.definition.annotaion.stage.RunnableStage;
import org.talk.is.cheap.project.free.flow.starter.worker.task.definition.annotaion.task.Task;

@Task(name = "aaa", version = 1)
public class TT {

    public TT() {
        System.out.println("触发构造函数");

    }

    @RunnableStage(name = "method1", version = 1, toStageName = "method2", isStartingStage = true)
    public void method1() {

    }

    @RunnableStage(name = "method2", version = 1)
    public void method2() {

    }
}
