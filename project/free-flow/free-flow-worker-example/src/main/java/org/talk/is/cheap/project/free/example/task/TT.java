package org.talk.is.cheap.project.free.example.task;


import org.talk.is.cheap.project.free.flow.starter.worker.task.defination.annotaion.stage.RunnableStage;
import org.talk.is.cheap.project.free.flow.starter.worker.task.defination.annotaion.task.Task;

@Task(name = "aaa")
public class TT {

    public TT(){
        System.out.println("触发构造函数");

    }

    @RunnableStage(name = "method1",to="method2")
    public void method1(){

    }

    @RunnableStage(name = "method1")
    public void method2(){

    }
}
