package org.talk.is.cheap.project.free.example.task;


import lombok.extern.slf4j.Slf4j;
import org.talk.is.cheap.project.free.example.task.param.TTMethod1Input;
import org.talk.is.cheap.project.free.example.task.param.TTSharedContext;
import org.talk.is.cheap.project.free.flow.starter.worker.task.definition.annotaion.stage.RunnableStage;
import org.talk.is.cheap.project.free.flow.starter.worker.task.definition.annotaion.task.Task;
import org.talk.is.cheap.project.free.flow.starter.worker.task.driver.runtime.StageRuntimeEnv;

@Task(name = "aaa", version = 1, sharedContextCodecClass = TTSharedContext.TTSharedContextInputClass.class)
@Slf4j
public class TT {


    public TT() {
        System.out.println("触发构造函数");

    }

    @RunnableStage(name = "method1", version = 1, toStageName = "method2", isStartingStage = true, inputCodecClass =
            TTMethod1Input.TTMethod1InputCodec.class)
    public void method1(StageRuntimeEnv<TTMethod1Input> stageRuntimeEnv) {
        log.info("method1 input: {}", stageRuntimeEnv.getInput());
        TTSharedContext ttSharedContext = stageRuntimeEnv.getSharedContext();
        log.info("method1 sharedContext: {}", stageRuntimeEnv);
    }

    @RunnableStage(name = "method2", toStageName = {"method31", "method32"}, version = 1)
    public void method2() {
        log.info("method2");
    }


    @RunnableStage(name = "method31", version = 1)
    public void method31() {
        log.info("method31");


    }

    @RunnableStage(name = "method32", version = 1)
    public void method32() {
        log.info("method32");

    }
}
