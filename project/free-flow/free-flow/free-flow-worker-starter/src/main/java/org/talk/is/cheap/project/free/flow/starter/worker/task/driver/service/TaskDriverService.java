package org.talk.is.cheap.project.free.flow.starter.worker.task.driver.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.talk.is.cheap.project.free.flow.common.enums.NodeStatus;
import org.talk.is.cheap.project.free.flow.common.message.HttpBody;
import org.talk.is.cheap.project.free.flow.common.message.impl.scheduler.PrepareStageReq;
import org.talk.is.cheap.project.free.flow.common.message.impl.scheduler.PrepareStageResp;
import org.talk.is.cheap.project.free.flow.common.message.impl.scheduler.WorkerCompleteStageResultReq;
import org.talk.is.cheap.project.free.flow.common.message.impl.scheduler.WorkerFailStageReq;
import org.talk.is.cheap.project.free.flow.common.message.impl.scheduler.WorkerStartStageReportReq;
import org.talk.is.cheap.project.free.flow.common.message.impl.worker.WorkerStartTaskReq;
import org.talk.is.cheap.project.free.flow.common.task.definition.bo.StageDefinitionBO;
import org.talk.is.cheap.project.free.flow.common.task.definition.bo.TaskDefinitionBO;
import org.talk.is.cheap.project.free.flow.common.utils.FieldAwareLockManager;
import org.talk.is.cheap.project.free.flow.common.utils.VerifyUtil;
import org.talk.is.cheap.project.free.flow.starter.worker.client.SchedulerTaskProcessClient;
import org.talk.is.cheap.project.free.flow.starter.worker.cluster.service.WorkerNodeService;
import org.talk.is.cheap.project.free.flow.starter.worker.task.definition.service.LocalTaskDefinitionService;
import org.talk.is.cheap.project.free.flow.starter.worker.task.driver.runtime.RuntimeEnvStatus;
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
    private WorkerNodeService workerNodeService;

    @Autowired
    private SchedulerTaskProcessClient schedulerTaskProcessClient;

    private ThreadPoolExecutor threadPoolExecutor;

    private final Map<Long, Object> taskExecutionIdBeanMap = new ConcurrentHashMap<>();
    private final FieldAwareLockManager<Long> taskExecutionLockManager = new FieldAwareLockManager<>();


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
     * @return
     */
    public boolean startTask(WorkerStartTaskReq.Data workerStartTaskData) throws Exception {
        VerifyUtil.requireTrue(canStartTask(workerStartTaskData.getTaskName(), workerStartTaskData.getTaskVersion()),
                String.format("worker can't ran run task: %s, version: %d", workerStartTaskData.getTaskName(),
                        workerStartTaskData.getTaskVersion()));

        TaskDefinitionBO taskDefinitionBO = localTaskDefinitionService.getTaskDefinitionBO(workerStartTaskData.getTaskName());

        Map<String, Long> startingStageExecutionId = workerStartTaskData.getStartingStageExecutionId();
        VerifyUtil.requireTrue(taskDefinitionBO.getStartingStageNames().containsAll(startingStageExecutionId.keySet())
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
        taskRuntimeEnv.getDispatchedStages().add(stageName);
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
                schedulerTaskProcessClient.stageStartReport(workerNodeService.getRandomSchedulerURI(), req);
                if (stageDefinitionBO.getParameters().length == 0) {
                    stageMethod.invoke(taskBean, new Object[0]);
                } else {
//                    不用校验了，加载任务的时候已经校验过了
                    stageMethod.invoke(taskBean, stageRuntimeEnv);
                }

                return stageExecutionId;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, threadPoolExecutor);
        taskRuntimeEnv.getStageNameFutures().put(stageName, stageFuture);

        stageFuture.thenAccept((v) -> {
            // 通知scheduler
            reportCompleteStage(stageExecutionId, taskRuntimeEnv.getEncodedSharedContext());
            taskRuntimeEnv.getFailedStages().remove(stageName);
            taskRuntimeEnv.getFinishedStages().add(stageName);
            // 尝试驱动下一个stage
            if (this.workerNodeService.getStatus() == NodeStatus.RUNNABLE) {
                tryNextStages(taskDefinitionBO, taskRuntimeEnv, taskExecutionId, stageDefinitionBO.getName());
            } else if ((taskRuntimeEnv.getFinishedStages().size() + taskRuntimeEnv.getFinishedStages().size()) == taskRuntimeEnv.getDispatchedStages().size() &&
                    taskRuntimeEnv.getRuntimeEnvStatus() != RuntimeEnvStatus.RESCHEDULED) {
                // 这块需要加锁，并且需要双重判断，举个例子，有一个任务执行完了stageA到了tryNextStages这里还没跳转进tryNextStages内
                // 并且此时taskRuntimeEnv.getFinishedStages().size() == taskRuntimeEnv.getDispatchedStages().size()
                // 然后节点状态突然改变为this.workerNodeService.getStatus()了
                // 随后这个任务的另一个stageB完成了，此时进入到这个代码块中
                // 这时候如果不加锁，代码会认为当前这个task的执行已经没有新的stage任务分派了，可以暂停并且通知scheduler可以将这个任务重新分派了
                // 但是这时候stageA的线程活了，他继续往后执行了，又分派了后续的stage
                // 这就出现错误了，一个本来已经应该暂停执行的任务没有暂停，而是继续执行了。
                // 因此，这块需要加锁，并且后续tryNextStages内也要加锁并且二次判断this.workerNodeService.getStatus()的状态，同理这里也要双重判断
                // 就是单例模式的双重判断
                taskExecutionLockManager.lock(taskExecutionId);
                try {
                    // 如果这个判断是false，说明还有没完成的stage，等这个stage
                    if ((taskRuntimeEnv.getFinishedStages().size() + taskRuntimeEnv.getFinishedStages().size()) == taskRuntimeEnv.getDispatchedStages().size() &&
                            taskRuntimeEnv.getRuntimeEnvStatus() != RuntimeEnvStatus.RESCHEDULED) {
                        // 当前task已经暂停了，没有任何还在执行或者将要执行的stage了，可以reschedule了
                    }
                } finally {
                    taskExecutionLockManager.unlockAndRemove(taskExecutionId);
                }
            }
        }).exceptionally((e) -> {
            // 这有点问题，因为thenAccept的异常也会被抛到这里
            log.error("执行执行阶段（taskExeId: {}, stageExeId: {}）出现异常", taskExecutionId, stageExecutionId, e);
            WorkerFailStageReq workerFailStageReq = new WorkerFailStageReq();
            WorkerFailStageReq.WorkerFailStageReqData data = new WorkerFailStageReq.WorkerFailStageReqData();
            data.setTaskExecutionId(taskExecutionId);
            data.setStageExecutionId(stageExecutionId);
            data.setErrorMsg(e.getMessage());
            workerFailStageReq.setData(data);
            schedulerTaskProcessClient.failStage(workerNodeService.getRandomSchedulerURI(), workerFailStageReq);
            taskRuntimeEnv.getFailedStages().add(stageName);
            if ((taskRuntimeEnv.getFinishedStages().size() + taskRuntimeEnv.getFinishedStages().size()) == taskRuntimeEnv.getDispatchedStages().size() &&
                    taskRuntimeEnv.getRuntimeEnvStatus() != RuntimeEnvStatus.RESCHEDULED) {
                taskExecutionLockManager.lock(taskExecutionId);
                try {
                    // 如果这个判断是false，说明还有没完成的stage，等这个stage
                    if ((taskRuntimeEnv.getFinishedStages().size() + taskRuntimeEnv.getFinishedStages().size()) == taskRuntimeEnv.getDispatchedStages().size() &&
                            taskRuntimeEnv.getRuntimeEnvStatus() != RuntimeEnvStatus.RESCHEDULED) {
                        // 当前task已经暂停了，可以reschedule了
                    }
                } finally {
                    taskExecutionLockManager.unlockAndRemove(taskExecutionId);
                }
            }
            return null;
        });
    }


    // todo：当任务完成或者失败并且不能再重试的时候需要清空taskExecutionId相关的对象
    public void clearCompletedTaskObject(Long taskExecutionId) {
        this.taskExecutionIdBeanMap.remove(taskExecutionId);
        this.taskRuntimeService.remove(taskExecutionId);
    }

    private void reportCompleteStage(Long stageExecutionId, String encodedSharedContext) {
        URI schedulerLeaderUri = workerNodeService.getRandomSchedulerURI();
        WorkerCompleteStageResultReq workerCompleteStageResultReq = new WorkerCompleteStageResultReq();
        WorkerCompleteStageResultReq.StageResult stageResult = new WorkerCompleteStageResultReq.StageResult();
        stageResult.setStageExecutionId(stageExecutionId);
        stageResult.setSucceeded(true);
        stageResult.setCompletionTime(new Date());
        stageResult.setEncodedSharedContextAtCompletion(encodedSharedContext);
        workerCompleteStageResultReq.setData(WorkerCompleteStageResultReq.Data.builder().stageResultList(List.of(stageResult)).build());

        HttpBody<String> completeStageResp = schedulerTaskProcessClient.completeStage(schedulerLeaderUri, workerCompleteStageResultReq);
        VerifyUtil.requireTrue(completeStageResp.isSuccess(), String.format("任务(stageExeId:%d)完成状态上报失败", stageExecutionId));

    }

    private void tryNextStages(TaskDefinitionBO taskDefinitionBO, TaskRuntimeEnv<?> taskRuntimeEnv, Long taskExecutionId,
                               String completedStageName) {
        URI schedulerLeaderUri = workerNodeService.getRandomSchedulerURI();
        for (String nextStageName : taskDefinitionBO.getPointOutGraph().get(completedStageName)) {
            Set<String> fromStages = taskDefinitionBO.getPointInGraph().get(nextStageName);

            if (!taskRuntimeEnv.getDispatchedStages().contains(nextStageName) &&
                    taskRuntimeEnv.getFinishedStages().containsAll(fromStages)) {
                // nextStageName的父stage全部完成之后才能驱动，需要考虑并发问题，避免同时提交两个一模一样的任务。
                //
                try {
                    taskExecutionLockManager.lock(taskExecutionId);
                    // 当前节点需要仍然是runnable状态，如果是terminating状态那么就不能再创建新的任务了
                    // 注意，这块还需要加锁，避免外面判断的时候的并发问题
                    if (this.workerNodeService.getStatus() == NodeStatus.RUNNABLE &&
                            !taskRuntimeEnv.getDispatchedStages().contains(nextStageName) &&
                            taskRuntimeEnv.getFinishedStages().containsAll(fromStages)) {
                        // nextStageName的全部父stage都已经完成
                        PrepareStageReq prepareStageReq = new PrepareStageReq();
                        PrepareStageReq.PrepareStageReqData prepareStageReqData = new PrepareStageReq.PrepareStageReqData();
                        prepareStageReqData.setStageName(nextStageName);
                        prepareStageReqData.setTaskExecutionId(taskExecutionId);
                        prepareStageReqData.setEncodedSharedContextSnapshotAtStartup(taskRuntimeEnv.getEncodedSharedContext());
                        prepareStageReqData.setPrepareTime(new Date());
                        prepareStageReq.setData(prepareStageReqData);
                        PrepareStageResp prepareStageResp = schedulerTaskProcessClient.prepareStage(schedulerLeaderUri,
                                prepareStageReq);
                        VerifyUtil.requireTrue(prepareStageResp.isSuccess(), String.format("启动下一个stage:%s异常，异常信息：%s", nextStageName,
                                prepareStageResp.getMsg()));

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
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void retryStage(long taskExecutionId, String stageName, long stageExecutionId, String encodedInput) throws
            NoSuchMethodException {

        StageRuntimeEnv retryStageRuntimeEnv = taskRuntimeService.updateRetryStageRuntimeEnv(taskExecutionId, stageExecutionId,
                stageName
                , encodedInput);

        TaskRuntimeEnv<?> taskRuntimeEnv = this.taskRuntimeService.get(taskExecutionId);
        VerifyUtil.requireNotNull(taskRuntimeEnv, String.format("taskExeId:%d的任务运行环境变量已经丢失", taskExecutionId));
        taskRuntimeEnv.getDispatchedStages().add(stageName);
        final Object taskBean = this.taskExecutionIdBeanMap.get(taskExecutionId);

        TaskDefinitionBO taskDefinitionBO = localTaskDefinitionService.getTaskDefinitionBO(taskRuntimeEnv.getTaskName());
        StageDefinitionBO stageDefinitionBO = taskDefinitionBO.getStageDefinitionMap().get(stageName);
        Class<?> taskClass = taskDefinitionBO.getTaskClass();
        Method stageMethod = taskClass.getDeclaredMethod(stageDefinitionBO.getMethodName(),
                Arrays.stream(stageDefinitionBO.getParameters()).map(Parameter::getType).toArray(Class[]::new));
        CompletableFuture<Void> stageFuture = CompletableFuture.supplyAsync(() -> {
                    try {
                        // 告知scheduler某个stage已经排到要执行了
                        WorkerStartStageReportReq req = new WorkerStartStageReportReq();
                        req.setData(List.of(WorkerStartStageReportReq.WorkerStartToExecuteStageReqDatum.builder()
                                .taskExecutionId(taskExecutionId)
                                .stageExecutionId(stageExecutionId)
                                .build()));
                        schedulerTaskProcessClient.stageStartReport(workerNodeService.getRandomSchedulerURI(), req);
                        if (stageDefinitionBO.getParameters().length == 0) {
                            stageMethod.invoke(taskBean, new Object[0]);
                        } else {
                            stageMethod.invoke(taskBean, retryStageRuntimeEnv);
                        }

                        return stageExecutionId;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }, threadPoolExecutor)
                .thenAccept((v) -> {
                    reportCompleteStage(stageExecutionId, taskRuntimeEnv.getEncodedSharedContext());
                    taskRuntimeEnv.getFinishedStages().add(stageName);
                    // 尝试驱动下一个stage
                    tryNextStages(taskDefinitionBO, taskRuntimeEnv, taskExecutionId, stageDefinitionBO.getName());
                })
                .exceptionally((e) -> {
                    // 这有点问题，因为thenAccept的异常也会被抛到这里
                    log.error("重试执行阶段（taskExeId: {}, stageExeId: {}）出现异常", taskExecutionId, stageExecutionId, e);
                    WorkerFailStageReq workerFailStageReq = new WorkerFailStageReq();
                    WorkerFailStageReq.WorkerFailStageReqData data = new WorkerFailStageReq.WorkerFailStageReqData();
                    data.setTaskExecutionId(taskExecutionId);
                    data.setStageExecutionId(stageExecutionId);
                    data.setErrorMsg(e.getMessage());
                    workerFailStageReq.setData(data);
                    schedulerTaskProcessClient.failStage(workerNodeService.getRandomSchedulerURI(), workerFailStageReq);
                    return null;
                });
        taskRuntimeEnv.getStageNameFutures().put(stageName, stageFuture);

    }


}
