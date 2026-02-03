package org.talk.is.cheap.project.free.flow.scheduler.task.service;


import io.vavr.Tuple2;
import io.vavr.Tuple3;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.talk.is.cheap.project.free.flow.common.enums.StartupSourceType;
import org.talk.is.cheap.project.free.flow.common.enums.TaskStageStatus;
import org.talk.is.cheap.project.free.flow.common.message.HttpBody;
import org.talk.is.cheap.project.free.flow.common.message.impl.dto.TaskDefinitionDTO;
import org.talk.is.cheap.project.free.flow.common.message.impl.scheduler.WorkerCompleteStageResultReq;
import org.talk.is.cheap.project.free.flow.common.message.impl.scheduler.WorkerStartStageReportReq;
import org.talk.is.cheap.project.free.flow.common.message.impl.worker.WorkerRetryStageReq;
import org.talk.is.cheap.project.free.flow.common.message.impl.worker.WorkerStartTaskReq;
import org.talk.is.cheap.project.free.flow.common.message.impl.worker.WorkerStartTaskResp;
import org.talk.is.cheap.project.free.flow.common.utils.VerifyUtil;
import org.talk.is.cheap.project.free.flow.scheduler.cluster.service.WorkerClusterManager;
import org.talk.is.cheap.project.free.flow.scheduler.task.client.WorkerTaskDriverClient;
import org.talk.is.cheap.project.free.flow.starter.repository.config.RedisAutoConfig;
import org.talk.is.cheap.project.free.flow.starter.repository.config.RepositoryAutoConfig;
import org.talk.is.cheap.project.free.flow.starter.repository.dao.mbg.query.example.StageStartupExample;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.es.pojo.ESPojoDTO;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.es.pojo.StageExecutionResultMsg;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.es.pojo.StageStartupParam;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.es.pojo.TaskSharedContext;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.StageDefinition;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.StageExecution;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.StageStartup;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.TaskDefinition;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.TaskExecution;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.TaskStartup;
import org.talk.is.cheap.project.free.flow.starter.repository.service.StageDefinitionService;
import org.talk.is.cheap.project.free.flow.starter.repository.service.StageExecutionService;
import org.talk.is.cheap.project.free.flow.starter.repository.service.StageStartupService;
import org.talk.is.cheap.project.free.flow.starter.repository.service.TaskExecutionService;
import org.talk.is.cheap.project.free.flow.starter.repository.service.TaskGraphDefinitionService;
import org.talk.is.cheap.project.free.flow.starter.repository.service.TaskStartupService;
import org.talk.is.cheap.project.free.flow.starter.repository.service.derived.StageDefinitionServiceWrapper;
import org.talk.is.cheap.project.free.flow.starter.repository.service.derived.StageExecutionServiceWrapper;
import org.talk.is.cheap.project.free.flow.starter.repository.service.derived.StageStartupServiceWrapper;
import org.talk.is.cheap.project.free.flow.starter.repository.service.derived.TaskDefinitionServiceWrapper;
import org.talk.is.cheap.project.free.flow.starter.repository.service.derived.TaskExecutionServiceWrapper;
import org.talk.is.cheap.project.free.flow.starter.repository.service.derived.TaskStartupServiceWrapper;
import org.talk.is.cheap.project.free.flow.starter.repository.service.es.StageExecutionResultMsgService;
import org.talk.is.cheap.project.free.flow.starter.repository.service.es.StageStartupParamService;
import org.talk.is.cheap.project.free.flow.starter.repository.service.es.TaskSharedContextService;
import org.talk.is.cheap.project.free.flow.starter.repository.service.redis.RedissonService;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * 提示：
 * 1. 注意并发，如果可能操作task级别的对象，需要并发加锁避免一些stage并行执行某些stage失败后更新task对象导致数据不一致，但如果只是更新某个stage，不用更关心并发问题，
 * 因为一个stage一般只会有一个scheduler处理，更新时，为避免死锁，先更新execution，再更新startup
 */
@Service
@Slf4j
public class WorkerTaskDriverService {

    @Service
    public static class WorkerTaskDriverServiceTxnHelper {

        @Autowired
        private StageStartupServiceWrapper stageStartupServiceWrapper;
        @Autowired
        private StageExecutionServiceWrapper stageExecutionServiceWrapper;
        @Autowired
        private TaskStartupServiceWrapper taskStartupServiceWrapper;
        @Autowired
        private TaskExecutionServiceWrapper taskExecutionServiceWrapper;

