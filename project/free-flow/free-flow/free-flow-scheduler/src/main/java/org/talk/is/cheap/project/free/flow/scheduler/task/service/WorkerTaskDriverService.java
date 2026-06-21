package org.talk.is.cheap.project.free.flow.scheduler.task.service;


import io.vavr.Tuple2;
import io.vavr.Tuple3;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.talk.is.cheap.project.free.flow.common.enums.StartupSourceType;
import org.talk.is.cheap.project.free.flow.common.enums.TaskStageStatus;
import org.talk.is.cheap.project.free.flow.common.exception.TaskExecutionErrorCode;
import org.talk.is.cheap.project.free.flow.common.message.HttpBody;
import org.talk.is.cheap.project.free.flow.common.message.impl.scheduler.PrepareStageReq;
import org.talk.is.cheap.project.free.flow.common.message.impl.scheduler.WorkerCompleteStageResultReq;
import org.talk.is.cheap.project.free.flow.common.message.impl.scheduler.WorkerStartStageReportReq;
import org.talk.is.cheap.project.free.flow.common.message.impl.worker.WorkerResumeTaskReq;
import org.talk.is.cheap.project.free.flow.common.message.impl.worker.WorkerResumeTaskResp;
import org.talk.is.cheap.project.free.flow.common.message.impl.worker.WorkerRetryStageReq;
import org.talk.is.cheap.project.free.flow.common.message.impl.worker.WorkerStartTaskReq;
import org.talk.is.cheap.project.free.flow.common.message.impl.worker.WorkerStartTaskResp;
import org.talk.is.cheap.project.free.flow.common.utils.VerifyUtil;
import org.talk.is.cheap.project.free.flow.scheduler.cluster.service.WorkerClusterManager;
import org.talk.is.cheap.project.free.flow.scheduler.task.client.WorkerTaskDriverClient;
import org.talk.is.cheap.project.free.flow.starter.repository.config.RedisAutoConfig;
import org.talk.is.cheap.project.free.flow.starter.repository.config.RepositoryAutoConfig;
import org.talk.is.cheap.project.free.flow.starter.repository.dao.mbg.query.example.StageStartupExample;
import org.talk.is.cheap.project.free.flow.starter.repository.dao.mbg.query.example.TaskGraphDefinitionExample;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.es.pojo.ESPojoDTO;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.es.pojo.StageExecutionResultMsg;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.es.pojo.StageStartupParam;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.es.pojo.TaskSharedContext;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.StageDefinition;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.StageExecution;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.StageStartup;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.TaskDefinition;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.TaskExecution;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.TaskGraphDefinition;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.TaskStartup;
import org.talk.is.cheap.project.free.flow.starter.repository.service.StageDefinitionService;
import org.talk.is.cheap.project.free.flow.starter.repository.service.StageExecutionService;
import org.talk.is.cheap.project.free.flow.starter.repository.service.StageStartupService;
import org.talk.is.cheap.project.free.flow.starter.repository.service.TaskExecutionService;
import org.talk.is.cheap.project.free.flow.starter.repository.service.TaskGraphDefinitionService;
import org.talk.is.cheap.project.free.flow.starter.repository.service.TaskSourceTargetStartupRelationService;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * 提示：
 * 注意并发，如果可能操作task级别的对象，需要并发加锁避免一些stage并行执行某些stage失败后更新task对象导致数据不一致，但如果只是更新某个stage，不用更关心并发问题，
 * 因为一个stage一般只会有一个scheduler处理，更新时，为避免死锁，先更新execution，再更新startup
 * <p>
 * <p>
 * 更新，并发问题没那么复杂，因为即使不加锁，多数情况下也不会有并发问题。
 * 任务的执行包括：
 * 1. prepareForTask
 * 2. 然后启动执行
 * 3. 针对某个stage，先做prepareForStage，
 * 4. 任务执行之前，worker做startStageReport
 * 5. 然后completeStage或者failAndRetryStage
 * <p>
 * 其中4和5一定在3完成获得响应之后再触发，因为worker代码中只有3完成之后才会开始执行任务，于是，只有后两个操作需要考虑并发问题，然而startStageReport在操作的时候只会更新pending状态的对象，如果某个stage
 * 已经失败了，那么执行stageReport也没影响
 * 而同一个stage肯定不可能即complete又fail，因此也没有并发问题。
 * <p>
 * 但是要注意数据库死锁，无论是操作task还是startup，都需要按照同一个顺序执行更新，先更新execution，再更新startup
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


        /**
         * 确保先更新taskExecution，再更新taskStartup，避免多线程并发的时候，db死锁
         *
         * @param taskExecutionId
         * @param taskStartupId
         */
        @Transactional(rollbackFor = Exception.class, transactionManager = RepositoryAutoConfig.TRANSACTION_MANAGER_BEAN_NAME)
        public void forceFailTask(Long taskExecutionId, Long taskStartupId) {
            if (taskExecutionId != null) {
                taskExecutionServiceWrapper.updateSelectiveById(taskExecutionId,
                        new TaskExecution().withStatus(TaskStageStatus.FAILED.getStatus()), null);
            }
            if (taskStartupId != null) {
                taskStartupServiceWrapper.updateByIdSelective(taskStartupId,
                        new TaskStartup().withStatus(TaskStageStatus.FAILED.getStatus()), null);
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

    @Autowired
    private TaskSourceTargetStartupRelationService taskSourceTargetStartupRelationService;

    // es
    @Autowired
    private StageStartupParamService stageStartupParamService;

    @Autowired
    private TaskSharedContextService taskSharedContextService;

    @Autowired
    private StageExecutionResultMsgService stageExecutionResultMsgService;

//    bugfix:只有leader有完整的workerTaskDefinitionManager，而当前这个service可能会被所有scheduler调用，因此不应当依赖这个
//    @Autowired
//    private WorkerTaskDefinitionManager workerTaskDefinitionManager;

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
        String workerAddress = taskScheduler.assignTaskToWorkerAddress(taskName, taskDefinition.getVersion());
        VerifyUtil.requireNotBlank(workerAddress, "No node capable of executing this task can be found.");

        // 创建task的startup和execution
        TaskStartup taskStartup = new TaskStartup()
                .withTaskId(taskDefinition.getId())
                .withSourceType(StartupSourceType.EXTERNAL.getValue())
                .withStatus(TaskStageStatus.PENDING.getStatus());
        VerifyUtil.requireTrue(taskStartupService.create(taskStartup) > 0, "创建task启动记录失败");

        try {
            // 只记录启动时候的
            taskSharedContextService.create(TaskSharedContext.builder()
                    .taskStartupId(taskStartup.getId())
                    .encodedTaskSharedContext(initialEncodedSharedContext)
                    .updateTime(new Date()).build()
            );


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

                // 为所有stage都创建Startup
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

                if (stageEncodedInputs.containsKey(stageName) || stageDefinition.getIsStartingStage()) {
                    // 如果对某个stage有设定输入，需要记录输入信息，用以后续的恢复任务之类的使用
                    // 如果是启动节点，也需要创建stageStartupParam，用来承接成功后记录sharedContext，详见completeStage中做的事
                    String encodedInput = stageEncodedInputs.get(stageName);
                    stageStartupParamService.create(
                            StageStartupParam.builder().stageStartupId(stageStartup.getId())
                                    .encodedInput(encodedInput)
                                    .encodedSharedContextSnapshotAtStartup(stageDefinition.getIsStartingStage()?initialEncodedSharedContext:null)
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
     * <p>
     * 不用加锁，如果stageStageReport和failStage对同一个stage并发了，startStageReport在failStage前还是后执行，对failStage不造成影响
     *
     * @param startToExecuteStageReqData
     */
    @Transactional(rollbackFor = Exception.class, transactionManager = RepositoryAutoConfig.TRANSACTION_MANAGER_BEAN_NAME)
    public void startStageReport(List<WorkerStartStageReportReq.WorkerStartToExecuteStageReqDatum> startToExecuteStageReqData) {
        for (WorkerStartStageReportReq.WorkerStartToExecuteStageReqDatum datum : startToExecuteStageReqData) {

            Long stageExecutionId = datum.getStageExecutionId();
            Date startTime = datum.getStartTime();
            StageExecution stageExecution = stageExecutionServiceWrapper.selectById(stageExecutionId, TaskStageStatus.PENDING.getStatus());
            if (stageExecution != null) {
                stageExecutionServiceWrapper.updateSelectiveById(stageExecutionId,
                        new StageExecution().withStatus(TaskStageStatus.RUNNING.getStatus())
                                .withRevision(stageExecution.getRevision() + 1)
                                .withStartTime(startTime),
                        stageExecution.getRevision());

                StageStartup stageStartup = stageStartupServiceWrapper.selectById(stageExecution.getStageStartupId(),
                        TaskStageStatus.PENDING.getStatus());
                if (stageStartup != null) {
                    stageStartupServiceWrapper.updateSelectiveById(stageStartup.getId(),
                            new StageStartup()
                                    .withRevision(stageStartup.getRevision() + 1)
                                    .withStatus(TaskStageStatus.RUNNING.getStatus()), stageStartup.getRevision());
                }
            }

            Long taskExecutionId = datum.getTaskExecutionId();
            TaskExecution taskExecution = taskExecutionServiceWrapper.selectById(taskExecutionId, TaskStageStatus.PENDING.getStatus());
            if (taskExecution != null) {
                taskExecutionServiceWrapper.updateSelectiveById(taskExecutionId,
                        new TaskExecution()
                                .withRevision(taskExecution.getRevision())
                                .withStatus(TaskStageStatus.RUNNING.getStatus()), taskExecution.getRevision());

                TaskStartup taskStartup = taskStartupServiceWrapper.selectById(taskExecution.getTaskStartupId(),
                        TaskStageStatus.PENDING.getStatus());
                if (taskStartup != null) {
                    taskStartupServiceWrapper.updateByIdSelective(taskStartup.getId(),
                            new TaskStartup()
                                    .withRevision(taskStartup.getRevision())
                                    .withStatus(TaskStageStatus.RUNNING.getStatus()),
                            taskStartup.getRevision());
                }
            }
        }
    }


    /**
     * worker要运行某个stage，scheduler做好准备，即创建对应的stageExecution并且返回input给worker，非根节点都需要再次prepare
     *
     * @return
     * @throws IOException
     */
    @Transactional(rollbackFor = Exception.class, transactionManager = RepositoryAutoConfig.TRANSACTION_MANAGER_BEAN_NAME)
    public Tuple2<Long, String> prepareForStage(PrepareStageReq.PrepareStageReqData data) throws IOException {
        long taskExecutionId = data.getTaskExecutionId();
        String stageName = data.getStageName();
        String encodedSharedContextSnapshotAtStartup = data.getEncodedSharedContextSnapshotAtStartup();
        Date prepareTime = data.getPrepareTime();

        TaskExecution taskExecution = taskExecutionServiceWrapper.selectById(taskExecutionId, TaskStageStatus.RUNNING.getStatus());
        VerifyUtil.requireNotNull(taskExecution, String.format("无法找到id为%d的运行中的taskExecution对象", taskExecutionId));

        TaskStartup taskStartup = taskStartupServiceWrapper.selectById(taskExecution.getTaskStartupId(),
                TaskStageStatus.RUNNING.getStatus());
        VerifyUtil.requireNotNull(taskStartup,
                String.format("无法找到id为%d的运行中的taskStartup对象", taskExecution.getTaskStartupId()));

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

        ESPojoDTO<StageStartupParam> stageStartupParamESPojoDTO = stageStartupParamService.getByStageStartupIds(stageStartup.getId());
        StageStartupParam stageStartupParam = null;
        if (stageStartupParamESPojoDTO == null) {
            // 只有starting stage才会在startTask的时候创建stageStartupParam对象，其他的stage都得手动创建。
            stageStartupParam = StageStartupParam.builder()
                    .stageStartupId(stageStartup.getId())
                    .encodedInput("")
                    .encodedSharedContextSnapshotAtStartup(encodedSharedContextSnapshotAtStartup)
                    .updateTime(prepareTime)
                    .build();
            stageStartupParamService.create(stageStartupParam);
        } else {
            stageStartupParam = stageStartupParamESPojoDTO.getData();
            if (StringUtils.isNotBlank(encodedSharedContextSnapshotAtStartup)) {
                stageStartupParam.setEncodedSharedContextSnapshotAtStartup(encodedSharedContextSnapshotAtStartup);
                stageStartupParam.setUpdateTime(prepareTime);
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
     * 仅仅updatestage信息
     * <p>
     * update:
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
        Integer stageStatus = stageResult.getSucceeded() ? TaskStageStatus.SUCCEEDED.getStatus() :
                TaskStageStatus.FAILED.getStatus();

        VerifyUtil.requireTrue(stageExecutionServiceWrapper.updateSelectiveById(
                        stageExecutionId, new StageExecution().withStatus(stageStatus), stageExecution.getRevision()) > 0,
                String.format("更新stageExecution:%d成功状态失败", stageExecutionId));
        VerifyUtil.requireTrue(stageStartupServiceWrapper.updateSelectiveById(stageStartup.getId(),
                        new StageStartup().withStatus(stageStatus).withCompletionTime(stageResult.getCompletionTime()),
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
        ESPojoDTO<StageStartupParam> esPojoDTO = stageStartupParamService.getByStageStartupIds(stageStartup.getId());
        if (esPojoDTO == null) {
            log.info("{}", stageStartup);
            log.info("{}", stageStartupParamService.getByStageStartupIds(stageStartup.getId()));
        }
        StageStartupParam stageStartupParam = esPojoDTO.getData();
        stageStartupParam.setEncodedSharedContextSnapshotAtCompletion(stageResult.getEncodedSharedContextAtCompletion());
        stageStartupParam.setUpdateTime(new Date());
        stageStartupParamService.update(esPojoDTO.getId(), stageStartupParam);
    }

    public void completeTask(long taskExecutionId) {
        // 判断task是否已经整体完成，考虑了一下，似乎没有并发问题
        TaskExecution taskExecution = taskExecutionServiceWrapper.selectById(taskExecutionId);
        TaskStartup taskStartup = taskStartupServiceWrapper.selectById(taskExecution.getTaskStartupId());
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


    /**
     * 某个stage失败了，对应的task也要失败
     *
     */
    /**
     * @param taskExecutionId
     * @param stageExecutionId
     * @param errorMsg
     * @param workerPausing    如果worker在暂停状态中，那么就不需要重试，这个任务需要等待重新分配
     * @throws IOException
     */
    public void failStageAndRetry(long taskExecutionId, long stageExecutionId, Integer errorCode, String errorMsg, boolean workerPausing) throws IOException {

        log.info("任务失败：taskExeId:{},stageExeId:{}", taskExecutionId, stageExecutionId);
        RLock rLock = redissonClient.getLock(RedissonService.getTaskExecutionLockKey(taskExecutionId));
        try {
            // 判断重试，这块需要考虑发起任务和更新db的并发问题，
            // 一个是不同机器之间的并发，比如一个阶段失败了，另一个阶段也失败了，他们如果请求到不同的scheduler，这可能会重复重试，这种情况下revision控制不住，需要分布式锁(其实数据库锁也行)
            rLock.lock(5, TimeUnit.SECONDS);

            // 拆分为两个方法，确保db操作完成提交之后，再提交runAsync方法去跑真正的retry任务。
            Runnable retryRunnable = failStageAndPrepareRetryRunnable(taskExecutionId, stageExecutionId, errorCode, errorMsg,
                    workerPausing);

            if (retryRunnable != null) {
                CompletableFuture.runAsync(retryRunnable, threadPoolExecutor)
                        .exceptionally(e -> {
                            log.error("重试task异常", e);
                            return null;
                        });
            }
        } finally {
//            坑：如果这个锁没有被占用，执行unlock会报错的，所以需要判断一下。否则如果这里执行报错了，上面业务执行的异常会变成finally的异常
            try {
                if (rLock.isLocked()) {
                    rLock.unlock();
                }
            } catch (Exception e) {
                log.error("解锁异常", e);
            }
        }
    }


    /**
     * 完成所有失败与准备重试的db准备，返回一个runnable对象，作为一个callback，执行会发起重试任务的请求。
     *
     * @param taskExecutionId
     * @param stageExecutionId
     * @param errorMsg
     * @param workerPausing    这个worker是否已经在暂停中，如果这个worker是在暂停中，那么retryStage的操作不要发起
     * @return
     * @throws IOException
     */
    @Transactional(rollbackFor = Exception.class, transactionManager = RepositoryAutoConfig.TRANSACTION_MANAGER_BEAN_NAME)
    private Runnable failStageAndPrepareRetryRunnable(long taskExecutionId, long stageExecutionId, Integer errorCode, String errorMsg,
                                                      boolean workerPausing) throws IOException {
        StageExecution stageExecution = stageExecutionServiceWrapper.selectById(stageExecutionId, TaskStageStatus.RUNNING.getStatus());
        if (stageExecution == null) {
            log.info("未发现运行中的id={}的StageExecution对象，可能任务并未启动", stageExecutionId);
            return null;
        }

        TaskStageStatus stageStatus = TaskStageStatus.FAILED;
        if (TaskExecutionErrorCode.STAGE_TIME_OUT.getCode().equals(errorCode)) {
            stageStatus = TaskStageStatus.TIME_OUT;
        }
        stageExecution.setRevision(stageExecution.getRevision() + 1);
        stageExecutionServiceWrapper.updateSelectiveById(stageExecutionId,
                new StageExecution().withStatus(stageStatus.getStatus()).withRevision(stageExecution.getRevision()),
                null);

        // 记录结果信息
        if (StringUtils.isNotBlank(errorMsg)) {
            stageExecutionResultMsgService.create(StageExecutionResultMsg.builder()
                    .stageExecutionId(stageExecutionId)
                    .msg(errorMsg)
                    .createTime(new Date()).build());
        }
        StageStartup stageStartup = stageStartupServiceWrapper.selectById(stageExecution.getStageStartupId());
        stageStartup.setFailCount(stageStartup.getFailCount() + 1);
        stageStartup.setRevision(stageStartup.getRevision() + 1);
        StageDefinition stageDefinition = stageDefinitionServiceWrapper.selectById(stageStartup.getStageId());


        if (stageStartup.getFailCount() >= stageDefinition.getMaxRetryCount()) {
            // stage超过重试次数了，stage失败掉
            log.info("stageStartup:{}重试超过最大重试次数设置为失败，还需要将taskExecution:{}设置为失败", stageStartup.getId(), taskExecutionId);
            stageStartupServiceWrapper.updateSelectiveById(stageStartup.getId(),
                    new StageStartup()
                            .withFailCount(stageStartup.getFailCount())
                            .withStatus(stageStatus.getStatus())
                            .withRevision(stageStartup.getRevision()), null);
            // 随之task也要失败
            return failTaskAndPrepareRetryRunnable(taskExecutionId, errorCode);

        } else {
            // stage还可以重试
            log.info("stage(startupId:{})重试了{}次，还可以重试", stageStartup.getId(), stageStartup.getFailCount());
            stageStartupServiceWrapper.updateSelectiveById(stageStartup.getId(),
                    new StageStartup()
                            .withFailCount(stageStartup.getFailCount())
                            .withStatus(TaskStageStatus.PENDING.getStatus())
                            .withRevision(stageStartup.getRevision()), null);

            StageExecution retryStageExecution = new StageExecution()
                    .withStageStartupId(stageStartup.getId())
                    .withStatus(TaskStageStatus.PENDING.getStatus())
                    .withWorkerAddress(stageExecution.getWorkerAddress());

            VerifyUtil.requireTrue(stageExecutionService.create(retryStageExecution) == 1, "创建重试任务执行记录失败");

            ESPojoDTO<StageStartupParam> esPojoDTO = stageStartupParamService.getByStageStartupIds(stageStartup.getId());
            StageStartupParam stageStartupParam;
            if (esPojoDTO == null) {
                // 如果为空说明这个stage在prepare阶段就失败了，因此encodedSharedContextSnapshotAtStartup没有被记录下来只能记为lost
                stageStartupParam = StageStartupParam.builder()
                        .stageStartupId(stageStartup.getId())
                        .encodedInput("")
                        .encodedSharedContextSnapshotAtStartup("lost")
                        .updateTime(new Date())
                        .build();
                stageStartupParamService.create(stageStartupParam);
            } else {
                stageStartupParam = esPojoDTO.getData();
            }
            if (workerPausing) {
                // worker停止中，等待重调度到新的节点继续执行
                return null;
            }

            return () -> {
                try {

                    WorkerRetryStageReq.WorkerRetryStageReqData data = new WorkerRetryStageReq.WorkerRetryStageReqData();
                    data.setStageName(stageDefinition.getName());
                    data.setStageExecutionId(retryStageExecution.getId());
                    data.setTaskExecutionId(taskExecutionId);
                    data.setEncodedInput(stageStartupParam.getEncodedInput());
                    data.setStageFailedCount(stageStartup.getFailCount());

                    WorkerRetryStageReq workerRetryStageReq = new WorkerRetryStageReq();
                    workerRetryStageReq.setData(data);

                    HttpBody<String> resp =
                            workerTaskDriverClient.retryStage(WorkerClusterManager.getWorkerURI(retryStageExecution.getWorkerAddress()),
                                    workerRetryStageReq);
                    VerifyUtil.requireTrue(resp.isSuccess(), String.format("重试stageExeId:%d发起失败，错误信息：%s", retryStageExecution.getId(),
                            resp.getMsg()));
                } catch (Exception e) {
                    log.error("stage重试失败", e);
                    workerTaskDriverServiceTxnHelper.forceFailStageAndTask(
                            retryStageExecution.getId(), stageStartup.getId(), taskExecutionId,
                            stageExecution.getStageStartupId()
                    );
                }
            };
        }

    }

    @Nullable
    @Transactional(rollbackFor = Exception.class, transactionManager = RepositoryAutoConfig.TRANSACTION_MANAGER_BEAN_NAME)
    private Runnable failTaskAndPrepareRetryRunnable(long taskExecutionId, Integer errorCode) {
        TaskExecution taskExecution = taskExecutionServiceWrapper.selectById(taskExecutionId);
        VerifyUtil.requireTrue(TaskStageStatus.RUNNING.getStatus().equals(taskExecution.getStatus()),
                String.format("任务(taskExecutionId:%d)已经不是运行状态，可能已经启动了重试", taskExecutionId));
        Integer status = TaskExecutionErrorCode.TASK_TIME_OUT.getCode().equals(errorCode) ?
                TaskStageStatus.TIME_OUT.getStatus() : TaskStageStatus.FAILED.getStatus();
        if (taskExecutionServiceWrapper.updateSelectiveById(taskExecutionId, new TaskExecution()
                        .withStatus(status)
                        .withRevision(taskExecution.getRevision() + 1),
                taskExecution.getRevision()) != 1) {
            log.warn("更新taskExecution:{}为失败状态失败，可能是已经被其他任务并发失败", taskExecutionId);
        }

        TaskStartup taskStartup = taskStartupServiceWrapper.selectById(taskExecution.getTaskStartupId(),
                TaskStageStatus.RUNNING.getStatus());
        TaskDefinition taskDefinition = taskDefinitionServiceWrapper.selectById(taskStartup.getTaskId());

        if (taskStartup.getFailCount() + 1 >= taskDefinition.getMaxRetryCount()) {
            log.info("taskStartup:{}重试超过最大重试次数，需要将taskStartup设置为失败", taskStartup.getId());
            // task任务整体重试超过限制了，task任务整体失败

            if (taskStartupServiceWrapper.updateByIdSelective(taskStartup.getId(),
                    new TaskStartup().withFailCount(taskStartup.getFailCount() + 1)
                            .withStatus(status)
                            .withRevision(taskStartup.getRevision() + 1),
                    taskStartup.getRevision()) != 1) {
                log.warn("更新taskStartup:{}为失败状态失败，可能是已经被其他任务并发失败", taskStartup.getId());
            }
            workerTaskDriverClient.clearTask(WorkerClusterManager.getWorkerURI(taskExecution.getAssignedWorkerAddr()),
                    taskExecutionId);
            return null;

        } else {
            // task还可以重试
            log.info("taskStartup:{}重试了{}次，还可以重试", taskStartup.getId(), taskStartup.getFailCount());

            VerifyUtil.requireTrue(
                    taskStartupServiceWrapper.updateByIdSelective(taskStartup.getId(),
                            new TaskStartup()
                                    .withFailCount(taskStartup.getFailCount() + 1)
                                    .withStatus(TaskStageStatus.PENDING.getStatus())
                                    .withRevision(taskStartup.getRevision() + 1), taskStartup.getRevision()) == 1,
                    String.format("恢复taskStartup:%d为PENDING状态失败，可能已经被恢复", taskStartup.getId()));
            workerTaskDriverClient.clearTask(WorkerClusterManager.getWorkerURI(taskExecution.getAssignedWorkerAddr()),
                    taskExecutionId);
            return () -> {
                retryTask(taskStartup.getId(), taskExecutionId);
            };
        }
    }


    public void failTaskAndRetry(long taskExecutionId, Integer errorCode, String errorMsg) {
        log.info("任务失败：taskExeId:{}", taskExecutionId);
        RLock rLock = redissonClient.getLock(RedissonService.getTaskExecutionLockKey(taskExecutionId));
        try {
            // 判断重试，这块需要考虑发起任务和更新db的并发问题，
            // 1. 同一台机器之间不同线程的并发，db更新没完成，就在另一个线程里尝试读取并操作，那可能是读取不到的db里的信息的。这个需要本地锁解决
            // 2. 不同机器操作同一个taskStartup或者重复创建taskExecution的并发，这个需要通过revision锁或者redis锁
            // 3. 不同机器之间的并发，比如一个阶段失败了，另一个阶段也失败了，他们如果请求到不同的scheduler，
            // 一个可能希望重试stage，另一个可能希望直接失败掉任务，这可能会创建重复的taskExe，这种情况下revision控制不住，比如通过锁控制
            rLock.lock(5, TimeUnit.SECONDS);

            // 拆分为两个方法，确保db操作完成提交之后，再提交runAsync方法去跑真正的retry任务。
            Runnable retryRunnable = failTaskAndPrepareRetryRunnable(taskExecutionId, errorCode);

            if (retryRunnable != null) {
                CompletableFuture.runAsync(retryRunnable, threadPoolExecutor)
                        .exceptionally(e -> {
                            log.error("重试task异常", e);
                            return null;
                        });
            }
        } finally {
//            坑：如果这个锁没有被占用，执行unlock会报错的，所以需要判断一下。否则如果这里执行报错了，上面业务执行的异常会变成finally的异常
            try {
                if (rLock.isLocked()) {
                    rLock.unlock();
                }
            } catch (Exception e) {
                log.error("解锁异常", e);
            }
        }
    }

    private void retryTask(long taskStartupId, long failedTaskExecutionId) {

        Long retryTaskExecutionId = null;
        try {

            TaskStartup taskStartup = taskStartupServiceWrapper.selectById(taskStartupId, TaskStageStatus.PENDING.getStatus());
            VerifyUtil.requireNotNull(taskStartup, String.format("can't find pending taskStartup id:%d", taskStartupId));
            TaskSharedContext taskSharedContext = taskSharedContextService.getByTaskStartupId(taskStartupId);
            VerifyUtil.requireNotNull(taskSharedContext, String.format("can't find taskSharedContext with taskStartupId:%d",
                    taskStartupId));


            TaskDefinition taskDefinition = taskDefinitionServiceWrapper.selectById(taskStartup.getTaskId());
            VerifyUtil.requireNotNull(taskDefinition, String.format("can'f find taskdefinition with id:%d for startupid:%d",
                    taskStartup.getTaskId(), taskStartup.getId()));
            List<StageDefinition> stageDefinitions = stageDefinitionServiceWrapper.selectByTaskId(taskDefinition.getId());
            VerifyUtil.requireFalse(stageDefinitions.isEmpty(), String.format("无法找到taskId为%s的阶段定义", taskStartup.getId()));
            Map<Long, StageDefinition> stageDefIdMap = stageDefinitions.stream().collect(Collectors.toMap(StageDefinition::getId, d -> d));

            // 从执行失败的taskExecution获取启动任务所需的相关入参
            List<StageStartup> failedTaskStageStartups = stageStartupServiceWrapper.selectByTaskExecutionId(failedTaskExecutionId);
            Map<Long, StageStartup> failedTaskStageStartupIdMap =
                    failedTaskStageStartups.stream().collect(Collectors.toMap(StageStartup::getId, s -> s));
            List<ESPojoDTO<StageStartupParam>> failedTaskStageStartupParamDTOs =
                    stageStartupParamService.getByStageStartupIds(failedTaskStageStartups.stream().map(StageStartup::getId).collect(Collectors.toList()));
            Map<String, StageStartupParam> failedTaskStageNameParamMap =
                    failedTaskStageStartupParamDTOs.stream().collect(Collectors.toMap(dto -> {
                                Long stageStartupId = dto.getData().getStageStartupId();
                                StageDefinition stageDefinition =
                                        stageDefIdMap.get(failedTaskStageStartupIdMap.get(stageStartupId).getStageId());
                                return stageDefinition.getName();
                            },
                            ESPojoDTO::getData));

            // 有可能会被调度到新的节点执行
            String workerAddress = taskScheduler.assignTaskToWorkerAddress(taskDefinition.getName(), taskDefinition.getVersion());

            TaskExecution retryTaskExecution = new TaskExecution().withTaskStartupId(taskStartup.getId())
                    .withStatus(TaskStageStatus.PENDING.getStatus())
                    .withAssignedWorkerAddr(workerAddress);

            VerifyUtil.requireTrue(taskExecutionService.create(retryTaskExecution) > 0, "无法创建任务执行对象");
            retryTaskExecutionId = retryTaskExecution.getTaskStartupId();

            Map<String, Long> startingStageName2ExecutionId = new HashMap<>();
            Map<String, String> retryTaskStageEncodedInputs = new HashMap<>();
            for (StageDefinition stageDefinition : stageDefinitions) {
                String stageName = stageDefinition.getName();
                StageStartup retryTaskStageStartup = new StageStartup()
                        .withTaskExecutionId(retryTaskExecution.getId())
                        .withStatus(TaskStageStatus.PENDING.getStatus())
                        .withStageId(stageDefinition.getId());

                VerifyUtil.requireTrue(stageStartupService.create(retryTaskStageStartup) > 0,
                        String.format("Failed to create stage startup: %s", stageName));

                if (stageDefinition.getIsStartingStage() || failedTaskStageNameParamMap.containsKey(stageName)) {
                    // 从历史执行记录的es中读取上一次任务启动时的入参，包括各个stage的启动参数，以及sharedContext，并es中重建新的taskExecution的stageStartup对象的es对象
                    StageStartupParam failedTaskStageStartupParam = failedTaskStageNameParamMap.get(stageName);
                    String encodedInput = null;
                    if(failedTaskStageStartupParam!=null){
                        retryTaskStageEncodedInputs.put(stageName, failedTaskStageStartupParam.getEncodedInput());
                        encodedInput = failedTaskStageStartupParam.getEncodedInput();
                    }
                    stageStartupParamService.create(
                            StageStartupParam.builder().stageStartupId(retryTaskStageStartup.getId())
                                    .encodedInput(encodedInput)
                                    .encodedSharedContextSnapshotAtStartup(stageDefinition.getIsStartingStage()?taskSharedContext.getEncodedTaskSharedContext():null)
                                    .updateTime(new Date())
                                    .build()
                    );
                }

                if (stageDefinition.getIsStartingStage()) {
                    // 正常情况下是在stage启动之前，由scheduler创建execution对象，但是在start task的时候，顺手创建一部分stage的execution
                    // 仅仅针对starting 的stage创建execution对象，这样可以在启动任务与worker交互的时候节省一次请求。
                    StageExecution stageExecution = new StageExecution()
                            .withStageStartupId(retryTaskStageStartup.getId())
                            .withStatus(TaskStageStatus.PENDING.getStatus())
                            .withWorkerAddress(workerAddress);
                    VerifyUtil.requireTrue(stageExecutionService.create(stageExecution) > 0,
                            String.format("Failed to create stage execution: %s", stageName));
                    startingStageName2ExecutionId.put(stageName, stageExecution.getId());

                }
            }
            WorkerStartTaskReq req = new WorkerStartTaskReq();
            WorkerStartTaskReq.Data data = WorkerStartTaskReq.Data.builder()
                    .taskName(taskDefinition.getName())
                    .taskVersion(taskDefinition.getVersion())
                    .stageEncodedInputs(retryTaskStageEncodedInputs)
                    .taskExecutionId(retryTaskExecution.getId())
                    .initialEncodedSharedContext(taskSharedContext.getEncodedTaskSharedContext())
                    .startingStageExecutionId(startingStageName2ExecutionId)
                    .taskFailedCount(taskStartup.getFailCount())
                    .build();
            req.setData(data);

            WorkerStartTaskResp workerStartTaskResp = workerTaskDriverClient.startTask(WorkerClusterManager.getWorkerURI(workerAddress),
                    req);
            VerifyUtil.requireTrue(workerStartTaskResp.isSuccess(),
                    String.format("重试task(startupId:%d,retryTaskExeId:%d)发起失败，msg:%s", taskStartupId, retryTaskExecution.getId(),
                            workerStartTaskResp.getMsg()));
        } catch (Exception e) {
            log.error("重试task失败", e);
            // 启动失败兜底
            workerTaskDriverServiceTxnHelper.forceFailTask(retryTaskExecutionId, taskStartupId);
        }

    }

    @Transactional(rollbackFor = Exception.class, transactionManager = RepositoryAutoConfig.TRANSACTION_MANAGER_BEAN_NAME)
    public void rescheduleTask(long pausedTaskExecutionId) throws IOException {
        TaskExecution pausedTaskExecution = taskExecutionServiceWrapper.selectById(pausedTaskExecutionId);
        VerifyUtil.requireNotNull(pausedTaskExecution, String.format("未找到id为%d的TaskExecution对象", pausedTaskExecutionId));
        if (TaskStageStatus.FAILED.getStatus().equals(pausedTaskExecution.getStatus())) {
            // 如果pausedTaskExecutionId已经失败了，不应该被重新调度了。
            // 对应上述fail操作的时候，如果某个task的失败，会有两种原因：
            // 1. task执行次数超过限制，task执行已经失败，不应当重新调度；
            // 2. task执行次数没有超过限制，failStage中已经重新分配节点执行，那么pausedTaskExecutionId也不应当被重新调度
            // 综上，重新调度的时候需要避免某个已经完整失败的task被重新调度。
            log.info("任务TaskExecutionId:{}已经失败，无需重新调度", pausedTaskExecution);
            return;
        }
        taskExecutionServiceWrapper.updateSelectiveById(pausedTaskExecutionId,
                new TaskExecution().withStatus(TaskStageStatus.RESCHEDULED.getStatus()), null);


        // 先定位任务的基础信息
        TaskStartup pausedTaskStartup = taskStartupServiceWrapper.selectById(pausedTaskExecution.getTaskStartupId());
        VerifyUtil.requireNotNull(pausedTaskStartup, String.format("未找到id为%d的TaskExecution对象", pausedTaskExecution.getTaskStartupId()));
        taskStartupServiceWrapper.updateByIdSelective(pausedTaskStartup.getId(),
                new TaskStartup().withStatus(TaskStageStatus.RESCHEDULED.getStatus()), null);

        TaskDefinition taskDefinition = taskDefinitionServiceWrapper.selectById(pausedTaskStartup.getTaskId());
        Map<Long, StageDefinition> stageIdDefinition = stageDefinitionServiceWrapper.selectByTaskId(pausedTaskStartup.getTaskId()).stream()
                .collect(Collectors.toMap(StageDefinition::getId, d -> d));


        VerifyUtil.requireNotNull(taskDefinition, String.format("未找到id为%d的TaskDefinition对象", pausedTaskStartup.getTaskId()));

        // 获取这个任务的执行情况
        List<StageStartup> pausedTaskStageStartups = stageStartupServiceWrapper.selectByTaskExecutionId(pausedTaskExecutionId);
        List<ESPojoDTO<StageStartupParam>> pausedTaskStageParamDTOs =
                stageStartupParamService.getByStageStartupIds(pausedTaskStageStartups.stream().map(StageStartup::getId).collect(Collectors.toList()));

        // 寻找最新的共享上下文以及各个stage的入参，此时任务处于暂停状态，没有在运行中的任务，因此买个stageStartup的状态要么是失败，要么是成功。
        String latestEncodedSharedContext = null;
        Date latestUpdateTime = null;
        Map<String, String> stageNameEncodedInputs = new HashMap<>();
        Map<Long, StageStartup> pausedTaskStageStartupIdMap =
                pausedTaskStageStartups.stream().collect(Collectors.toMap(StageStartup::getId, s -> s));
        for (ESPojoDTO<StageStartupParam> stageParamDTO : pausedTaskStageParamDTOs) {
            StageStartupParam data = stageParamDTO.getData();
            if (data == null) {
                continue;
            }
            VerifyUtil.requireTrue(
                    pausedTaskStageStartupIdMap.containsKey(data.getStageStartupId()) &&
                            stageIdDefinition.containsKey(pausedTaskStageStartupIdMap.get(data.getStageStartupId()).getStageId()),
                    String.format("未找到对应的任务定义：stageStartupId: %d", data.getStageStartupId()));
            stageNameEncodedInputs.put(stageIdDefinition.get(pausedTaskStageStartupIdMap.get(data.getStageStartupId()).getStageId()).getName(),
                    data.getEncodedInput());

            if (latestUpdateTime == null || data.getUpdateTime().compareTo(latestUpdateTime) > 0) {
                latestUpdateTime = data.getUpdateTime();
                if (StringUtils.isNotBlank(data.getEncodedSharedContextSnapshotAtCompletion())) {
                    latestEncodedSharedContext = data.getEncodedSharedContextSnapshotAtCompletion();
                } else if (StringUtils.isNotBlank(data.getEncodedSharedContextSnapshotAtStartup())) {
                    latestEncodedSharedContext = data.getEncodedSharedContextSnapshotAtStartup();
                }
            }
        }


        Set<Long> pausedTaskSucceedStageIds = new HashSet<>();
//        Set<Long> pausedTaskSucceedStageStartIds = new HashSet<>();
        for (StageStartup stageStartup : pausedTaskStageStartups) {
            if (TaskStageStatus.SUCCEEDED.getStatus().equals(stageStartup.getStatus())) {
                pausedTaskSucceedStageIds.add(stageStartup.getStageId());
//                pausedTaskSucceedStageStartIds.add(stageStartup.getId());
            }
        }
        // 通过bfs找到需要从哪开始执行，其实就是通过bfs并且跳过所有的成功的节点，找到第一个未成功的节点
        /**
         * 大概意思是比如有这样一个图
         *          A      D
         *       B     C        F
         *     G
         * A连BC，D连CF，B指向G，如果A D C成功，那么就应该从B和F执行
         * 本质上就是找到所有全部父节点都已经完成且本节点没有完整的节点
         */
        TaskGraphDefinitionExample taskGraphDefinitionExample = new TaskGraphDefinitionExample();
        taskGraphDefinitionExample.createCriteria().andTaskIdEqualTo(pausedTaskStartup.getTaskId());
        List<TaskGraphDefinition> taskGraphDefinitions = taskGraphDefinitionService.selectByExample(taskGraphDefinitionExample);
        Map<Long, Set<Long>> pointOutStageIdGraph = new HashMap<>();
        Map<Long, Set<Long>> pointInStageIdGraph = new HashMap<>();
        for (TaskGraphDefinition d : taskGraphDefinitions) {
            pointOutStageIdGraph.computeIfAbsent(d.getFromStageId(), (k) -> new HashSet<>()).add(d.getToStageId());
            pointInStageIdGraph.computeIfAbsent(d.getToStageId(), (k) -> new HashSet<>()).add(d.getFromStageId());
        }

        // 根节点，使用gset数据结构，这个是因为有可能多个节点会指向同一个父节点
        Map<Long, StageStartup> pausedTaskStageStartupStageIdMap =
                pausedTaskStageStartups.stream().collect(Collectors.toMap(StageStartup::getStageId, s -> s));
        Set<Long> layer =
                stageIdDefinition.values().stream().filter(StageDefinition::getIsStartingStage).map(StageDefinition::getId).collect(Collectors.toSet());
        HashSet<Long> resumeTaskStartingStageIds = new HashSet<>();
        while (!layer.isEmpty()) {
            HashSet<Long> nextLayer = new HashSet<>();
            for (Long stageId : layer) {
                if (stageIdDefinition.get(stageId).getIsStartingStage()) {
                    // 如果是根节点，那么直接认为其父节点是成功的。
                    if (!pausedTaskSucceedStageIds.contains(stageId)) {
                        // 如果当前这个根节点没有成功，则直接作为新的启动节点
                        resumeTaskStartingStageIds.add(stageId);
                    }
                } else {
                    // 如果不是根节点
                    if (!pausedTaskSucceedStageIds.contains(stageId) && pausedTaskSucceedStageIds.containsAll(pointInStageIdGraph.get(stageId))) {
                        // 如果当前节点未成功，并且父节点全都成功，那么就是恢复的启动stage之一
                        resumeTaskStartingStageIds.add(stageId);
                    }
                }
                if (pointOutStageIdGraph.containsKey(stageId)) {
                    nextLayer.addAll(pointOutStageIdGraph.get(stageId));
                }
            }
            layer = nextLayer;
        }


        // 创建Task和Stage的各种Startup
        TaskStartup resumeTaskStartup = new TaskStartup()
                .withTaskId(pausedTaskStartup.getTaskId())
                .withFailCount(pausedTaskStartup.getFailCount())
                .withStatus(TaskStageStatus.PENDING.getStatus())
                .withSourceType(StartupSourceType.TASK_RESUME.getValue())
                .withSourceId(pausedTaskStartup.getId());
        taskStartupService.create(resumeTaskStartup);

        TaskSharedContext resumeTaskSharedContext = TaskSharedContext.builder().taskStartupId(resumeTaskStartup.getId())
                .encodedTaskSharedContext(latestEncodedSharedContext).build();
        taskSharedContextService.create(resumeTaskSharedContext);
        String resumeTaskWorkerAddr = taskScheduler.assignTaskToWorkerAddress(taskDefinition.getName(), taskDefinition.getVersion());
        if (resumeTaskWorkerAddr == null) {
            // todo: 这个可以通过mq来改造，从而实现一个暂时没有被成功恢复的任务可以再次被尝试恢复。
            log.error("未找到能够恢复继续执行taskStartupId:{}的节点，恢复失败", pausedTaskStartup.getId());
            return;
        }
        TaskExecution resumeTaskExecution = new TaskExecution().withAssignedWorkerAddr(resumeTaskWorkerAddr)
                .withTaskStartupId(resumeTaskStartup.getId())
                .withStatus(TaskStageStatus.PENDING.getStatus());
        taskExecutionService.create(resumeTaskExecution);
        Map<String, Long> resumeTaskStartingStageNameExecutionIds = new HashMap<>();
        Map<String, Integer> resumeTaskStartingStageNameFailedCount = new HashMap<>();
        for (StageDefinition stageD : stageIdDefinition.values()) {
            if (pausedTaskSucceedStageIds.contains(stageD.getId())) {
                // 如果已经成功，就不用创建了startup对象了
                continue;
            }
            Integer stageFailCount = pausedTaskStageStartupStageIdMap.get(stageD.getId()).getFailCount();
            StageStartup stageStartup = new StageStartup()
                    .withTaskExecutionId(resumeTaskExecution.getId())
                    .withStageId(stageD.getId())
                    .withStatus(TaskStageStatus.PENDING.getStatus())
                    .withFailCount(stageFailCount);
            stageStartupService.create(stageStartup);

            if (resumeTaskStartingStageIds.contains(stageD.getId())) {
                // 与prepareTask一样，启动task之前至少需要将启动节点的execturiong对象准备好
                StageExecution stageExecution = new StageExecution()
                        .withStageStartupId(stageStartup.getId())
                        .withWorkerAddress(resumeTaskWorkerAddr)
                        .withStatus(TaskStageStatus.PENDING.getStatus());
                stageExecutionService.create(stageExecution);

                resumeTaskStartingStageNameExecutionIds.put(stageD.getName(), stageExecution.getId());
                resumeTaskStartingStageNameFailedCount.put(stageD.getName(), stageFailCount);
            }

            if (stageNameEncodedInputs.containsKey(stageD.getName()) || resumeTaskStartingStageIds.contains(stageD.getId())) {
                // 同样的，与prepareTask一样，如果有输入，就需要记录，如果是启动节点，也要记录
                String encodedInput = stageNameEncodedInputs.get(stageD.getName());
                stageStartupParamService.create(
                        StageStartupParam.builder().stageStartupId(stageStartup.getId())
                                .encodedInput(encodedInput)
                                .encodedSharedContextSnapshotAtStartup(latestEncodedSharedContext)
                                .updateTime(new Date())
                                .build());
            }
        }


        WorkerResumeTaskReq workerResumeTaskReq = new WorkerResumeTaskReq();
        WorkerResumeTaskReq.Data data = new WorkerResumeTaskReq.Data();
        data.setTaskName(taskDefinition.getName());
        data.setTaskVersion(taskDefinition.getVersion());
        data.setTaskExecutionId(resumeTaskExecution.getId());
        data.setSucceedStageNames(pausedTaskSucceedStageIds.stream().map(id -> stageIdDefinition.get(id).getName()).collect(Collectors.toSet()));
        data.setStageEncodedInputs(stageNameEncodedInputs);
        data.setInitialEncodedSharedContext(latestEncodedSharedContext);
        data.setStartingStageExecutionId(resumeTaskStartingStageNameExecutionIds);
        data.setTaskFailedCount(resumeTaskStartup.getFailCount());
        data.setStageFailedCount(resumeTaskStartingStageNameFailedCount);
        workerResumeTaskReq.setData(data);


        WorkerResumeTaskResp workerResumeTaskResp =
                workerTaskDriverClient.resumeTask(WorkerClusterManager.getWorkerURI(resumeTaskWorkerAddr), workerResumeTaskReq);
        VerifyUtil.requireTrue(workerResumeTaskResp.isSuccess(),
                String.format("恢复任务（taskExecutionId: %d）失败", pausedTaskExecutionId));

    }

    public void terminateTask(long taskExecutionId) {
        TaskExecution taskExecution = taskExecutionServiceWrapper.selectById(taskExecutionId);
        taskExecution.setStatus(TaskStageStatus.TERMINATED.getStatus());


        taskExecutionServiceWrapper.updateSelectiveById(taskExecutionId, taskExecution, taskExecution.getRevision());
    }

}
