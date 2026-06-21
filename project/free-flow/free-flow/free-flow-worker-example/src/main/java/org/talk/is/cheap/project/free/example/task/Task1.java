package org.talk.is.cheap.project.free.example.task;


import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.talk.is.cheap.project.free.flow.common.task.codec.JsonInputCodec;
import org.talk.is.cheap.project.free.flow.starter.worker.task.definition.annotaion.stage.RunnableStage;
import org.talk.is.cheap.project.free.flow.starter.worker.task.definition.annotaion.task.Task;
import org.talk.is.cheap.project.free.flow.starter.worker.task.driver.runtime.StageRuntimeEnv;

import java.util.Random;

@Task(name = "task1", version = 3, sharedContextCodecClass = Task1.TTSharedContext.TTSharedContextInputClass.class)
@Slf4j
public class Task1 {

    @Data
    public static class TTMethod1Input {
        public static class TTMethod1InputCodec extends JsonInputCodec<TTMethod1Input> {

        }

        @Data
        public static class TTMethod1InputInner {
            private String address;
        }

        private int num;
        private String name;
        private TTMethod1InputInner ttMethod1InputInner;
    }

    @Data
    public static class TTMethod3Input {
        public static class TTMethod3InputCodec extends JsonInputCodec<TTMethod3Input> {

        }

        @Data
        public static class TTMethod3InputInner {
            private String address;
        }

        private int age;
        private TTMethod3InputInner ttMethod3InputInner;
    }


    @Data
    public class TTSharedContext {

        public static class TTSharedContextInputClass extends JsonInputCodec<TTSharedContext> {

        }

        @Data
        public static class TTSharedContextInnerData {
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
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        TTSharedContext ttSharedContext = stageRuntimeEnv.getSharedContext();
        ttSharedContext.setName("abcd");
        stageRuntimeEnv.log("method1");
        log.info("method1 sharedContext: {}", stageRuntimeEnv);
    }

    @RunnableStage(name = "method2", toStageName = {"method3"}, version = 1)
    public void method2(StageRuntimeEnv<?> stageRuntimeEnv) {
        log.info("method2");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        TTSharedContext ttSharedContext = stageRuntimeEnv.getSharedContext();
        ttSharedContext.getTtSharedContextInnerData().setCache("cache in method2");
        stageRuntimeEnv.log("method2");
        log.info("method2 sharedContext: {}", stageRuntimeEnv);
    }


    @RunnableStage(name = "method3", version = 1, toStageName = {"method4_1", "method4_2", "method4_3"},
            inputCodecClass = TTMethod3Input.TTMethod3InputCodec.class)
    public void method3(StageRuntimeEnv<TTMethod3Input> stageRuntimeEnv) {
        log.info("method3");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        log.info("method3: {}", stageRuntimeEnv.getInput());
        TTSharedContext ttSharedContext = stageRuntimeEnv.getSharedContext();
        log.info("method3 sharedContext: {}", ttSharedContext);
    }

    @RunnableStage(name = "method4_1", version = 1)
    public void method4_1(StageRuntimeEnv<?> stageRuntimeEnv) {
        try {
            Thread.sleep((new Random(10).nextInt(3) + 2) * 1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


        TTSharedContext ttSharedContext = stageRuntimeEnv.getSharedContext();
        log.info("method41, context: {}", ttSharedContext);
        ttSharedContext.setName("method4");
        if (ttSharedContext.getNum() % 2 == 0) {
            ttSharedContext.setNum(ttSharedContext.getNum() + 1);
            throw new RuntimeException("测试失败");
        }

    }

    @RunnableStage(name = "method4_2", version = 1)
    public void method4_2() {
        log.info("method42");
        try {
            Thread.sleep((new Random(10).nextInt(3) + 2) * 1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @RunnableStage(name = "method4_3", version = 1)
    public void method4_3() {

        log.info("method43");
        try {
            Thread.sleep((new Random(10).nextInt(3) + 2) * 1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
