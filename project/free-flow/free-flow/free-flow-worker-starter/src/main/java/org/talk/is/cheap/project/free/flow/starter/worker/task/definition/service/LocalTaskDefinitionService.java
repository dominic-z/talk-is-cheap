package org.talk.is.cheap.project.free.flow.starter.worker.task.definition.service;

import com.google.common.collect.Sets;
import io.vavr.Tuple2;
import io.vavr.control.Option;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.talk.is.cheap.project.free.flow.common.enums.StageType;
import org.talk.is.cheap.project.free.flow.common.exception.IllegalTaskDefinitionException;
import org.talk.is.cheap.project.free.flow.common.message.impl.dto.StageDefinitionDTO;
import org.talk.is.cheap.project.free.flow.common.message.impl.dto.TaskDefinitionDTO;
import org.talk.is.cheap.project.free.flow.common.task.codec.InputCodec;
import org.talk.is.cheap.project.free.flow.common.task.definition.bo.StageDefinitionBO;
import org.talk.is.cheap.project.free.flow.common.task.definition.bo.TaskDefinitionBO;
import org.talk.is.cheap.project.free.flow.common.utils.ReflectUtil;
import org.talk.is.cheap.project.free.flow.common.utils.VerifyUtil;
import org.talk.is.cheap.project.free.flow.starter.worker.task.definition.annotaion.stage.RunnableStage;
import org.talk.is.cheap.project.free.flow.starter.worker.task.definition.annotaion.task.Task;
import org.talk.is.cheap.project.free.flow.starter.worker.task.driver.runtime.StageRuntimeEnv;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * 本地(当前应用）的task的管理器
 */
@Slf4j
@Service
public class LocalTaskDefinitionService {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private RemoteTaskDefinitionService remoteTaskDefinitionService;

    private final Map<String, TaskDefinitionBO> taskDefinitionBOMap = new HashMap<>();


    /**
     * 校验task定义，要求：
     * 1. 无环
     * 2. 有明确的根节点
     * 3. 每个stage的toStage都有效
     * 4. 图是连通的，没有悬挂的节点
     * 5. 是否有版本冲突，例如相同的task与version，但下面的stage构造不同
     *
     * @throws IllegalTaskDefinitionException
     */
    public void prepareAndValidateTaskDefinition() throws IllegalTaskDefinitionException {
        Map<String, Object> taskBeans = applicationContext.getBeansWithAnnotation(Task.class);

        for (String beanName : taskBeans.keySet()) {
            Object taskBean = taskBeans.get(beanName);
            TaskDefinitionBO taskDefinitionBO = getAndValidateTaskDefinitionBO(taskBean);

            VerifyUtil.requireFalse(taskDefinitionBOMap.containsKey(taskDefinitionBO.getName()),
                    String.format("Found a task name that is duplicated: %s", taskDefinitionBO.getName()));
            taskDefinitionBOMap.put(taskDefinitionBO.getName(), taskDefinitionBO);
        }

        // 批量对比本地和远端的task定义是否匹配，等所有task定义读取完之后再统一对比，否则每个任务就都要请求一次scheduler
        checkLocalRemoteTaskDefinitionMatch();


    }

    /**
     * 解析@Task标注的bean，！充分验证有效性！并转化为TaskDefinitionBO对象
     *
     * @param taskBean
     * @return
     * @throws IllegalTaskDefinitionException
     */
    private TaskDefinitionBO getAndValidateTaskDefinitionBO(Object taskBean) throws IllegalTaskDefinitionException {
        Class<?> taskClass = taskBean.getClass();
        Task taskAnnotation = taskClass.getAnnotation(Task.class);
        String taskName = taskAnnotation.name();
        int version = taskAnnotation.version();
        int maxRetryCount = taskAnnotation.maxRetryCount();
        Class<? extends InputCodec<?>> sharedContextCodecClass = taskAnnotation.sharedContextCodecClass();
        Class<?> sharedConextClass = ReflectUtil.getCodecGenericClass(sharedContextCodecClass);


        int timeoutInSeconds = taskAnnotation.timeout();

        VerifyUtil.requireFalse(StringUtils.isBlank(taskName), String.format("taskName can't be blank, class: %s", taskClass.getName()));

        TaskDefinitionBO taskDefinitionBO = TaskDefinitionBO.builder()
                .name(taskName)
                .version(version)
                .maxRetryCount(maxRetryCount)
                .timeoutInSecond(timeoutInSeconds)
                .sharedContextCodecClass(sharedContextCodecClass)
                .sharedContextClass(sharedConextClass)
                .taskClass(taskClass)
                .build();

        parseStageMethodAndSetStageDefinitionBOs(taskDefinitionBO, taskClass.getDeclaredMethods());


        // 通过dfs来确定连通性和是否有环
        validateCircleAndConnected(taskDefinitionBO);


        return taskDefinitionBO;
    }

    /**
     * 解析方法定义，并获得StageDefinitionBO对象
     *
     * @param taskDefinitionBO
     * @param declaredMethods
     * @throws IllegalTaskDefinitionException
     */
    private static void parseStageMethodAndSetStageDefinitionBOs(TaskDefinitionBO taskDefinitionBO, Method[] declaredMethods) throws IllegalTaskDefinitionException {
        for (Method method : declaredMethods) {
            Annotation[] annotations = method.getAnnotations();
            if (annotations == null) {
                continue;
            }
            StageDefinitionBO stageDefinitionBO = null;
            RunnableStage runnableStageAnno = method.getAnnotation(RunnableStage.class);
            if (runnableStageAnno != null) {
                String stageName = runnableStageAnno.name();

                VerifyUtil.requireFalse(StringUtils.isBlank(stageName), String.format("stageName can't be blank, method: %s",
                        method.getName()));


                int stageVersion = runnableStageAnno.version();
                boolean startingStage = runnableStageAnno.isStartingStage();
                Class<? extends InputCodec<?>> inputCodecClass = runnableStageAnno.inputCodecClass();
                String[] toStageNames = runnableStageAnno.toStageName();
                int maxRetryCount = runnableStageAnno.maxRetryCount();
                int timeout = runnableStageAnno.timeoutInSecond();
                String methodName = method.getName();

                Parameter[] parameters = method.getParameters();
                VerifyUtil.requireTrue((method.getParameters().length == 0) || (method.getParameters().length == 1 && parameters[0].getType() == StageRuntimeEnv.class),
                        String.format("阶段%s解析异常，方法入参能能是空或者之多一个StageRuntimeEnv类型，并且如果无参数，输入对象编解码器也应当为空", stageName));
                Class<?> inputClass = null;
                if (parameters.length != 0) {
                    Parameter parameter = parameters[0];
                    inputClass = ReflectUtil.getCodecGenericClass(inputCodecClass);
                }

                stageDefinitionBO = StageDefinitionBO.builder()
                        .name(stageName)
                        .version(stageVersion)
                        .stageType(StageType.RUNNABLE)
                        .isStartingStage(startingStage)
                        .inputCodecClass(inputCodecClass)
                        .inputClass(inputClass)
                        .toStageNames(Set.of(toStageNames))
                        .maxRetryCount(maxRetryCount)
                        .timeout(timeout)
                        .methodName(methodName)
                        .parameters(parameters)
                        .build();

            }

            if (stageDefinitionBO != null) {

                VerifyUtil.requireFalse(taskDefinitionBO.getStageDefinitionMap().containsKey(stageDefinitionBO.getName()),
                        String.format("Found a stage name that is duplicated: %s in task: %s", stageDefinitionBO.getName(),
                                stageDefinitionBO.getName()));

                taskDefinitionBO.getStageDefinitionMap().put(stageDefinitionBO.getName(), stageDefinitionBO);
                if (stageDefinitionBO.getIsStartingStage()) {
                    taskDefinitionBO.getStartingStageNames().add(stageDefinitionBO.getName());
                }

                taskDefinitionBO.getPointOutGraph().put(stageDefinitionBO.getName(), stageDefinitionBO.getToStageNames());
                for (String toStageName : stageDefinitionBO.getToStageNames()) {
                    taskDefinitionBO.getPointInGraph().computeIfAbsent(toStageName, (v) -> new HashSet<>()).add(stageDefinitionBO.getName());
                }
            }
        }


        // 构建graph以及stageDefinitionBO的fromStageMap
        for (String fromStageName : taskDefinitionBO.getPointOutGraph().keySet()) {
            Set<String> toStageNames = taskDefinitionBO.getPointOutGraph().get(fromStageName);
            for (String toStageName : toStageNames) {

                VerifyUtil.requireTrue(taskDefinitionBO.getStageDefinitionMap().containsKey(toStageName),
                        String.format("Undefined stage name found: %s", toStageName));

                taskDefinitionBO.getStageDefinitionMap().get(toStageName).getFromStageNames().add(fromStageName);
            }
        }

        VerifyUtil.requireFalse(taskDefinitionBO.getStartingStageNames().isEmpty(),
                String.format("Root stage not found, taskName: %s", taskDefinitionBO.getName()));
    }


    /**
     * 图是无环的并且是连通的
     *
     * @param taskDefinitionBO task定义，取其中的graph作为图结构
     * @throws IllegalTaskDefinitionException
     */
    private void validateCircleAndConnected(TaskDefinitionBO taskDefinitionBO) throws IllegalTaskDefinitionException {
        Set<String> connectedStages = new HashSet<>();
        for (String root : taskDefinitionBO.getStartingStageNames()) {
            validateCircle(root, taskDefinitionBO, new LinkedHashSet<>(), connectedStages);
        }


        // 图是否是连通的
        VerifyUtil.requireTrue(
                connectedStages.size() == taskDefinitionBO.getStageDefinitionMap().size() && connectedStages.containsAll(taskDefinitionBO.getStageDefinitionMap().keySet()),
                String.format("There are unreachable stages: %s in task: %s",
                        String.join(",", Sets.difference(taskDefinitionBO.getStageDefinitionMap().keySet(), connectedStages)),
                        taskDefinitionBO.getName()));
    }


    /**
     * 通过dfs判断是否有环
     *
     * @param currentStageName 当前访问的stage
     * @param taskDefinitionBO task定义，取其中的graph
     * @param dfsPath          当前dfs中的路径记录，相当于递归调用栈
     * @param connectedStages  目前为止已经访问过的stage
     * @throws IllegalTaskDefinitionException
     */
    private void validateCircle(String currentStageName, TaskDefinitionBO taskDefinitionBO, LinkedHashSet<String> dfsPath,
                                Set<String> connectedStages) throws IllegalTaskDefinitionException {
        connectedStages.add(currentStageName);
        Set<String> toStageNames = taskDefinitionBO.getStageDefinitionMap().get(currentStageName).getToStageNames();
        if (toStageNames.isEmpty()) {
            return;
        }
        dfsPath.add(currentStageName);
        for (String toStageName : toStageNames) {
            VerifyUtil.requireFalse(dfsPath.contains(toStageName),
                    String.format("there is a circle in task %s : %s", taskDefinitionBO.getName(), String.join("->", dfsPath)));
            validateCircle(toStageName, taskDefinitionBO, dfsPath, connectedStages);
        }
        dfsPath.remove(currentStageName);
    }


    /**
     * 对比本地和远程的task定义是否一致
     *
     * @throws IllegalTaskDefinitionException
     */
    private void checkLocalRemoteTaskDefinitionMatch() throws IllegalTaskDefinitionException {
        final int batchSize = 20;


        List<Tuple2<String, Integer>> taskNameVersions = new ArrayList<>();
        Iterator<TaskDefinitionBO> iterator = taskDefinitionBOMap.values().iterator();
        while (iterator.hasNext()) {
            TaskDefinitionBO taskDefinitionBO = iterator.next();
            taskNameVersions.add(new Tuple2<>(taskDefinitionBO.getName(), taskDefinitionBO.getVersion()));
            if (!iterator.hasNext() || taskNameVersions.size() == batchSize) {
                List<TaskDefinitionDTO> taskDefinitionDTOs = remoteTaskDefinitionService.getTaskDefinitionDTOs(taskNameVersions);

                for (TaskDefinitionDTO remoteVO : taskDefinitionDTOs) {
                    checkLocalAndRemoteTaskDefinitionMatch(taskDefinitionBOMap.get(remoteVO.getName()), remoteVO);
                }
            }
        }
    }

    // 对比已经存在的task的定义与本身自己的task定义
    private void checkLocalAndRemoteTaskDefinitionMatch(TaskDefinitionBO localBO, TaskDefinitionDTO remoteDTO) throws IllegalTaskDefinitionException {
        String taskName = localBO.getName();

        VerifyUtil.requireTrue(StringUtils.equals(localBO.getSharedContextClass().getName(),
                        remoteDTO.getSharedContextFullyQualifiedClassName()),
                String.format("本地与远端同一个版本（任务：%s，版本：%s）的任务定义的共享上下文全限定名不一致，如任务更新需升级版本号", localBO.getName(), localBO.getVersion()));
        VerifyUtil.requireTrue(StringUtils.equals(localBO.getSharedContextCodecClass().getName(),
                        remoteDTO.getSharedContextCodecFullyQualifiedClassName()),
                String.format("本地与远端同一个版本（任务：%s，版本：%s）的任务定义的共享上下文的解析类不一致，如任务更新需升级版本号", localBO.getName(), localBO.getVersion()));


        // 比较俩包装类型是否相等的稍微简单点的方法
        VerifyUtil.requireTrue(Option.of(remoteDTO.getMaxRetryCount()).equals(Option.of(localBO.getMaxRetryCount())),
                String.format("Conflicts with the task definition of the remote end. max retry count are not equal.task: %s", taskName));

        VerifyUtil.requireTrue(Option.of(remoteDTO.getTimeout()).equals(Option.of(localBO.getTimeoutInSecond())),
                String.format("Conflicts with the task definition of the remote end. timeout are not equal.task: %s", taskName));


        Map<String, Set<String>> remoteGraph = remoteDTO.getPointOutGraph();
        Map<String, Set<String>> localGraph = localBO.getPointOutGraph();
        VerifyUtil.requireFalse(remoteGraph.size() != localGraph.size() || !remoteGraph.keySet().containsAll(localGraph.keySet()),
                String.format("Conflicts with the task definition of the remote end. graph keys are not equal.task: %s", taskName));

        // 利用bfs来比较两个graph是否相等，bfs写的比较简单
        VerifyUtil.requireFalse(remoteDTO.getRoots().size() != localBO.getStartingStageNames().size() || !remoteDTO.getRoots().containsAll(localBO.getStartingStageNames()),
                String.format("Conflicts with the task definition of the remote end. root nodes are not equal.task: %s", taskName));

        LinkedList<String> deque = new LinkedList<>(remoteDTO.getRoots());
        Set<String> visitedStageNames = new HashSet<>(remoteDTO.getRoots());
        while (!deque.isEmpty()) {
            String stageName = deque.removeFirst();

            VerifyUtil.requireTrue(remoteDTO.getStageDefinitionMap().containsKey(stageName) && localBO.getStageDefinitionMap().containsKey(stageName),
                    String.format("""
                                    Conflicts with the task definition of the remote end.
                                    Found a stage that exists only in either the remote end or the local end. stage name: %s""",
                            stageName));

            VerifyUtil.requireTrue(
                    (localBO.getStageDefinitionMap().get(stageName).getInputClass() == null &&
                            remoteDTO.getStageDefinitionMap().get(stageName).getInputFullyQualifiedClassName() == null)
                            ||
                            (localBO.getStageDefinitionMap().get(stageName).getInputClass() != null &&
                                    remoteDTO.getStageDefinitionMap().get(stageName).getInputFullyQualifiedClassName() != null
                                    && StringUtils.equals(localBO.getStageDefinitionMap().get(stageName).getInputClass().getName(),
                                    remoteDTO.getStageDefinitionMap().get(stageName).getInputFullyQualifiedClassName())
                            )
                    ,
                    String.format("本地与远端同一个版本（任务：%s，版本：%s，阶段：%s）的阶段定义的入参不一致，如任务更新需升级版本号", localBO.getName(), localBO.getVersion(),
                            stageName));
            VerifyUtil.requireTrue(
                    (localBO.getStageDefinitionMap().get(stageName).getInputCodecClass() == null &&
                            remoteDTO.getStageDefinitionMap().get(stageName).getInputCodecFullyQualifiedClassName() == null)
                            ||
                            (localBO.getStageDefinitionMap().get(stageName).getInputCodecClass() != null &&
                                    remoteDTO.getStageDefinitionMap().get(stageName).getInputCodecFullyQualifiedClassName() != null
                                    && StringUtils.equals(localBO.getStageDefinitionMap().get(stageName).getInputCodecClass().getName(),
                                    remoteDTO.getStageDefinitionMap().get(stageName).getInputCodecFullyQualifiedClassName())
                            )
                    ,
                    String.format("本地与远端同一个版本（任务：%s，版本：%s，阶段：%s）的阶段定义的入参的解析类不一致，如任务更新需升级版本号", localBO.getName(), localBO.getVersion(),
                            stageName));


            StageDefinitionDTO remoteStageDTO = remoteDTO.getStageDefinitionMap().get(stageName);
            StageDefinitionBO localStageBO = localBO.getStageDefinitionMap().get(stageName);

            VerifyUtil.requireTrue(remoteStageDTO.getVersion().equals(localStageBO.getVersion()),
                    String.format("""
                                    Conflicts with the task definition of the remote end.
                                    stage version conflicts with the same task version. stage name: %s""",
                            stageName));


            VerifyUtil.requireTrue(Option.of(remoteStageDTO.getMaxRetryCount()).equals(Option.of(localStageBO.getMaxRetryCount())),
                    String.format("""
                                    Conflicts with the task definition of the remote end.
                                    max retry count is not equal. stage name: %s""",
                            stageName));

            VerifyUtil.requireTrue(Option.of(remoteStageDTO.getTimeout()).equals(Option.of(localStageBO.getTimeout())),
                    String.format("""
                                    Conflicts with the task definition of the remote end.
                                    timeout is not equal. stage name: %s""",
                            stageName));

            Set<String> remoteStageChildren = remoteGraph.get(stageName);
            Set<String> localStageChildren = localGraph.get(stageName);

            VerifyUtil.requireTrue(remoteStageChildren != null && localStageChildren != null
                            && remoteStageChildren.size() == localStageChildren.size() && remoteStageChildren.containsAll(localStageChildren),
                    String.format("Conflicts with the task definition of the remote end. stage children are not equal.stage name: %s",
                            stageName));

            visitedStageNames.add(stageName);
            remoteStageChildren.stream()
                    .filter(child -> !visitedStageNames.contains(child))
                    .forEach(deque::addLast);
        }
    }


    /**
     * 以下是针对taskDefinitionBOMap的一些get方法，避免这个map暴露出去
     */

    /**
     * 是否具备某个name的task
     *
     * @param taskName
     * @param version  如果为null，则不关心版本
     * @return
     */
    public boolean hasTask(String taskName, Integer version) {
        if (!taskDefinitionBOMap.containsKey(taskName)) {
            return false;
        }
        if (version == null) {
            return true;
        } else {
            return taskDefinitionBOMap.get(taskName).getVersion().equals(version);
        }
    }


    public Set<String> getTaskNames() {
        return taskDefinitionBOMap.keySet();
    }

    public TaskDefinitionBO getTaskDefinitionBO(String name) {
        return taskDefinitionBOMap.get(name);
    }

}
