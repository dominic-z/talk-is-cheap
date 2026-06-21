package org.talk.is.cheap.project.free.example.task;


import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.talk.is.cheap.project.free.example.repository.DemoDao;
import org.talk.is.cheap.project.free.flow.common.task.codec.InputCodec;
import org.talk.is.cheap.project.free.flow.common.task.codec.JsonInputCodec;
import org.talk.is.cheap.project.free.flow.starter.worker.task.definition.annotaion.stage.RunnableStage;
import org.talk.is.cheap.project.free.flow.starter.worker.task.definition.annotaion.task.Task;
import org.talk.is.cheap.project.free.flow.starter.worker.task.driver.runtime.StageRuntimeEnv;

/**
 * 测试优雅关闭
 */
@Task(name = "task3", version = 3, sharedContextCodecClass = Task3.TTSharedContext.TTSharedContextInputClass.class, maxRetryCount = 2)
@Slf4j
public class Task3 {

    @Data
    public static class TTSharedContext {

        public static class TTSharedContextInputClass extends JsonInputCodec<TTSharedContext> {

        }

        private int num;
        private String name;

        public void setName(String name) {
            synchronized (this) {
                this.name = name;
            }
        }
    }

    public static class SimpleIntInputCodec extends InputCodec<Integer> {

        @Override
        public String encode(Integer integer) {
            if (integer == null) {
                return null;
            }
            return Integer.toString(integer);
        }

        @Override
        public Integer decode(String encode, Class<Integer> integerClass) {
            if (encode == null) {
                return null;
            }
            return Integer.parseInt(encode);
        }
    }

    @Autowired
    private DemoDao demoDao;


    @RunnableStage(name = "method1", version = 1, toStageName = "method2", isStartingStage = true,
            inputCodecClass = SimpleIntInputCodec.class)
    public void method1(StageRuntimeEnv<Integer> stageRuntimeEnv) {
        TTSharedContext ttSharedContext = stageRuntimeEnv.getSharedContext();
        log.info("method1 input: {}, sharedContext: {}", stageRuntimeEnv.getInput(), ttSharedContext);
        ttSharedContext.setName("method1");
//        demoDao.reset(0);
        try {
            if (stageRuntimeEnv.getInput() != null) {
                Thread.sleep(stageRuntimeEnv.getInput() * 1000);
            }

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        stageRuntimeEnv.log("method1");
    }

    @RunnableStage(name = "method2", toStageName = {"method3_1", "method3_2", "method3_3"}, version = 1,
            inputCodecClass = SimpleIntInputCodec.class)
    public void method2(StageRuntimeEnv<Integer> stageRuntimeEnv) {
        TTSharedContext ttSharedContext = stageRuntimeEnv.getSharedContext();
        log.info("method2 input: {}, sharedContext: {}", stageRuntimeEnv.getInput(), ttSharedContext);
        ttSharedContext.setName(ttSharedContext.getName() + ":method2");
        try {
            if (stageRuntimeEnv.getInput() != null) {
                Thread.sleep(stageRuntimeEnv.getInput() * 1000);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    @RunnableStage(name = "method3_1", toStageName = {"method4_1", "method4_2"}, version = 1,
            inputCodecClass = SimpleIntInputCodec.class)
    public void method3_1(StageRuntimeEnv<Integer> stageRuntimeEnv) {
        TTSharedContext ttSharedContext = stageRuntimeEnv.getSharedContext();
        log.info("method3_1 input: {}, sharedContext: {}", stageRuntimeEnv.getInput(), ttSharedContext);
        ttSharedContext.setName(ttSharedContext.getName() + ":method3_1");
        try {
            if (stageRuntimeEnv.getInput() != null) {
                Thread.sleep(stageRuntimeEnv.getInput() * 1000);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @RunnableStage(name = "method3_2", toStageName = {"method4_1", "method4_2"}, version = 1, maxRetryCount = 2,
            inputCodecClass = SimpleIntInputCodec.class)
    public void method3_2(StageRuntimeEnv<Integer> stageRuntimeEnv) {
        TTSharedContext ttSharedContext = stageRuntimeEnv.getSharedContext();
        log.info("method3_2 input: {}, sharedContext: {}", stageRuntimeEnv.getInput(), ttSharedContext);
        ttSharedContext.setName(ttSharedContext.getName() + ":method3_2");
        try {
            if (stageRuntimeEnv.getInput() != null) {
                Thread.sleep(stageRuntimeEnv.getInput() * 1000);
            }

//            if (demoDao.getQuantity() < 3) {
//                // 测试task整体失败的情况
//                demoDao.addQuantity(1);
//                throw new RuntimeException("测试异常");
//            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @RunnableStage(name = "method3_3", version = 1,
            inputCodecClass = SimpleIntInputCodec.class)
    public void method3_3(StageRuntimeEnv<Integer> stageRuntimeEnv) {
        TTSharedContext ttSharedContext = stageRuntimeEnv.getSharedContext();
        log.info("method3_3 input: {}, sharedContext: {}", stageRuntimeEnv.getInput(), ttSharedContext);
        ttSharedContext.setName(ttSharedContext.getName() + ":method3_3");
        try {
            if (stageRuntimeEnv.getInput() != null) {
                Thread.sleep(stageRuntimeEnv.getInput() * 1000);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @RunnableStage(name = "method4_1", toStageName = {"method5"}, version = 1,
            inputCodecClass = SimpleIntInputCodec.class)
    public void method4_1(StageRuntimeEnv<Integer> stageRuntimeEnv) {
        TTSharedContext ttSharedContext = stageRuntimeEnv.getSharedContext();
        log.info("method4_1 input: {}, sharedContext: {}", stageRuntimeEnv.getInput(), ttSharedContext);
        ttSharedContext.setName(ttSharedContext.getName() + ":method4_1");
        try {
            if (stageRuntimeEnv.getInput() != null) {
                Thread.sleep(stageRuntimeEnv.getInput() * 1000);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @RunnableStage(name = "method4_2", toStageName = {"method5"}, version = 1,
            inputCodecClass = SimpleIntInputCodec.class)
    public void method4_2(StageRuntimeEnv<Integer> stageRuntimeEnv) {
        TTSharedContext ttSharedContext = stageRuntimeEnv.getSharedContext();
        log.info("method4_2 input: {}, sharedContext: {}", stageRuntimeEnv.getInput(), ttSharedContext);
        ttSharedContext.setName(ttSharedContext.getName() + ":method4_2");
        try {
            if (stageRuntimeEnv.getInput() != null) {
                Thread.sleep(stageRuntimeEnv.getInput() * 1000);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @RunnableStage(name = "method5", version = 1,
            inputCodecClass = SimpleIntInputCodec.class)
    public void method5(StageRuntimeEnv<Integer> stageRuntimeEnv) {
        TTSharedContext ttSharedContext = stageRuntimeEnv.getSharedContext();
        log.info("method5 input: {}, sharedContext: {}", stageRuntimeEnv.getInput(), ttSharedContext);
        ttSharedContext.setName(ttSharedContext.getName() + ":method5");
        try {
            if (stageRuntimeEnv.getInput() != null) {
                Thread.sleep(stageRuntimeEnv.getInput() * 1000);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
