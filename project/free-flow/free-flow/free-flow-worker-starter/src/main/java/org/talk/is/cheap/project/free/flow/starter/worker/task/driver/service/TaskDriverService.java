package org.talk.is.cheap.project.free.flow.starter.worker.task.driver.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.talk.is.cheap.project.free.flow.common.message.impl.StartWorkerTaskReq;
import org.talk.is.cheap.project.free.flow.common.task.codec.InputCodec;
import org.talk.is.cheap.project.free.flow.common.task.definition.bo.StageDefinitionBO;
import org.talk.is.cheap.project.free.flow.common.task.definition.bo.TaskDefinitionBO;
import org.talk.is.cheap.project.free.flow.common.utils.VerifyUtil;
import org.talk.is.cheap.project.free.flow.starter.worker.task.definition.service.LocalTaskDefinitionService;
import org.talk.is.cheap.project.free.flow.starter.worker.task.driver.runtime.StageRuntimeEnv;
import org.talk.is.cheap.project.free.flow.starter.worker.task.driver.runtime.TaskRuntimeEnv;
import org.talk.is.cheap.project.free.flow.starter.worker.task.driver.runtime.TaskRuntimeService;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


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
    public boolean startTask(String taskName, Integer taskVersion, StartWorkerTaskReq.TaskStartupData taskStartupData) throws Exception {
        VerifyUtil.shallBeTrue(canStartTask(taskName, taskVersion),
                String.format("worker can't ran run task: %s, version: %d", taskName, taskVersion));

        TaskDefinitionBO taskDefinitionBO = localTaskDefinitionService.getTaskDefinitionBO(taskName);
        Class<?> taskClass = taskDefinitionBO.getTaskClass();
        Object taskBean = applicationContext.getBean(taskClass);
        taskRuntimeService.createTaskRuntimeEnv(taskDefinitionBO, taskStartupData);

        taskBeanMap.put(taskStartupData.getTaskStartupId(), taskBean);
        List<StageDefinitionBO> rootStages =
                taskDefinitionBO.getRoots().stream().map(n -> taskDefinitionBO.getStageDefinitionMap().get(n)).toList();

        for (StageDefinitionBO rootStage : rootStages) {
            Long stageStartupId = taskStartupData.getStageStartupDataMap().get(rootStage.getName()).getStageStartupId();

            Method stageMethod = taskClass.getDeclaredMethod(rootStage.getMethodName(),
                    Arrays.stream(rootStage.getParameters()).map(Parameter::getType).toArray(Class[]::new));
            CompletableFuture<Void> stageFuture = CompletableFuture.runAsync(() -> {
                try {
                    if (rootStage.getParameters().length == 0) {
                        stageMethod.invoke(taskBean, null);
                    } else {
                        stageMethod.invoke(taskBean, taskRuntimeService.getStageRuntimeEnv(stageStartupId));
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
        }

        return true;
    }


}
