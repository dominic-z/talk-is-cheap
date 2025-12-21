package org.talk.is.cheap.project.free.flow.starter.worker.task.driver.service;

import cn.hutool.core.collection.ConcurrentHashSet;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.talk.is.cheap.project.free.flow.common.message.impl.scheduler.PrepareStageReq;
import org.talk.is.cheap.project.free.flow.common.message.impl.scheduler.PrepareStageResp;
import org.talk.is.cheap.project.free.flow.common.message.impl.scheduler.WorkerCompleteStageResultReq;
import org.talk.is.cheap.project.free.flow.common.message.impl.scheduler.WorkerStartStageReportReq;
import org.talk.is.cheap.project.free.flow.common.message.impl.worker.WorkerStartTaskReq;
import org.talk.is.cheap.project.free.flow.common.task.definition.bo.StageDefinitionBO;
import org.talk.is.cheap.project.free.flow.common.task.definition.bo.TaskDefinitionBO;
import org.talk.is.cheap.project.free.flow.common.utils.FieldAwareLockManager;
import org.talk.is.cheap.project.free.flow.common.utils.VerifyUtil;
import org.talk.is.cheap.project.free.flow.starter.worker.client.SchedulerTaskProcessClient;
import org.talk.is.cheap.project.free.flow.starter.worker.cluster.service.ClusterService;
import org.talk.is.cheap.project.free.flow.starter.worker.task.definition.service.LocalTaskDefinitionService;
import org.talk.is.cheap.project.free.flow.starter.worker.task.driver.runtime.StageRuntimeEnv;
import org.talk.is.cheap.project.free.flow.starter.worker.task.driver.runtime.TaskRuntimeEnv;
import org.talk.is.cheap.project.free.flow.starter.worker.task.driver.runtime.TaskRuntimeService;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URI;
import java.util.Arrays;
import java.util.Date;
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

    @Autowired
    private ClusterService clusterService;

    @Autowired
    private SchedulerTaskProcessClient schedulerTaskProcessClient;

    private ThreadPoolExecutor threadPoolExecutor;

    private final Map<Long, Object> taskExecutionIdBeanMap = new ConcurrentHashMap<>();
    private final Map<Long, Set<String>> taskExecutionIdFinishedStageMap = new ConcurrentHashMap<>();
    // 已经提交的stage的任务，避免重复提交
    private final Map<Long, Set<String>> taskExecutionIdDispatchedStageMap = new ConcurrentHashMap<>();

    private FieldAwareLockManager<Long> taskExecutionLockManager = new FieldAwareLockManager<>();
    private Map<Long, CompletableFuture<?>> taskExecutionIdFuture = new ConcurrentHashMap<>();


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
    public boolean startTask(WorkerStartTaskReq.Data workerStartTaskData) throws Exception {
        VerifyUtil.shallBeTrue(canStartTask(workerStartTaskData.getTaskName(), workerStartTaskData.getTaskVersion()),
                String.format("worker can't ran run task: %s, version: %d", workerStartTaskData.getTaskName(),
                        workerStartTaskData.getTaskVersion()));

        TaskDefinitionBO taskDefinitionBO = localTaskDefinitionService.getTaskDefinitionBO(workerStartTaskData.getTaskName());

        Map<String, Long> startingStageExecutionId = workerStartTaskData.getStartingStageExecutionId();
        VerifyUtil.shallBeTrue(taskDefinitionBO.getStartingStageNames().containsAll(startingStageExecutionId.keySet())
                        && startingStageExecutionId.size() == taskDefinitionBO.getStartingStageNames().size()
                , "部分stage的启动参数不全，请检查参数");

        Class<?> taskClass = taskDefinitionBO.getTaskClass();
        Object taskBean = applicationContext.getBean(taskClass);
        TaskRuntimeEnv<?> taskRuntimeEnv = taskRuntimeService.createTaskRuntimeEnvs(taskDefinitionBO, workerStartTaskData);

        taskExecutionIdBeanMap.put(workerStartTaskData.getTaskExecutionId(), taskBean);

        for (String startingStageName : taskDefinitionBO.getStartingStageNames()) {
            executeStage(taskDefinitionBO, taskRuntimeEnv, startingStageName);
        }


        return true;
    }

    private void executeStage(TaskDefinitionBO taskDefinitionBO, TaskRuntimeEnv<?> taskRuntimeEnv, String stageName) throws NoSuchMethodException {
        StageDefinitionBO stageDefinitionBO = taskDefinitionBO.getStageDefinitionMap().get(stageName);
        Class<?> taskClass = taskDefinitionBO.getTaskClass();
        Method stageMethod = taskClass.getDeclaredMethod(stageDefinitionBO.getMethodName(),
                Arrays.stream(stageDefinitionBO.getParameters()).map(Parameter::getType).toArray(Class[]::new));

        final StageRuntimeEnv<?> stageRuntimeEnv = taskRuntimeEnv.getStageRuntimeEnvs().get(stageName);

        Long taskExecutionId = taskRuntimeEnv.getTaskExecutionId();
        this.taskExecutionIdDispatchedStageMap.computeIfAbsent(taskExecutionId, (v) -> new ConcurrentHashSet<>()).add(stageName);
        final Object taskBean = this.taskExecutionIdBeanMap.get(taskExecutionId);
        final Long stageExecutionId = stageRuntimeEnv.getStageExecutionId();
        CompletableFuture<Long> stageFuture = CompletableFuture.supplyAsync(() -> {
            try {
                // 告知scheduler某个stage已经排到要执行了
                WorkerStartStageReportReq req = new WorkerStartStageReportReq();
                req.setData(List.of(WorkerStartStageReportReq.WorkerStartToExecuteStageReqDatum.builder()
                        .taskExecutionId(taskExecutionId)
                        .stageExecutionId(stageExecutionId)
                        .build()));
                schedulerTaskProcessClient.startStageReport(clusterService.getRandomSchedulerURI(), req);
                if (stageDefinitionBO.getParameters().length == 0) {
                    stageMethod.invoke(taskBean, (Object) null);
                } else {
                    stageMethod.invoke(taskBean, stageRuntimeEnv);
                }

                return stageExecutionId;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, threadPoolExecutor);
        taskExecutionIdFuture.put(taskExecutionId, stageFuture);

        stageFuture.thenAccept((v) -> {
            // todo: stage完成

            // 通知scheduler
            URI schedulerLeaderUri = clusterService.getRandomSchedulerURI();
            WorkerCompleteStageResultReq workerCompleteStageResultReq = new WorkerCompleteStageResultReq();

            WorkerCompleteStageResultReq.StageResult stageResult = new WorkerCompleteStageResultReq.StageResult();
            stageResult.setStageExecutionId(stageExecutionId);
            stageResult.setSucceeded(true);
            stageResult.setCompletionTime(new Date());
            stageResult.setEncodedSharedContextAtCompletion(taskRuntimeEnv.getEncodedSharedContext());
            workerCompleteStageResultReq.setData(WorkerCompleteStageResultReq.Data.builder().stageResultList(List.of(stageResult)).build());

            schedulerTaskProcessClient.completeStage(schedulerLeaderUri, workerCompleteStageResultReq);
            taskExecutionIdFinishedStageMap.computeIfAbsent(taskExecutionId, (i) -> new ConcurrentHashSet<>()).add(stageName);

            // 尝试驱动下一个stage

            for (String nextStageName : taskDefinitionBO.getPointOutGraph().get(stageDefinitionBO.getName())) {

                // nextStageName的父stage全部完成之后才能驱动，需要考虑并发问题，避免同时提交两个一模一样的任务。
                //
                Set<String> fromStages = taskDefinitionBO.getPointInGraph().get(nextStageName);
                try {
                    taskExecutionLockManager.lock(taskExecutionId);
                    if (taskExecutionIdFinishedStageMap.get(taskExecutionId).containsAll(fromStages)) {
                        // nextStageName的全部父stage都已经完成
                        PrepareStageReq prepareStageReq = new PrepareStageReq();
                        PrepareStageReq.PrepareStageReqData prepareStageReqData = new PrepareStageReq.PrepareStageReqData();
                        prepareStageReqData.setStageName(nextStageName);
                        prepareStageReqData.setTaskExecutionId(taskExecutionId);
                        prepareStageReqData.setEncodedSharedContextSnapshotAtStartup(taskRuntimeEnv.getEncodedSharedContext());
                        prepareStageReq.setData(prepareStageReqData);
                        PrepareStageResp prepareStageResp = schedulerTaskProcessClient.prepareStage(schedulerLeaderUri, prepareStageReq);
                        VerifyUtil.shallBeTrue(prepareStageResp.isSuccess(), String.format("启动下一个stage:%s异常", nextStageName));

                        PrepareStageResp.PrepareStageRespData prepareStageRespData = prepareStageResp.getData();
                        Long nextStageExecutionId = prepareStageRespData.getStageExecutionId();
                        taskRuntimeService.createStageRuntimeEnv(taskRuntimeEnv, nextStageName, nextStageExecutionId);
                        executeStage(taskDefinitionBO, taskRuntimeEnv, nextStageName);
                    }

                } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
                    log.error("无法启动下一个stage", e);
                    // todo: 发生异常
                    throw new RuntimeException(e);
                } finally {
                    taskExecutionLockManager.unlockAndRemove(taskExecutionId);
                }


            }
        }).exceptionally((e) -> {
            log.error("执行任务（id: {}）出现异常", taskExecutionId, e);
            // todo: 发生异常


            return null;
        });
    }


    private void tryDriveStage(long taskExecution, String stageName) {
        try {
            taskExecutionLockManager.lock(taskExecution);


        } finally {
            taskExecutionLockManager.unlockAndRemove(taskExecution);
        }

    }

}
