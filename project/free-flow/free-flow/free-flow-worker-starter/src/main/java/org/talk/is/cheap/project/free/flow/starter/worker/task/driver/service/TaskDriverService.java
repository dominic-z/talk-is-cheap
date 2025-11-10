package org.talk.is.cheap.project.free.flow.starter.worker.task.driver.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.talk.is.cheap.project.free.flow.common.message.impl.worker.StartWorkerStageReq;
import org.talk.is.cheap.project.free.flow.common.task.definition.bo.StageDefinitionBO;
import org.talk.is.cheap.project.free.flow.common.task.definition.bo.TaskDefinitionBO;
import org.talk.is.cheap.project.free.flow.common.utils.VerifyUtil;
import org.talk.is.cheap.project.free.flow.starter.worker.task.definition.service.LocalTaskDefinitionService;
import org.talk.is.cheap.project.free.flow.starter.worker.task.driver.runtime.StageRuntimeEnv;
import org.talk.is.cheap.project.free.flow.starter.worker.task.driver.runtime.TaskRuntimeService;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * 如何驱动一个task的运行？分为两步：
 * 1. scheduler寻找可以运行当前任务的worker：通过hash分片找到一个worker，与这个worker进行交互询问是否可以支持分配
 * 2. 如果可以，scheduler告知这个worker启动这个task，由task实际启动执行。执行过程中将结果返回至scheduler
 */
@Slf4j
@Service
public class TaskDriverService {

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private LocalTaskDefinitionService localTaskDefinitionService;

    @Autowired
    private TaskRuntimeService taskRuntimeService;

    private ThreadPoolExecutor threadPoolExecutor;

    private final Map<Long, Object> taskBeanMap = new ConcurrentHashMap<>();

    @PostConstruct
    private void init() {
        int cpuNum = Runtime.getRuntime().availableProcessors();
        threadPoolExecutor = new ThreadPoolExecutor(cpuNum, cpuNum * 2, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    }

    /**
     * 本worker是否能运行某个task，如果taskVersion为null，则不关心具体是什么版本
     *
     * @param taskName
     * @param taskVersion
     * @return
     */
    public boolean canStartTask(String taskName, Integer taskVersion) {

        return localTaskDefinitionService.hasTask(taskName, taskVersion);

    }

    /**
     * 驱动一个taskName，如果taskVersion为null，则不关心具体什么版本
     *
     * @param taskName
     * @param taskVersion
     * @return
     */
    public boolean startStage(StartWorkerStageReq.StageStartupData taskStartupData) throws Exception {
        VerifyUtil.shallBeTrue(canStartTask(taskStartupData.getTaskName(), taskStartupData.getTaskVersion()),
                String.format("worker can't ran run task: %s, version: %d", taskStartupData.getTaskName(),
                        taskStartupData.getTaskVersion()));

        TaskDefinitionBO taskDefinitionBO = localTaskDefinitionService.getTaskDefinitionBO(taskStartupData.getTaskName());
        Class<?> taskClass = taskDefinitionBO.getTaskClass();
        Object taskBean = applicationContext.getBean(taskClass);
        StageRuntimeEnv stageRuntimeEnv = taskRuntimeService.createStageRuntimeEnv(taskDefinitionBO, taskStartupData);

        taskBeanMap.put(taskStartupData.getTaskStartupId(), taskBean);


        StageDefinitionBO stageDefinitionBO = taskDefinitionBO.getStageDefinitionMap().get(taskStartupData.getStageName());
        Method stageMethod = taskClass.getDeclaredMethod(stageDefinitionBO.getMethodName(),
                Arrays.stream(stageDefinitionBO.getParameters()).map(Parameter::getType).toArray(Class[]::new));
        CompletableFuture<Void> stageFuture = CompletableFuture.runAsync(() -> {
            try {
                if (stageDefinitionBO.getParameters().length == 0) {
                    stageMethod.invoke(taskBean, null);
                } else {
                    stageMethod.invoke(taskBean, stageRuntimeEnv);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, threadPoolExecutor);

        stageFuture.thenAccept((v) -> {
            // todo: stage完成
        }).exceptionally((e) -> {
            // todo: 发生异常
            return null;
        });

        return true;
    }


}
