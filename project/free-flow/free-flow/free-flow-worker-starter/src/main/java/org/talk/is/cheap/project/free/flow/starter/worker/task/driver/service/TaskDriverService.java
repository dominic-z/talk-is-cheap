package org.talk.is.cheap.project.free.flow.starter.worker.task.driver.service;

import cn.hutool.core.collection.ConcurrentHashSet;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.talk.is.cheap.project.free.flow.common.enums.NodeStatus;
import org.talk.is.cheap.project.free.flow.common.enums.TaskStageStatus;
import org.talk.is.cheap.project.free.flow.common.message.HttpBody;
import org.talk.is.cheap.project.free.flow.common.message.impl.scheduler.PrepareStageReq;
import org.talk.is.cheap.project.free.flow.common.message.impl.scheduler.PrepareStageResp;
import org.talk.is.cheap.project.free.flow.common.message.impl.scheduler.RescheduleTaskReq;
import org.talk.is.cheap.project.free.flow.common.message.impl.scheduler.WorkerCompleteStageResultReq;
import org.talk.is.cheap.project.free.flow.common.message.impl.scheduler.WorkerFailStageReq;
import org.talk.is.cheap.project.free.flow.common.message.impl.scheduler.WorkerFailTaskReq;
import org.talk.is.cheap.project.free.flow.common.message.impl.scheduler.WorkerStartStageReportReq;
import org.talk.is.cheap.project.free.flow.common.message.impl.worker.WorkerResumeTaskReq;
import org.talk.is.cheap.project.free.flow.common.message.impl.worker.WorkerStartTaskReq;
import org.talk.is.cheap.project.free.flow.common.task.definition.bo.StageDefinitionBO;
import org.talk.is.cheap.project.free.flow.common.task.definition.bo.TaskDefinitionBO;
import org.talk.is.cheap.project.free.flow.common.utils.FieldAwareLockManager;
import org.talk.is.cheap.project.free.flow.common.utils.VerifyUtil;
import org.talk.is.cheap.project.free.flow.starter.worker.client.SchedulerTaskProcessClient;
import org.talk.is.cheap.project.free.flow.starter.worker.cluster.service.WorkerNodeService;
import org.talk.is.cheap.project.free.flow.starter.worker.domain.dto.CreateTaskRuntimeEnvDTO;
import org.talk.is.cheap.project.free.flow.common.exception.TaskExecutionException;
import org.talk.is.cheap.project.free.flow.common.exception.TaskExecutionErrorCode;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * 如何驱动一个task的运行？分为两步：
 * 1. scheduler寻找可以运行当前任务的worker：通过hash分片找到一个worker，与这个worker进行交互询问是否可以支持分配
 * 2. 如果可以，scheduler告知这个worker启动这个task，由task实际启动执行。执行过程中将结果返回至scheduler
 * <p>
 * 原则：scheduler仅仅负责维护数据库状态以及启动任务，任务本身的状态、流转、是否完成由worker自己决定并通知scheduler
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
    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;

    private final Map<Long, Object> taskExecutionIdBeanMap = new ConcurrentHashMap<>();
    private final FieldAwareLockManager<Long> taskExecutionLockManager = new FieldAwareLockManager<>();


    @PostConstruct
    private void init() {
        int cpuNum = Runtime.getRuntime().availableProcessors();
        threadPoolExecutor = new ThreadPoolExecutor(cpuNum, cpuNum * 2, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
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
        TaskRuntimeEnv<?> taskRuntimeEnv = taskRuntimeService.createTaskRuntimeEnv(taskDefinitionBO,
                CreateTaskRuntimeEnvDTO.builder()
                        .stageEncodedInputs(workerStartTaskData.getStageEncodedInputs())
                        .initialEncodedSharedContext(workerStartTaskData.getInitialEncodedSharedContext())
                        .taskExecutionId(workerStartTaskData.getTaskExecutionId())
                        .startingStageExecutionId(workerStartTaskData.getStartingStageExecutionId())
                        .taskFailedCount(workerStartTaskData.getTaskFailedCount())
                        .stageFailedCount(new HashMap<>())
                        .succeedStageNames(new ConcurrentHashSet<>())
                        .build());

        taskExecutionIdBeanMap.put(workerStartTaskData.getTaskExecutionId(), taskBean);

        for (String startingStageName : taskDefinitionBO.getStartingStageNames()) {
            executeStage(taskDefinitionBO, taskRuntimeEnv, taskBean, startingStageName);
        }


        return true;
    }

    // stageName对应的stageExecution需要确实存在于在数据库中
    private void executeStage(TaskDefinitionBO taskDefinitionBO, TaskRuntimeEnv<?> taskRuntimeEnv, Object taskBean, String stageName) throws NoSuchMethodException, InterruptedException {
        StageDefinitionBO stageDefinitionBO = taskDefinitionBO.getStageDefinitionMap().get(stageName);
        Class<?> taskClass = taskDefinitionBO.getTaskClass();
        Method stageMethod = taskClass.getDeclaredMethod(stageDefinitionBO.getMethodName(),
                Arrays.stream(stageDefinitionBO.getParameters()).map(Parameter::getType).toArray(Class[]::new));

        final StageRuntimeEnv<?> stageRuntimeEnv = taskRuntimeEnv.getStageRuntimeEnvs().get(stageName);

        Long taskExecutionId = taskRuntimeEnv.getTaskExecutionId();
        taskRuntimeEnv.getDispatchedStages().add(stageName);
        final Long stageExecutionId = stageRuntimeEnv.getStageExecutionId();
        CompletableFuture<Long> stageFuture = CompletableFuture.supplyAsync(() -> {
            try {
                // 坑：scheduler prepareStage写入es，completeStage的时候需要查询es并更新，
                // 但es是每一秒刷一次盘，也就是说写入的数据要在1s之后才能看到，为了降低es的压力，prepare之前等待1.5s
                // https://www.qianwen.com/share/chat/df009f25897c44c7b789f2a9b7d9375b
                Thread.sleep(1500);

                // 告知scheduler某个stage已经排到要执行了
                WorkerStartStageReportReq req = new WorkerStartStageReportReq();
                req.setData(List.of(WorkerStartStageReportReq.WorkerStartToExecuteStageReqDatum.builder()
                        .taskExecutionId(taskExecutionId)
                        .stageExecutionId(stageExecutionId)
                        .build()));
                schedulerTaskProcessClient.stageStartReport(workerNodeService.getRandomSchedulerURI(), req);
                Date startTime = new Date();
                stageRuntimeEnv.setStartTime(startTime);
                if(taskRuntimeEnv.getStartTime()==null){
                    taskRuntimeEnv.setStartTime(startTime);
                }
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
            completeStageAndTryNext(taskDefinitionBO, taskRuntimeEnv, taskBean, stageName, stageExecutionId);
        }).exceptionally((e) -> {
            // 这有点问题，因为thenAccept的异常也会被抛到这里
            failStage(taskRuntimeEnv, stageName, stageExecutionId, e);
            return null;
        });
    }

    private void completeStageAndTryNext(TaskDefinitionBO taskDefinitionBO, TaskRuntimeEnv<?> taskRuntimeEnv, Object taskBean,
                                         String stageName,
                                         Long stageExecutionId) {
    /*
    想了好几遍，需要考虑当前节点的状态，如果当前节点是正常的，那么就正常执行就行了；但如果当前节点在terminating状态，那么需要确保一个已经完整完成的task不需要被重调度
    如果不全局加锁，只在后面判断是否需要要reschedule当前task的时候加锁
    那么有可能出现taskRuntimeEnv.getSucceedStages().size() == taskDefinitionBO.getStageDefinitionMap().size()但是taskRuntimeEnv
    .getRuntimeEnvStatus()!=SUCCEED的中间状态，
    并发的时候，比如另一个stage运行到判断是否要reschedule当前task的时候，因为此时一定满足
    (taskRuntimeEnv.getSucceedStages().size() + taskRuntimeEnv.getFailedStages().size()) == taskRuntimeEnv.getDispatchedStages()
    .size()
    所有已经调度的task已经完成，导致这个已经完整完成的task还是会被重新调度。
     */
        taskExecutionLockManager.lock(taskRuntimeEnv.getTaskExecutionId());
        try {
            taskRuntimeEnv.getFailedStages().remove(stageName);
            taskRuntimeEnv.getSucceedStages().add(stageName);
            StageRuntimeEnv<?> stageRuntimeEnv = taskRuntimeEnv.getStageRuntimeEnvs().get(stageName);
            Date current = new Date();

            if (taskRuntimeEnv.getSucceedStages().size() == taskDefinitionBO.getStageDefinitionMap().size()) {
                taskRuntimeEnv.setRuntimeEnvStatus(RuntimeEnvStatus.SUCCEED);
            } else if (stageRuntimeEnv.getDeadline() != null && current.compareTo(stageRuntimeEnv.getDeadline()) > 0) {
                // 如果任务整体都完成了，没所谓超不超时了
                // 说明stage超时了，当前这个stageExecution当做失败处理
                throw new TaskExecutionException(TaskExecutionErrorCode.STAGE_TIME_OUT.getCode(), String.format("stageExecutionId:%d超时",
                        stageRuntimeEnv.getStageExecutionId()));
            }
            reportCompleteStage(taskRuntimeEnv, stageExecutionId, RuntimeEnvStatus.SUCCEED.equals(taskRuntimeEnv.getRuntimeEnvStatus()));

            if (RuntimeEnvStatus.SUCCEED.equals(taskRuntimeEnv.getRuntimeEnvStatus())) {
                // 如果任务完成，清理掉任务对象，
                clearCompletedTaskObject(taskRuntimeEnv.getTaskExecutionId());
            } else if (RuntimeEnvStatus.RUNNING.equals(taskRuntimeEnv.getRuntimeEnvStatus())) {
                if (taskRuntimeEnv.getDeadline() != null && current.compareTo(taskRuntimeEnv.getDeadline()) > 0) {
                    // 说明task超时了，当前这个taskExecution当做失败处理
                    // 单独判断task是否超时，这是为了考虑到stage如果正常执行完也没有超时啥的，那stage状态应该正常成功，但仅仅将task设置为超时。
                    taskRuntimeEnv.setRuntimeEnvStatus(RuntimeEnvStatus.TIME_OUT);
                    WorkerFailTaskReq req = new WorkerFailTaskReq();
                    WorkerFailTaskReq.WorkerFailTaskReqData data = new WorkerFailTaskReq.WorkerFailTaskReqData();
                    data.setTaskExecutionId(taskRuntimeEnv.getTaskExecutionId());
                    data.setErrorCode(TaskExecutionErrorCode.TASK_TIME_OUT.getCode());
                    data.setErrorMsg(TaskExecutionErrorCode.TASK_TIME_OUT.getMsg());
                    data.setPausing(this.workerNodeService.getStatus() == NodeStatus.TERMINATING);
                    req.setData(data);
                    schedulerTaskProcessClient.failTask(workerNodeService.getRandomSchedulerURI(), req);
                } else if (this.workerNodeService.getStatus() == NodeStatus.RUNNABLE) {
                    // 尝试驱动下一个stage
                    tryNextStages(taskDefinitionBO, taskRuntimeEnv, taskBean, stageName);
                } else if (this.workerNodeService.getStatus() == NodeStatus.TERMINATING &&
                        (taskRuntimeEnv.getSucceedStages().size() + taskRuntimeEnv.getFailedStages().size()) == taskRuntimeEnv.getDispatchedStages().size()) {
                    // 这块刚好也需要加锁，举个例子，有一个任务执行完了stageA到了tryNextStages这里还没跳转进tryNextStages内
                    // 并且此时taskRuntimeEnv.getFinishedStages().size() == taskRuntimeEnv.getDispatchedStages().size()
                    // 然后节点状态突然改变为this.workerNodeService.getStatus()了
                    // 随后这个任务的另一个stageB完成了，此时进入到这个代码块中执行了重调度
                    // 这时候如果不加锁，代码会认为当前这个task的执行已经没有新的stage任务分派了，可以暂停并且通知scheduler可以将这个任务重新分派了
                    // 但是这时候stageA的线程活了，他继续往后执行了，又分派了后续的stage
                    // 这就出现错误了，一个本来已经应该暂停执行的任务没有暂停，而是继续执行了。
                    // 另外，如果task成功了，也没必要重调度了
                    rescheduleTask(taskRuntimeEnv);
                } else {
                    // 如果走到这里，说明还有没有完成的stage，等这个没完成的stage到这个代码块的时候会进行相应的上报
                    log.debug("仍有未执行完的stage，或者已经被重新调度了");
                }
            } else {
                log.warn("任务(taskExecutionId:{})已经被终止，无法继续执行后续任务", taskRuntimeEnv.getTaskExecutionId());
            }
        } finally {
            taskExecutionLockManager.unlockAndRemove(taskRuntimeEnv.getTaskExecutionId());
        }
    }

    // todo: 目前判断是否可以重试，task要不要重试，仍然是由scheduler判断的，和“worker自己判断任务执行状态”的设计有冲突，
    //  这有问题，比如一个stage的失败导致task失败了，但是其他stage的执行流不受这个失败的stage的影响的话，其他stage还会顺着执行下去
    //  所以需要task失败的时候，worker能够感知到，从而停止其他stage继续执行。因此把判断是否重试的逻辑挪到worker做好。
    //  但是我实在改不动了，就简单加个错误次数判断吧
    private void failStage(TaskRuntimeEnv<?> taskRuntimeEnv, String stageName, Long stageExecutionId, Throwable e) {
        try {
            taskExecutionLockManager.lock(taskRuntimeEnv.getTaskExecutionId()); // 需要整体加锁，和completeStageAndTryNext需要并发安全，因为现在有个进入到stw


            // 状态以及重新调度的判断，这需要stage的运行状态进入一个稳定的状态，即成功的stage已经全都完全处理完毕，失败的stage也都完全处理完毕（完成上报、状态修改等）
            log.error("执行执行阶段（taskExeId: {}, stageExeId: {}）出现异常", taskRuntimeEnv.getTaskExecutionId(), stageExecutionId, e);
            taskRuntimeEnv.getFailedStages().add(stageName);

            WorkerFailStageReq workerFailStageReq = new WorkerFailStageReq();
            WorkerFailStageReq.WorkerFailStageReqData data = new WorkerFailStageReq.WorkerFailStageReqData();
            data.setTaskExecutionId(taskRuntimeEnv.getTaskExecutionId());
            data.setStageExecutionId(stageExecutionId);
            Throwable cause = e.getCause(); // e为包装类，java.util.concurrent.CompletionException
            if (cause instanceof TaskExecutionException bizE) {
                data.setErrorCode(bizE.getErrorCode());
            }
            data.setErrorMsg(e.getMessage());
            workerFailStageReq.setData(data);

            // 但是我实在改不动了，就简单加个错误次数判断吧 简单加个判断自行修改taskRuntimeEnv的状态吧。
            StageRuntimeEnv<?> stageRuntimeEnv = taskRuntimeEnv.getStageRuntimeEnvs().get(stageName);
            TaskDefinitionBO taskDefinitionBO = localTaskDefinitionService.getTaskDefinitionBO(taskRuntimeEnv.getTaskName());
            StageDefinitionBO stageDefinitionBO = taskDefinitionBO.getStageDefinitionMap().get(stageName);
            if (stageDefinitionBO.getMaxRetryCount() <= stageRuntimeEnv.getStageFailedCount() + 1) {
                taskRuntimeEnv.setRuntimeEnvStatus(RuntimeEnvStatus.FAILED);
            }

            if (this.workerNodeService.getStatus() == NodeStatus.RUNNABLE) {
                data.setPausing(false);
                schedulerTaskProcessClient.failStage(workerNodeService.getRandomSchedulerURI(), workerFailStageReq);
            } else {
                // 用于告知scheduler当前节点在暂停中，防止stage被重试（而是等待重新调度）
                data.setPausing(true);
                schedulerTaskProcessClient.failStage(workerNodeService.getRandomSchedulerURI(), workerFailStageReq);
                // 告知scheduler可以重新调度了。即使这个task已经超过重试次数了，scheduler重调度的时候会判断这个task是否已经失败，避免一个失败的taskStartup被重新调度
                if ((taskRuntimeEnv.getSucceedStages().size() + taskRuntimeEnv.getFailedStages().size()) == taskRuntimeEnv.getDispatchedStages().size() &&
                        taskRuntimeEnv.getRuntimeEnvStatus() == RuntimeEnvStatus.RUNNING) {
                    // 既然失败了，就不可能有task整体成功的场景了，一定需要重新调度
                    rescheduleTask(taskRuntimeEnv);
                }
            }
        } finally {
            taskExecutionLockManager.unlockAndRemove(taskRuntimeEnv.getTaskExecutionId());
        }
    }

    private void rescheduleTask(TaskRuntimeEnv<?> taskRuntimeEnv) {
        if (RuntimeEnvStatus.RUNNING.equals(taskRuntimeEnv.getRuntimeEnvStatus())) {
            RescheduleTaskReq rescheduleTaskReq = new RescheduleTaskReq();
            RescheduleTaskReq.Data data = new RescheduleTaskReq.Data();
            data.setTaskExecutionId(taskRuntimeEnv.getTaskExecutionId());
            rescheduleTaskReq.setData(data);
            HttpBody<String> rescheduleResp = schedulerTaskProcessClient.rescheduleTask(rescheduleTaskReq);
            if (!rescheduleResp.isSuccess()) {
                log.error("重新调度任务（taskExeId:{}）失败", taskRuntimeEnv.getTaskExecutionId());
            }
            taskRuntimeEnv.setRuntimeEnvStatus(RuntimeEnvStatus.RESCHEDULED);
        }
        clearCompletedTaskObject(taskRuntimeEnv.getTaskExecutionId());
    }


    private void tryNextStages(TaskDefinitionBO taskDefinitionBO, TaskRuntimeEnv<?> taskRuntimeEnv, Object taskBean,
                               String completedStageName) {
        URI schedulerLeaderUri = workerNodeService.getRandomSchedulerURI();
        for (String nextStageName : taskDefinitionBO.getPointOutGraph().get(completedStageName)) {
            Set<String> fromStages = taskDefinitionBO.getPointInGraph().get(nextStageName);

            // 更新：原本外层没有加锁，只有这里加锁了，所以这里留了一个判断，现在外层代码改了加锁了，这里其实不需要加锁了，但是留着吧，记录一下双校验的应用
            if (this.workerNodeService.getStatus() == NodeStatus.RUNNABLE &&
                    !taskRuntimeEnv.getDispatchedStages().contains(nextStageName) &&
                    taskRuntimeEnv.getSucceedStages().containsAll(fromStages)) {
                // nextStageName的父stage全部完成之后才能驱动，需要考虑并发问题，避免同时提交两个一模一样的任务。
                //
                try {
                    taskExecutionLockManager.lock(taskRuntimeEnv.getTaskExecutionId());
                    // 当前节点需要仍然是runnable状态，如果是terminating状态那么就不能再创建新的任务了
                    // 注意，这块还需要加锁，避免外面判断的时候的并发问题
                    if (this.workerNodeService.getStatus() == NodeStatus.RUNNABLE &&
                            !taskRuntimeEnv.getDispatchedStages().contains(nextStageName) &&
                            taskRuntimeEnv.getSucceedStages().containsAll(fromStages)) {
                        // nextStageName的全部父stage都已经完成
                        PrepareStageReq prepareStageReq = new PrepareStageReq();
                        PrepareStageReq.PrepareStageReqData prepareStageReqData = new PrepareStageReq.PrepareStageReqData();
                        prepareStageReqData.setStageName(nextStageName);
                        prepareStageReqData.setTaskExecutionId(taskRuntimeEnv.getTaskExecutionId());
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


                        executeStage(taskDefinitionBO, taskRuntimeEnv, taskBean, nextStageName);
                    }
                } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException |
                         InterruptedException e) {
                    log.error("无法启动下一个stage", e);
                    // todo: 发生异常
                    throw new RuntimeException(e);
                } finally {
                    taskExecutionLockManager.unlockAndRemove(taskRuntimeEnv.getTaskExecutionId());
                }
            }
        }
    }

    private void reportCompleteStage(TaskRuntimeEnv<?> taskRuntimeEnv, Long succeedStageExecutionId, boolean taskSucceed) {
        URI schedulerLeaderUri = workerNodeService.getRandomSchedulerURI();
        WorkerCompleteStageResultReq workerCompleteStageResultReq = new WorkerCompleteStageResultReq();
        WorkerCompleteStageResultReq.StageResult stageResult = new WorkerCompleteStageResultReq.StageResult();
        stageResult.setStageExecutionId(succeedStageExecutionId);
        stageResult.setSucceeded(true);
        stageResult.setCompletionTime(new Date());
        stageResult.setEncodedSharedContextAtCompletion(taskRuntimeEnv.getEncodedSharedContext());
        workerCompleteStageResultReq.setData(WorkerCompleteStageResultReq.Data.builder()
                .taskExecutionId(taskRuntimeEnv.getTaskExecutionId())
                .taskSucceed(taskSucceed)
                .stageResultList(List.of(stageResult)).build());

        HttpBody<String> completeStageResp = schedulerTaskProcessClient.completeStage(schedulerLeaderUri, workerCompleteStageResultReq);
        VerifyUtil.requireTrue(completeStageResp.isSuccess(), String.format("任务(stageExeId:%d)完成状态上报失败", succeedStageExecutionId));

    }

    public void clearCompletedTaskObject(Long taskExecutionId) {
        this.taskExecutionIdBeanMap.remove(taskExecutionId);
        this.taskRuntimeService.remove(taskExecutionId);

        if (this.workerNodeService.getStatus() == NodeStatus.TERMINATING && this.taskExecutionIdBeanMap.isEmpty()) {
            // 当节点为终止中的时候并且任务对象已经是空的，那么就可以安全终止节点了。原本是分散在各个地方判断的，不好维护，统一在这里维护
            this.workerNodeService.safeToTerminate();
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void retryStage(long taskExecutionId, String stageName, long stageExecutionId, String encodedInput, Integer stageFailedCount) throws
            NoSuchMethodException, InterruptedException {

        StageRuntimeEnv retryStageRuntimeEnv = taskRuntimeService.updateRetryStageRuntimeEnv(taskExecutionId, stageExecutionId,
                stageName, encodedInput, stageFailedCount);
        TaskRuntimeEnv<?> taskRuntimeEnv = this.taskRuntimeService.get(taskExecutionId);
        TaskDefinitionBO taskDefinitionBO = localTaskDefinitionService.getTaskDefinitionBO(taskRuntimeEnv.getTaskName());
        final Object taskBean = this.taskExecutionIdBeanMap.get(taskExecutionId);
        executeStage(taskDefinitionBO, taskRuntimeEnv, taskBean, stageName);

//        TaskRuntimeEnv<?> taskRuntimeEnv = this.taskRuntimeService.get(taskExecutionId);
//        VerifyUtil.requireNotNull(taskRuntimeEnv, String.format("taskExeId:%d的任务运行环境变量已经丢失", taskExecutionId));
//        taskRuntimeEnv.getDispatchedStages().add(stageName);
//        final Object taskBean = this.taskExecutionIdBeanMap.get(taskExecutionId);
//
//        TaskDefinitionBO taskDefinitionBO = localTaskDefinitionService.getTaskDefinitionBO(taskRuntimeEnv.getTaskName());
//        StageDefinitionBO stageDefinitionBO = taskDefinitionBO.getStageDefinitionMap().get(stageName);
//        Class<?> taskClass = taskDefinitionBO.getTaskClass();
//        Method stageMethod = taskClass.getDeclaredMethod(stageDefinitionBO.getMethodName(),
//                Arrays.stream(stageDefinitionBO.getParameters()).map(Parameter::getType).toArray(Class[]::new));
//        CompletableFuture<Void> stageFuture = CompletableFuture.supplyAsync(() -> {
//                    try {
//                        // 告知scheduler某个stage已经排到要执行了
//                        WorkerStartStageReportReq req = new WorkerStartStageReportReq();
//                        req.setData(List.of(WorkerStartStageReportReq.WorkerStartToExecuteStageReqDatum.builder()
//                                .taskExecutionId(taskExecutionId)
//                                .stageExecutionId(stageExecutionId)
//                                .build()));
//                        schedulerTaskProcessClient.stageStartReport(workerNodeService.getRandomSchedulerURI(), req);
//                        if (stageDefinitionBO.getParameters().length == 0) {
//                            stageMethod.invoke(taskBean, new Object[0]);
//                        } else {
//                            stageMethod.invoke(taskBean, retryStageRuntimeEnv);
//                        }
//
//                        return stageExecutionId;
//                    } catch (Exception e) {
//                        throw new RuntimeException(e);
//                    }
//                }, threadPoolExecutor)
//                .thenAccept((v) -> {
//                    completeStageAndTryNext(taskDefinitionBO, taskRuntimeEnv, taskBean, stageName, stageExecutionId);
//                })
//                .exceptionally((e) -> {
//                    // 这有点问题，因为thenAccept的异常也会被抛到这里
//                    failStage(taskRuntimeEnv, stageName, stageExecutionId, e);
//                    return null;
//                });
//        taskRuntimeEnv.getStageNameFutures().put(stageName, stageFuture);

    }


    public void resumeTask(WorkerResumeTaskReq.Data data) throws Exception {
        String taskName = data.getTaskName();
        Integer taskVersion = data.getTaskVersion();
        VerifyUtil.requireTrue(localTaskDefinitionService.hasTask(taskName, taskVersion),
                String.format("当前节点无法运行任务（%s,%d）", taskName, taskVersion));
        TaskDefinitionBO taskDefinitionBO = localTaskDefinitionService.getTaskDefinitionBO(taskName);

        Class<?> taskClass = taskDefinitionBO.getTaskClass();
        Object taskBean = applicationContext.getBean(taskClass);
        TaskRuntimeEnv<?> taskRuntimeEnv = taskRuntimeService.createTaskRuntimeEnv(taskDefinitionBO,
                CreateTaskRuntimeEnvDTO.builder()
                        .taskExecutionId(data.getTaskExecutionId())
                        .initialEncodedSharedContext(data.getInitialEncodedSharedContext())
                        .stageEncodedInputs(data.getStageEncodedInputs())
                        .startingStageExecutionId(data.getStartingStageExecutionId())
                        .succeedStageNames(data.getSucceedStageNames())
                        .taskFailedCount(data.getTaskFailedCount())
                        .stageFailedCount(data.getStageFailedCount())
                        .build());

        taskExecutionIdBeanMap.put(data.getTaskExecutionId(), taskBean);

        for (String startingStageName : taskDefinitionBO.getStartingStageNames()) {
            executeStage(taskDefinitionBO, taskRuntimeEnv, taskBean, startingStageName);
        }
    }


    public void onNoTasksAfterDelay(int delay, TimeUnit timeUnit) {
        scheduledThreadPoolExecutor.schedule(() -> {
            if (this.taskExecutionIdBeanMap.isEmpty()) {
                workerNodeService.safeToTerminate();
            }
        }, delay, timeUnit);
    }


}
