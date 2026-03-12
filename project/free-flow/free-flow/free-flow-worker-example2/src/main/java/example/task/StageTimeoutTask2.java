package example.task;


import example.repository.DemoDao;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.talk.is.cheap.project.free.flow.common.task.codec.JsonInputCodec;
import org.talk.is.cheap.project.free.flow.starter.worker.task.definition.annotaion.stage.RunnableStage;
import org.talk.is.cheap.project.free.flow.starter.worker.task.definition.annotaion.task.Task;
import org.talk.is.cheap.project.free.flow.starter.worker.task.driver.runtime.StageRuntimeEnv;

import java.util.Random;

@Task(name = "stage-timeout-task2", version = 2, sharedContextCodecClass =
        StageTimeoutTask2.TTSharedContext.TTSharedContextInputClass.class, timeout = 25)
@Slf4j
public class StageTimeoutTask2 {

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
    public static class TTMethod5Input {
        public static class TTMethod5InputCodec extends JsonInputCodec<TTMethod5Input> {

        }

        @Data
        public static class TTMethod5InputInner {
            private String name;
            private Integer id;
        }

        private int age;
        private TTMethod5InputInner ttMethod5InputInner;
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

    public StageTimeoutTask2() {
        System.out.println("触发构造函数");

    }


    @RunnableStage(name = "method1", version = 1, toStageName = "method2", isStartingStage = true,
            inputCodecClass = TTMethod1Input.TTMethod1InputCodec.class)
    public void method1(StageRuntimeEnv<TTMethod1Input> stageRuntimeEnv) {
        log.info("method1 input: {}", stageRuntimeEnv.getInput());

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        TTSharedContext ttSharedContext = stageRuntimeEnv.getSharedContext();
        ttSharedContext.setName("abcd");
        stageRuntimeEnv.log("method1");
        log.info("method1 sharedContext: {}", stageRuntimeEnv);
    }

    @RunnableStage(name = "method2", toStageName = {"method3"}, version = 1, timeoutInSecond = 3)
    public void method2(StageRuntimeEnv<?> stageRuntimeEnv) {
        log.info("method2");
        TTSharedContext ttSharedContext = stageRuntimeEnv.getSharedContext();
        log.info("ttSharedContext: {}", ttSharedContext);
        try {
            if (ttSharedContext.getNum() < 1) {
                ttSharedContext.setNum(ttSharedContext.getNum() + 1);
                Thread.sleep(5000);
            } else {
                Thread.sleep(2000);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        ttSharedContext.getTtSharedContextInnerData().setCache("cache in method2");
        stageRuntimeEnv.log("method2_1");
        stageRuntimeEnv.log("method2_2");
        log.info("method2 sharedContext: {}", stageRuntimeEnv);
    }


    @RunnableStage(name = "method3", version = 1, toStageName = {"method4_1", "method4_2"},
            inputCodecClass = TTMethod3Input.TTMethod3InputCodec.class)
    public void method3(StageRuntimeEnv<TTMethod3Input> stageRuntimeEnv) {
        log.info("method3");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        log.info("method3: {}", stageRuntimeEnv.getInput());
        TTSharedContext ttSharedContext = stageRuntimeEnv.getSharedContext();
        ttSharedContext.setNum(20);
        log.info("method3 sharedContext: {}", ttSharedContext);
    }

    @RunnableStage(name = "method4_1", version = 1, toStageName = "method5", timeoutInSecond = 3)
    public void method4_1(StageRuntimeEnv<?> stageRuntimeEnv) {
        try {
//            Thread.sleep((new Random(10).nextInt(3)) * 1000);
            Thread.sleep(10);

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


        TTSharedContext ttSharedContext = stageRuntimeEnv.getSharedContext();
        log.info("method41, context: {}", ttSharedContext);
        ttSharedContext.setName("method4");
    }

    @RunnableStage(name = "method4_2", version = 1, toStageName = "method5")
    public void method4_2() {
        log.info("method42, getQuantity: {}", demoDao.getQuantity());
        try {
            //        用来测试task第一次失败第二次执行成功
            if (demoDao.getQuantity() < 1) {
                demoDao.addQuantity(1);
                Thread.sleep(20000);
            } else {
                demoDao.addQuantity(1);
                Thread.sleep(300);
            }

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Autowired
    private DemoDao demoDao;

    @RunnableStage(name = "method5", version = 1,
            inputCodecClass = TTMethod5Input.TTMethod5InputCodec.class, timeoutInSecond = 3)
    public void method5(StageRuntimeEnv<TTMethod5Input> stageRuntimeEnv) {

        log.info("method5, input :{}", stageRuntimeEnv.getInput());
        try {
            // 测试最后一个stage超时不干扰task是否超时
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