        @Transactional(rollbackFor = Exception.class, transactionManager = RepositoryAutoConfig.TRANSACTION_MANAGER_BEAN_NAME)
        public void forceFailTask(Long taskExecutionId, Long taskStartupId) {
            if (taskStartupId != null) {
                taskStartupServiceWrapper.updateByIdSelective(taskStartupId,
                        new TaskStartup().withStatus(TaskStageStatus.FAILED.getStatus()), null);
            }
            if (taskExecutionId != null) {
                taskExecutionServiceWrapper.updateSelectiveById(taskExecutionId,
                        new TaskExecution().withStatus(TaskStageStatus.FAILED.getStatus()), null);
            }
        }

        @Transactional(rollbackFor = Exception.class, transactionManager = RepositoryAutoConfig.TRANSACTION_MANAGER_BEAN_NAME)
        public void forceFailStageAndTask(Long stageExecutionId, Long stageStartupId, Long taskExecutionId, Long taskStartupId) {
            if (stageExecutionId != null) {
                stageExecutionServiceWrapper.updateSelectiveById(stageExecutionId,
                        new StageExecution().withStatus(TaskStageStatus.FAILED.getStatus()), null);
            }
            if (stageStartupId != null) {
                stageStartupServiceWrapper.updateSelectiveById(stageStartupId,
                        new StageStartup().withStatus(TaskStageStatus.FAILED.getStatus()), null);
            }
            forceFailTask(taskExecutionId, taskStartupId);
        }

    }


    /**
     * 数据库操作service
     */
    @Autowired
    private StageStartupServiceWrapper stageStartupServiceWrapper;
    @Autowired
    private StageStartupService stageStartupService;
    @Autowired
    private StageExecutionService stageExecutionService;
    @Autowired
    private StageExecutionServiceWrapper stageExecutionServiceWrapper;
    @Autowired
    private StageDefinitionServiceWrapper stageDefinitionServiceWrapper;
    @Autowired
    private StageDefinitionService stageDefinitionService;
    @Autowired
    private TaskGraphDefinitionService taskGraphDefinitionService;
    @Autowired
    private TaskDefinitionServiceWrapper taskDefinitionServiceWrapper;
    @Autowired
    private TaskStartupService taskStartupService;
    @Autowired
    private TaskStartupServiceWrapper taskStartupServiceWrapper;
    @Autowired
    private TaskExecutionServiceWrapper taskExecutionServiceWrapper;
    @Autowired
    private TaskExecutionService taskExecutionService;

    @Autowired
    private WorkerTaskDriverServiceTxnHelper workerTaskDriverServiceTxnHelper;

    // es
    @Autowired
    private StageStartupParamService stageStartupParamService;

    @Autowired
    private TaskSharedContextService taskSharedContextService;

    @Autowired
    private StageExecutionResultMsgService stageExecutionResultMsgService;

    @Autowired
    private WorkerTaskDefinitionManager workerTaskDefinitionManager;

    /**
     * worker客户端
     */
    @Autowired
    private WorkerTaskDriverClient workerTaskDriverClient;

    /**
     * 任务与集群管理service
     */
    @Autowired
    private WorkerClusterManager workerClusterManager;
    @Autowired
    private TaskScheduler taskScheduler;


    @Autowired
    @Qualifier(RedisAutoConfig.REDISSON_CLIENT)
    private RedissonClient redissonClient;

    @Autowired
    private RedissonService redissonService;

    private final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(8, 16, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(32));

