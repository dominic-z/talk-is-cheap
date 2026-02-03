package org.talk.is.cheap.project.free.example.task;


import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.talk.is.cheap.project.free.flow.common.task.codec.JsonInputCodec;
import org.talk.is.cheap.project.free.flow.starter.worker.task.definition.annotaion.stage.RunnableStage;
import org.talk.is.cheap.project.free.flow.starter.worker.task.definition.annotaion.task.Task;
import org.talk.is.cheap.project.free.flow.starter.worker.task.driver.runtime.StageRuntimeEnv;

@Task(name = "task1", version = 1, sharedContextCodecClass = Task1.TTSharedContext.TTSharedContextInputClass.class)
@Slf4j
public class Task1 {

    @Data
    public static class TTMethod1Input {


        public static class TTMethod1InputCodec extends JsonInputCodec<TTMethod1Input> {

        }


        @Data
        public static class  TTMethod1InputInner{
            private String address;
        }

        private int num;
        private String name;
        private TTMethod1InputInner ttMethod1InputInner;
    }


    @Data
    public class TTSharedContext {

        public static class TTSharedContextInputClass extends JsonInputCodec<TTSharedContext> {

        }
        @Data
        public static class  TTSharedContextInnerData{
            private String address;
            private String cache;
        }

        private int num;
        private String name;
        private TTSharedContextInnerData ttSharedContextInnerData;
    }

    public Task1() {
        System.out.println("触发构造函数");

    }

    @RunnableStage(name = "method1", version = 1, toStageName = "method2", isStartingStage = true,
            inputCodecClass = TTMethod1Input.TTMethod1InputCodec.class)
    public void method1(StageRuntimeEnv<TTMethod1Input> stageRuntimeEnv) {
        log.info("method1 input: {}", stageRuntimeEnv.getInput());

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        TTSharedContext ttSharedContext = stageRuntimeEnv.getSharedContext();
        ttSharedContext.setName("abcd");
        stageRuntimeEnv.log("method1");
        log.info("method1 sharedContext: {}", stageRuntimeEnv);
    }

    @RunnableStage(name = "method2", toStageName = {"method31", "method32"}, version = 1)
    public void method2(StageRuntimeEnv<?> stageRuntimeEnv) {
        log.info("method2");
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        TTSharedContext ttSharedContext = stageRuntimeEnv.getSharedContext();
        ttSharedContext.getTtSharedContextInnerData().setCache("cache in method2");
        stageRuntimeEnv.log("method2");
        log.info("method1 sharedContext: {}", stageRuntimeEnv);
    }


    @RunnableStage(name = "method31", version = 1)
    public void method31() {
        log.info("method31");
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    @RunnableStage(name = "method32", version = 1)
    public void method32() {
        log.info("method32");
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
