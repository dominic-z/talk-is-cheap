package org.talk.is.cheap.project.free.example.task;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.talk.is.cheap.project.free.flow.common.task.codec.JsonInputCodec;
import org.talk.is.cheap.project.free.flow.starter.worker.task.definition.annotaion.stage.RunnableStage;
import org.talk.is.cheap.project.free.flow.starter.worker.task.definition.annotaion.task.Task;
import org.talk.is.cheap.project.free.flow.starter.worker.task.driver.runtime.StageRuntimeEnv;

/**
 * 用于测试Worker下线后任务重新调度的测试Task。
 * 3个Stage串行执行，每个Stage sleep 10秒，提供足够时间窗口关闭Worker。
 * 通过SharedContext记录已完成的Stage数量，验证恢复后不重复执行已完成Stage。
 */
@Task(name = "reschedule-test-task", version = 1, maxRetryCount = 3,
        sharedContextCodecClass = RescheduleTestTask.RescheduleTestSharedContext.RescheduleTestSharedContextCodec.class)
@Slf4j
public class RescheduleTestTask {

    @Data
    public static class RescheduleTestSharedContext {
        public static class RescheduleTestSharedContextCodec extends JsonInputCodec<RescheduleTestSharedContext> {
        }

        private int completedStageCount = 0;
        private String lastCompletedStage = "";
    }

    @RunnableStage(name = "stage1", version = 1, isStartingStage = true, toStageName = "stage2")
    public void stage1(StageRuntimeEnv<?> stageRuntimeEnv) {
        log.info("[RescheduleTest] stage1 开始执行");
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        RescheduleTestSharedContext ctx = (RescheduleTestSharedContext) stageRuntimeEnv.getSharedContext();
        ctx.setCompletedStageCount(ctx.getCompletedStageCount() + 1);
        ctx.setLastCompletedStage("stage1");
        stageRuntimeEnv.log("stage1 completed");
        log.info("[RescheduleTest] stage1 执行完成, completedStageCount: {}", ctx.getCompletedStageCount());
    }

    @RunnableStage(name = "stage2", version = 1, toStageName = "stage3")
    public void stage2(StageRuntimeEnv<?> stageRuntimeEnv) {
        log.info("[RescheduleTest] stage2 开始执行");
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        RescheduleTestSharedContext ctx = (RescheduleTestSharedContext) stageRuntimeEnv.getSharedContext();
        ctx.setCompletedStageCount(ctx.getCompletedStageCount() + 1);
        ctx.setLastCompletedStage("stage2");
        stageRuntimeEnv.log("stage2 completed");
        log.info("[RescheduleTest] stage2 执行完成, completedStageCount: {}", ctx.getCompletedStageCount());
    }

    @RunnableStage(name = "stage3", version = 1)
    public void stage3(StageRuntimeEnv<?> stageRuntimeEnv) {
        log.info("[RescheduleTest] stage3 开始执行");
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        RescheduleTestSharedContext ctx = (RescheduleTestSharedContext) stageRuntimeEnv.getSharedContext();
        ctx.setCompletedStageCount(ctx.getCompletedStageCount() + 1);
        ctx.setLastCompletedStage("stage3");
        stageRuntimeEnv.log("stage3 completed");
        log.info("[RescheduleTest] stage3 执行完成, completedStageCount: {}", ctx.getCompletedStageCount());
    }
}