    /**
     * 在启动任务之前校验task，并且在数据库中创建任务数据，包括创建各种startup+execution数据，以便可以直接执行任务
     * 不包括retry的情况，retry的执行不需要创建startup
     * 引导worker设置初始化的共享上下文
     *
     * @param taskName
     * @param taskVersion 返回的是创建的执行任务的地址，taskExecutionId和stageExecutionIds
     */
//    @Transactional(rollbackFor = Exception.class, transactionManager = RepositoryAutoConfig.TRANSACTION_MANAGER_BEAN_NAME)
    public Tuple3<String, Long, Map<String, Long>> prepareForTask(String taskName, Integer taskVersion,
                                                                  String initialEncodedSharedContext,
                                                                  Map<String, String> stageEncodedInputs) {
        TaskDefinition taskDefinition = taskDefinitionServiceWrapper.queryByNameVersion(taskName, taskVersion);
        VerifyUtil.requireNotNull(taskDefinition, "未找到对应的任务定义:%s,%d".formatted(taskName, taskVersion));

        // 创建task的startup和execution
        TaskStartup taskStartup = new TaskStartup()
                .withTaskId(taskDefinition.getId())
                .withSourceType(StartupSourceType.EXTERNAL.getValue())
                .withStatus(TaskStageStatus.PENDING.getStatus());
        VerifyUtil.requireTrue(taskStartupService.create(taskStartup) > 0, "创建task启动记录失败");

        try {
            taskSharedContextService.create(TaskSharedContext.builder()
                    .taskStartupId(taskStartup.getId())
                    .encodedTaskSharedContext(initialEncodedSharedContext)
                    .updateTime(new Date()).build()
            );

            String workerAddress = taskScheduler.assignTaskToWorkerAddress(taskName, taskDefinition.getVersion());
            VerifyUtil.requireNotBlank(workerAddress, "No node capable of executing this task can be found.");

            TaskExecution taskExecution = new TaskExecution()
                    .withStatus(TaskStageStatus.PENDING.getStatus())
                    .withAssignedWorkerAddr(workerAddress)
                    .withTaskStartupId(taskStartup.getId());
            VerifyUtil.requireTrue(taskExecutionService.create(taskExecution) > 0, "创建task执行记录失败");

            // 创建stage的startup和execution
            List<StageDefinition> stageDefinitions = stageDefinitionServiceWrapper.selectByTaskId(taskDefinition.getId());
            Map<String, Long> rootStageName2ExecutionId = new HashMap<>();
            for (StageDefinition stageDefinition : stageDefinitions) {
                String stageName = stageDefinition.getName();


                StageStartup stageStartup = new StageStartup()
                        .withTaskExecutionId(taskExecution.getId())
                        .withStatus(TaskStageStatus.PENDING.getStatus())
                        .withStageId(stageDefinition.getId());

                VerifyUtil.requireTrue(stageStartupService.create(stageStartup) > 0,
                        String.format("Failed to create stage startup: %s", stageName));
                if (stageDefinition.getIsStartingStage()) {
                    // 正常情况下是在stage启动之前，由scheduler创建execution对象，但是在start task的时候，顺手创建一部分stage的execution
                    // 仅仅针对starting 的stage创建execution对象，这样可以在启动任务与worker交互的时候节省一次请求。
                    StageExecution stageExecution = new StageExecution()
                            .withStageStartupId(stageStartup.getId())
                            .withStatus(TaskStageStatus.PENDING.getStatus())
                            .withWorkerAddress(workerAddress);
                    VerifyUtil.requireTrue(stageExecutionService.create(stageExecution) > 0, String.format("Failed to create stage " +
                            "execution:" +
                            " %s", stageName));
                    rootStageName2ExecutionId.put(stageName, stageExecution.getId());

                }

                if (stageEncodedInputs.containsKey(stageName)) {
                    String encodedInput = stageEncodedInputs.get(stageName);
                    stageStartupParamService.create(
                            StageStartupParam.builder().stageStartupId(stageStartup.getId())
                                    .encodedInput(encodedInput)
                                    .encodedSharedContextSnapshotAtStartup(initialEncodedSharedContext)
                                    .updateTime(new Date())
                                    .build()
                    );
                }
            }


            return new Tuple3<>(workerAddress, taskExecution.getId(), rootStageName2ExecutionId);
        } catch (Exception e) {
            log.error("error when prepare task", e);
            try {
                taskStartup.setStatus(TaskStageStatus.FAILED.getStatus());
                taskStartupServiceWrapper.updateByIdSelective(taskStartup.getId(), taskStartup, null);
            } catch (Exception ie) {
                log.error("更新task状态为失败错误", ie);
            }
            throw new RuntimeException(e);
        }


    }

    /**
     * 某个stage启动时候，worker会上报，然后在这个方法里处理，用于更新task与status的状态，但是要考虑并发情况，并发冲突会发生在task的级别，stage的更新不会并发
     * 如果task目前因为并发而已经处于成功状态，那么各种update不成功也没问题。
     * 如果task目前因为并发而已经处于失败状态，那么各种update必须失败，此时必须并发安全。
     * <p>
     * 因为不要求update一定成功，而且mysql 的update操作会加行锁，而且还会通过revision控制，所以也没必要加锁了。
     * <p>
     * <p>
     * 更新，还是要加锁，因为failStage中修改task的操作必须成功，如果这边不加锁，failStage就必须得循环cas，如果任务一多，这对数据库压力过大
     *
     * @param startToExecuteStageReqData
     */
    @Transactional(rollbackFor = Exception.class, transactionManager = RepositoryAutoConfig.TRANSACTION_MANAGER_BEAN_NAME)
    public void startStageReport(List<WorkerStartStageReportReq.WorkerStartToExecuteStageReqDatum> startToExecuteStageReqData) {
        for (WorkerStartStageReportReq.WorkerStartToExecuteStageReqDatum datum : startToExecuteStageReqData) {
            Long taskExecutionId = datum.getTaskExecutionId();

            TaskExecution taskExecution = taskExecutionServiceWrapper.selectById(taskExecutionId, TaskStageStatus.PENDING.getStatus());
            if (taskExecution != null) {
//                RLock lock = redissonClient.getLock(RedissonService.getTaskExecutionLockKey(taskExecutionId));
                try {
//                    lock.lock(2, TimeUnit.SECONDS);
                    taskExecutionServiceWrapper.updateSelectiveById(taskExecutionId,
                            new TaskExecution().withStatus(TaskStageStatus.RUNNING.getStatus()), taskExecution.getRevision());

                    TaskStartup taskStartup = taskStartupServiceWrapper.selectById(taskExecution.getTaskStartupId(),
                            TaskStageStatus.PENDING.getStatus());
                    if (taskStartup != null) {
                        taskStartupServiceWrapper.updateByIdSelective(taskStartup.getId(),
                                new TaskStartup().withStatus(TaskStageStatus.RUNNING.getStatus()), taskStartup.getRevision());
                    }
                } finally {
//                    lock.unlock();
                }
            }


            Long stageExecutionId = datum.getStageExecutionId();
            Date startTime = datum.getStartTime();
            StageExecution stageExecution = stageExecutionServiceWrapper.selectById(stageExecutionId);

            stageExecutionServiceWrapper.updateSelectiveById(stageExecutionId,
                    new StageExecution().withStatus(TaskStageStatus.RUNNING.getStatus()).withStartTime(startTime),
                    stageExecution.getRevision());

            StageStartup stageStartup = stageStartupServiceWrapper.selectById(stageExecution.getStageStartupId(),
                    TaskStageStatus.PENDING.getStatus());
            if (stageStartup != null) {
                stageStartupServiceWrapper.updateSelectiveById(stageStartup.getId(),
                        new StageStartup().withStatus(TaskStageStatus.RUNNING.getStatus()), stageStartup.getRevision());
            }

        }
    }


    /**
     * worker要运行某个stage，scheduler做好准备，即创建对应的stageExecution并且返回input给worker
     *
     * @param taskExecutionId
     * @param stageName
     * @param encodedSharedContextSnapshotAtStartup
     * @return
     * @throws IOException
     */
    @Transactional(rollbackFor = Exception.class, transactionManager = RepositoryAutoConfig.TRANSACTION_MANAGER_BEAN_NAME)
    public Tuple2<Long, String> prepareForStage(long taskExecutionId, String stageName,
                                                String encodedSharedContextSnapshotAtStartup) throws IOException {
        TaskExecution taskExecution = taskExecutionServiceWrapper.selectById(taskExecutionId);
        VerifyUtil.requireNotNull(taskExecution, String.format("无法找到id为%d的taskExecution对象", taskExecutionId));

        TaskStartup taskStartup = taskStartupServiceWrapper.selectById(taskExecution.getTaskStartupId());
        VerifyUtil.requireTrue(!Objects.equals(taskStartup.getStatus(), TaskStageStatus.FAILED.getStatus()),
                String.format("任务%d已经失败", taskExecutionId));

        StageDefinition stageDefinition = stageDefinitionServiceWrapper.selectByTaskIdStageName(taskStartup.getTaskId(), stageName);
        VerifyUtil.requireNotNull(stageDefinition,
                String.format("taskId为%d的任务定义中无法找到唯一的一个name为%s的stage", taskStartup.getTaskId(), stageName));


        StageStartupExample stageStartupExample = new StageStartupExample();
        stageStartupExample.createCriteria()
                .andTaskExecutionIdEqualTo(taskExecutionId)
                .andStageIdEqualTo(stageDefinition.getId());
        List<StageStartup> stageStartups = stageStartupService.selectByExample(stageStartupExample);
        VerifyUtil.requireTrue(stageStartups.size() == 1,
                String.format("无法找到stage startup对象（taskExecutionId: %d, stageId: %d, status: %d）", taskExecutionId,
                        stageDefinition.getId(), TaskStageStatus.PENDING.getStatus())
        );
        StageStartup stageStartup = stageStartups.get(0);
        VerifyUtil.requireTrue(!Objects.equals(stageStartup.getStatus(), TaskStageStatus.FAILED.getStatus()),
                String.format("任务%d已经失败", taskExecutionId));

        ESPojoDTO<StageStartupParam> stageStartupParamESPojoDTO = stageStartupParamService.getByStageStartupId(stageStartup.getId());
        StageStartupParam stageStartupParam = null;
        if (stageStartupParamESPojoDTO == null) {
            stageStartupParam = StageStartupParam.builder()
                    .stageStartupId(stageStartup.getId())
                    .encodedInput("")
                    .encodedSharedContextSnapshotAtStartup(encodedSharedContextSnapshotAtStartup)
                    .updateTime(new Date())
                    .build();
            stageStartupParamService.create(stageStartupParam);
        } else {
            stageStartupParam = stageStartupParamESPojoDTO.getData();
            if (StringUtils.isNotBlank(encodedSharedContextSnapshotAtStartup)) {
                stageStartupParam.setEncodedSharedContextSnapshotAtStartup(encodedSharedContextSnapshotAtStartup);
                stageStartupParamService.update(stageStartupParamESPojoDTO.getId(), stageStartupParam);
            }
        }

        List<StageExecution> stageExecutions = stageExecutionServiceWrapper.selectByStartupId(stageStartup.getId(),
                TaskStageStatus.PENDING.getStatus());
        VerifyUtil.requireTrue(stageExecutions.isEmpty() || stageExecutions.size() == 1,
                String.format("找到多个StageExecution对象（startUpId: %d, status: %d）", stageStartup.getId(),
                        TaskStageStatus.PENDING.getStatus()));
        StageExecution stageExecution;
        if (stageExecutions.isEmpty()) {
            // 针对非starting stage，可能需要手挡创建
            stageExecution = new StageExecution()
                    .withStatus(TaskStageStatus.PENDING.getStatus())
                    .withStageStartupId(stageStartup.getId())
                    .withWorkerAddress(taskExecution.getAssignedWorkerAddr());
            stageExecutionService.create(stageExecution);
        } else {
            stageExecution = stageExecutions.get(0);
        }
        return new Tuple2<>(stageExecution.getId(), stageStartupParam.getEncodedInput());

    }


    /**
     * 某个stage已经完成，记录shareContext快照，正常情况下不需要考虑并发，因为一般情况下一个stage只会有一个scheduler在操作
     * 仅仅updatestage信息，task是否完成会由worker再次主动上报，不需要事务
     */
    public void completeStage(WorkerCompleteStageResultReq.StageResult stageResult) throws IOException {
        Long stageExecutionId = stageResult.getStageExecutionId();
        StageExecution stageExecution = stageExecutionServiceWrapper.selectById(stageExecutionId, TaskStageStatus.RUNNING.getStatus());
        VerifyUtil.requireTrue(stageExecution != null,
                "The running execution record for the stage with ID %d does not exist.".formatted(stageExecutionId));

        // 更新stage startup 和stage execution
        StageStartup stageStartup = stageStartupServiceWrapper.selectById(stageExecution.getStageStartupId(),
                TaskStageStatus.RUNNING.getStatus());
        VerifyUtil.requireTrue(stageStartup != null,
                "The startup record for the stage with ID %d does not exist.".formatted(stageExecution.getStageStartupId()));
        Integer taskStageStatus = stageResult.getSucceeded() ? TaskStageStatus.SUCCEEDED.getStatus() :
                TaskStageStatus.FAILED.getStatus();

        VerifyUtil.requireTrue(stageExecutionServiceWrapper.updateSelectiveById(
                        stageExecutionId, new StageExecution().withStatus(taskStageStatus), stageExecution.getRevision()) > 0,
                String.format("更新stageExecution:%d成功状态失败", stageExecutionId));
        VerifyUtil.requireTrue(stageStartupServiceWrapper.updateSelectiveById(stageStartup.getId(),
                        new StageStartup().withStatus(taskStageStatus).withCompletionTime(stageResult.getCompletionTime()),
                        stageStartup.getRevision()) > 0,
                String.format("更新stageStartup:%d成功状态失败", stageStartup.getId()));

        // 记录结果信息
        if (StringUtils.isNotBlank(stageResult.getMsg())) {
            stageExecutionResultMsgService.create(StageExecutionResultMsg.builder()
                    .stageExecutionId(stageExecution.getId())
                    .msg(stageResult.getMsg())
                    .createTime(new Date()).build());
        }

        // 记录sharedContext
        ESPojoDTO<StageStartupParam> esPojoDTO = stageStartupParamService.getByStageStartupId(stageStartup.getId());
        StageStartupParam stageStartupParam = esPojoDTO.getData();
        stageStartupParam.setEncodedSharedContextSnapshotAtCompletion(stageResult.getEncodedSharedContextAtCompletion());
        stageStartupParam.setUpdateTime(new Date());
        stageStartupParamService.update(esPojoDTO.getId(), stageStartupParam);

        // 判断task是否已经整体完成，考虑了一下，似乎没有并发问题，最后一个走到这一步的请求会尝试将全部task设置为成功
        TaskExecution taskExecution = taskExecutionServiceWrapper.selectById(stageStartup.getTaskExecutionId());
        TaskStartup taskStartup = taskStartupServiceWrapper.selectById(taskExecution.getTaskStartupId());
        TaskDefinition taskDefinition = taskDefinitionServiceWrapper.selectById(taskStartup.getTaskId());

        TaskDefinitionDTO taskDefinitionDTO = workerTaskDefinitionManager.getTaskDefinitionDTO(taskDefinition.getName(),
                taskDefinition.getVersion());

        List<StageStartup> stageStartupsWithSameTaskExe =
                stageStartupServiceWrapper.selectByTaskExecutionId(stageStartup.getTaskExecutionId());
        if (taskDefinitionDTO.getStageDefinitionMap().size() == stageStartupsWithSameTaskExe.size() &&
                stageStartupsWithSameTaskExe.stream().allMatch(s -> TaskStageStatus.SUCCEEDED.getStatus().equals(s.getStatus()))) {
            // 全部stage都已经尝试启动了，并且状态也已经成功了。更新task状态为成功
            taskExecutionServiceWrapper.updateSelectiveById(taskExecution.getId(),
                    new TaskExecution().withStatus(TaskStageStatus.SUCCEEDED.getStatus())
                            .withCompletionTime(new Date())
                            .withRevision(taskExecution.getRevision() + 1),
                    null
            );

            taskStartupServiceWrapper.updateByIdSelective(taskStartup.getId(),
                    new TaskStartup().withStatus(TaskStageStatus.SUCCEEDED.getStatus())
                            .withRevision(taskStartup.getRevision() + 1),
                    null
            );
        }

    }


    /**
     * 某个stage失败了，对应的task也要失败
     *
     * @param taskExecutionId
     * @param stageExecutionId
     */
    @Transactional(rollbackFor = Exception.class, transactionManager = RepositoryAutoConfig.TRANSACTION_MANAGER_BEAN_NAME)
    public void failStage(long taskExecutionId, long stageExecutionId, String errorMsg) throws IOException {
        log.info("任务失败：taskExeId:{},stageExeId:{}", taskExecutionId, stageExecutionId);
        RLock rLock = redissonClient.getLock(RedissonService.getTaskExecutionLockKey(taskExecutionId));
        try {

            StageExecution stageExecution = stageExecutionServiceWrapper.selectById(stageExecutionId, TaskStageStatus.RUNNING.getStatus());
            if (stageExecution == null) {
                log.info("未发现运行中的id={}的StageExecution对象，可能任务并未启动", stageExecutionId);
                return;
            }
            stageExecution.setStatus(TaskStageStatus.FAILED.getStatus());
            stageExecution.setRevision(stageExecution.getRevision() + 1);
            stageExecutionServiceWrapper.updateSelectiveById(stageExecutionId,
                    new StageExecution().withStatus(stageExecution.getStatus()).withRevision(stageExecution.getRevision()),
                    null);

            StageStartup stageStartup = stageStartupServiceWrapper.selectById(stageExecution.getStageStartupId());
            stageStartup.setFailCount(stageStartup.getFailCount() + 1);
            stageStartup.setRevision(stageStartup.getRevision() + 1);
            stageStartupServiceWrapper.updateSelectiveById(stageStartup.getId(),
                    new StageStartup().withFailCount(stageStartup.getFailCount()).withRevision(stageStartup.getRevision()), null);
            // 记录结果信息
            if (StringUtils.isNotBlank(errorMsg)) {
                stageExecutionResultMsgService.create(StageExecutionResultMsg.builder()
                        .stageExecutionId(stageExecutionId)
                        .msg(errorMsg)
                        .createTime(new Date()).build());
            }

            StageDefinition stageDefinition = stageDefinitionServiceWrapper.selectById(stageStartup.getStageId());

            rLock.lock(10, TimeUnit.SECONDS);
            TaskExecution taskExecution = taskExecutionServiceWrapper.selectById(taskExecutionId);
            if (stageDefinition.getMaxRetryCount() > stageStartup.getFailCount()) {
                // stage超过重试次数了，stage失败掉
                log.info("stageStartup:{}重试超过最大重试次数，需要将taskExecution:{}设置为失败", stageStartup.getId(), taskExecutionId);

                if (taskExecutionServiceWrapper.updateSelectiveById(taskExecutionId, new TaskExecution()
                                .withStatus(TaskStageStatus.FAILED.getStatus())
                                .withRevision(taskExecution.getRevision() + 1),
                        taskExecution.getRevision()) != 1) {
                    log.warn("更新taskExecution:{}为失败状态失败，可能是已经被其他任务并发失败", taskExecutionId);
                }

                TaskStartup taskStartup = taskStartupServiceWrapper.selectById(taskExecution.getTaskStartupId(),
                        TaskStageStatus.RUNNING.getStatus());
                TaskDefinition taskDefinition = taskDefinitionServiceWrapper.selectById(taskStartup.getTaskId());


                if (taskDefinition.getMaxRetryCount() > taskStartup.getFailCount() + 1) {
                    // task任务整体重试超过限制了，task任务整体失败
                    log.info("taskStartup:{}重试超过最大重试次数，需要将taskStartup设置为失败", taskStartup.getId());

                    if (taskStartupServiceWrapper.updateByIdSelective(taskStartup.getId(),
                            new TaskStartup().withFailCount(taskStartup.getFailCount() + 1)
                                    .withStatus(TaskStageStatus.FAILED.getStatus())
                                    .withRevision(taskStartup.getRevision() + 1),
                            taskStartup.getRevision()) == 1) {
                        log.warn("更新taskStartup:{}为失败状态失败，可能是已经被其他任务并发失败", taskStartup.getId());
                    }

                } else {
                    // task还可以重试
                    log.info("taskStartup:{}重试了{}次，还可以重试", taskStartup.getId(), taskStartup.getFailCount());
                    VerifyUtil.requireTrue(
                            taskStartupServiceWrapper.updateByIdSelective(taskStartup.getId(),
                                    new TaskStartup()
                                            .withStatus(TaskStageStatus.PENDING.getStatus())
                                            .withRevision(taskStartup.getRevision() + 1), taskStartup.getRevision()) == 1,
                            String.format("恢复taskStartup:%d为PENDING状态失败，可能已经被恢复", taskStartup.getId()));


                    CompletableFuture.runAsync(() -> {
                        try {
                            retryTask(taskStartup.getId(), taskExecutionId);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }, threadPoolExecutor);
                }

            } else {

                // stage还可以重试
                log.info("stage:{}重试了{}次，还可以重试", stageExecutionId, stageStartup.getFailCount());

                StageExecution retryStageExecution = new StageExecution()
                        .withStageStartupId(stageStartup.getId())
                        .withStatus(TaskStageStatus.PENDING.getStatus())
                        .withWorkerAddress(stageExecution.getWorkerAddress());

                VerifyUtil.requireTrue(stageExecutionService.create(retryStageExecution) == 1, "创建重试任务执行记录失败");

                ESPojoDTO<StageStartupParam> esPojoDTO = stageStartupParamService.getByStageStartupId(stageStartup.getId());


                CompletableFuture.runAsync(() -> {
                    WorkerRetryStageReq.WorkerRetryStageReqData data = new WorkerRetryStageReq.WorkerRetryStageReqData();
                    data.setStageName(stageDefinition.getName());
                    data.setStageExecutionId(retryStageExecution.getId());
                    data.setTaskExecutionId(taskExecutionId);
                    if (esPojoDTO != null) {
                        data.setEncodedInput(esPojoDTO.getData().getEncodedInput());
                    }

                    WorkerRetryStageReq workerRetryStageReq = new WorkerRetryStageReq();
                    workerRetryStageReq.setData(data);

                    HttpBody<String> resp =
                            workerTaskDriverClient.retryStage(workerClusterManager.getWorkerURI(retryStageExecution.getWorkerAddress()),
                                    workerRetryStageReq);

                    if (resp.isSuccess()) {
                        log.error("重试stageExeId:{}发起失败", retryStageExecution.getId());
                        // 重试失败了，兜底

                        workerTaskDriverServiceTxnHelper.forceFailStageAndTask(
                                retryStageExecution.getId(), stageStartup.getId(), taskExecutionId, stageExecution.getStageStartupId()
                        );
                    }
                }, threadPoolExecutor);

            }
        } finally {
//            坑：如果这个锁没有被占用，执行unlock会报错的，所以包装一下，如果这里执行报错了，异常会变成finally的异常
            try {
                if (rLock.isLocked()) {
                    rLock.unlock();
                }
            } catch (Exception e) {
                log.error("解锁异常", e);
            }
        }
        ;
    }


    public void retryTask(long taskStartupId, long failedTaskExecutionId) throws IOException {

        TaskStartup taskStartup = taskStartupServiceWrapper.selectById(taskStartupId, TaskStageStatus.PENDING.getStatus());
        VerifyUtil.requireNotNull(taskStartup, String.format("can't find taskStartupId with id:%d", taskStartupId));
        TaskSharedContext taskSharedContext = taskSharedContextService.getByTaskStartupId(taskStartupId);
        VerifyUtil.requireNotNull(taskSharedContext, String.format("can't find taskSharedContext with taskStartupId:%d", taskStartupId));

        List<StageStartup> stageStartups = stageStartupServiceWrapper.selectByTaskExecutionId(failedTaskExecutionId);
        Map<Long, StageStartup> stageStartupIdMap = stageStartups.stream().collect(Collectors.toMap(StageStartup::getId, s -> s));

        List<ESPojoDTO<StageStartupParam>> stageStartupParams =
                stageStartupParamService.getByStageStartupId(stageStartups.stream().map(StageStartup::getId).collect(Collectors.toList()));

        TaskDefinition taskDefinition = taskDefinitionServiceWrapper.selectById(taskStartup.getTaskId());
        VerifyUtil.requireNotNull(taskDefinition, String.format("can'f find taskdefinition with id:%d for startupid:%d",
                taskStartup.getTaskId(), taskStartup.getId()));
        List<StageDefinition> stageDefinitions = stageDefinitionServiceWrapper.selectByTaskId(taskDefinition.getId());
        VerifyUtil.requireFalse(stageDefinitions.isEmpty(), String.format("无法找到taskId为%s的阶段定义", taskStartup.getId()));

        Map<Long, StageDefinition> stageDefIdMap = stageDefinitions.stream().collect(Collectors.toMap(StageDefinition::getId, d -> d));
        Map<String, String> stageEncodedInputs = stageStartupParams.stream().collect(Collectors.toMap(dto -> {
                    Long stageStartupId = dto.getData().getStageStartupId();
                    StageDefinition stageDefinition = stageDefIdMap.get(stageStartupIdMap.get(stageStartupId).getStageId());
                    return stageDefinition.getName();
                },
                dto -> dto.getData().getEncodedInput()));


        String workerAddress = taskScheduler.assignTaskToWorkerAddress(taskDefinition.getName(), taskDefinition.getVersion());

        TaskExecution retryTaskExecution = new TaskExecution().withTaskStartupId(taskStartup.getId())
                .withStatus(TaskStageStatus.PENDING.getStatus())
                .withAssignedWorkerAddr(workerAddress);

        VerifyUtil.requireTrue(taskExecutionService.create(retryTaskExecution) > 0, "无法创建任务执行对象");


        Map<String, Long> startingStageName2ExecutionId = new HashMap<>();
        for (StageDefinition stageDefinition : stageDefinitions) {
            String stageName = stageDefinition.getName();
            StageStartup stageStartup = new StageStartup()
                    .withTaskExecutionId(retryTaskExecution.getId())
                    .withStatus(TaskStageStatus.PENDING.getStatus())
                    .withStageId(stageDefinition.getId());

            VerifyUtil.requireTrue(stageStartupService.create(stageStartup) > 0,
                    String.format("Failed to create stage startup: %s", stageName));
            if (stageDefinition.getIsStartingStage()) {
                // 正常情况下是在stage启动之前，由scheduler创建execution对象，但是在start task的时候，顺手创建一部分stage的execution
                // 仅仅针对starting 的stage创建execution对象，这样可以在启动任务与worker交互的时候节省一次请求。
                StageExecution stageExecution = new StageExecution()
                        .withStageStartupId(stageStartup.getId())
                        .withStatus(TaskStageStatus.PENDING.getStatus())
                        .withWorkerAddress(workerAddress);
                VerifyUtil.requireTrue(stageExecutionService.create(stageExecution) > 0, String.format("Failed to create stage " +
                        "execution:" +
                        " %s", stageName));
                startingStageName2ExecutionId.put(stageName, stageExecution.getId());

            }
        }


        WorkerStartTaskReq req = new WorkerStartTaskReq();
        WorkerStartTaskReq.Data data = WorkerStartTaskReq.Data.builder()
                .taskName(taskDefinition.getName())
                .taskVersion(taskDefinition.getVersion())
                .stageEncodedInputs(stageEncodedInputs)
                .taskExecutionId(retryTaskExecution.getId())
                .initialEncodedSharedContext(taskSharedContext.getEncodedTaskSharedContext())
                .startingStageExecutionId(startingStageName2ExecutionId)
                .build();
        req.setData(data);

        WorkerStartTaskResp workerStartTaskResp = workerTaskDriverClient.startTask(workerClusterManager.getWorkerURI(workerAddress), req);

        if (workerStartTaskResp.isSuccess()) {
            log.error("重试task(exeId:{})发起失败", retryTaskExecution.getId());
            // 启动失败兜底
            workerTaskDriverServiceTxnHelper.forceFailTask(
                    retryTaskExecution.getId(), taskStartup.getId()
            );
        }


    }
}
