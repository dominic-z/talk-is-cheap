package org.talk.is.cheap.project.free.flow.starter.worker.task.definition.service;

import com.google.common.collect.Sets;
import io.vavr.Tuple2;
import io.vavr.control.Option;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.talk.is.cheap.project.free.flow.common.exception.IllegalTaskDefinitionException;
import org.talk.is.cheap.project.free.flow.common.message.impl.vo.StageDefinitionVO;
import org.talk.is.cheap.project.free.flow.common.message.impl.vo.TaskDefinitionVO;
import org.talk.is.cheap.project.free.flow.common.task.definition.bo.StageDefinitionBO;
import org.talk.is.cheap.project.free.flow.common.task.definition.bo.TaskDefinitionBO;
import org.talk.is.cheap.project.free.flow.common.task.definition.codec.InputCodec;
import org.talk.is.cheap.project.free.flow.starter.worker.cluster.event.WorkerRegisteredEvent;
import org.talk.is.cheap.project.free.flow.starter.worker.task.definition.annotaion.stage.RunnableStage;
import org.talk.is.cheap.project.free.flow.starter.worker.task.definition.annotaion.task.Task;

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
public class LocalTaskDefinitionManager {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private RemoteTaskDefinitionService remoteTaskDefinitionService;

    private final Map<String, TaskDefinitionBO> taskDefinitionBOMap = new HashMap<>();

    private static void shallBeTrue(boolean bool, String errorMsg) throws IllegalTaskDefinitionException {
        if (!bool) {
            throw new IllegalTaskDefinitionException(errorMsg);
        }
    }

    private static void shallBeFalse(boolean bool, String errorMsg) throws IllegalTaskDefinitionException {
        shallBeTrue(!bool, errorMsg);
    }

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
    @EventListener(WorkerRegisteredEvent.class)
    public void prepareAndValidateTaskDefinition(WorkerRegisteredEvent workerRegisteredEvent) throws IllegalTaskDefinitionException {
        Map<String, Object> taskBeans = applicationContext.getBeansWithAnnotation(Task.class);

        for (String beanName : taskBeans.keySet()) {
            Object taskBean = taskBeans.get(beanName);
            TaskDefinitionBO taskDefinitionBO = getAndValidateTaskDefinitionBO(taskBean);

            shallBeFalse(taskDefinitionBOMap.containsKey(taskDefinitionBO.getName()),
                    String.format("Found a task name that is duplicated: %s", taskDefinitionBO.getName()));
            taskDefinitionBOMap.put(taskDefinitionBO.getName(), taskDefinitionBO);
        }


    }

    /**
     * 解析@Task标注的bean，验证有效性并转化为TaskDefinitionBO对象
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
        int timeoutInSeconds = taskAnnotation.timeout();

        shallBeFalse(StringUtils.isBlank(taskName), String.format("taskName can't be blank, class: %s", taskClass.getName()));

        TaskDefinitionBO taskDefinitionBO = TaskDefinitionBO.builder()
                .name(taskName)
                .version(version)
                .maxRetryCount(maxRetryCount)
                .timeoutInSecond(timeoutInSeconds).build();

        parseStageMethodAndSetStageDefinitionBOs(taskDefinitionBO, taskClass.getDeclaredMethods());


        // 通过dfs来确定连通性和是否有环
        validateCircleAndConnected(taskDefinitionBO);

        // 对比本地和远端的task定义是否匹配
        checkLocalRemoteTaskDefinitionMatch();
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

                shallBeFalse(StringUtils.isBlank(stageName), String.format("stageName can't be blank, method: %s", method.getName()));


                int stageVersion = runnableStageAnno.version();
                boolean startingStage = runnableStageAnno.isStartingStage();
                Class<? extends InputCodec<?>> inputCodecClass = runnableStageAnno.inputCodecClass();
                String[] toStageNames = runnableStageAnno.toStageName();
                int maxRetryCount = runnableStageAnno.maxRetryCount();
                int timeout = runnableStageAnno.timeout();
                String methodName = method.getName();
                Parameter[] parameters = method.getParameters();

                stageDefinitionBO = StageDefinitionBO.builder()
                        .name(stageName)
                        .version(stageVersion)
                        .isStartingStage(startingStage)
                        .inputCodecClass(inputCodecClass)
                        .toStageNames(Set.of(toStageNames))
                        .maxRetryCount(maxRetryCount)
                        .timeout(timeout)
                        .methodName(methodName)
                        .parameters(parameters).build();

            }

            if (stageDefinitionBO != null) {

                shallBeFalse(taskDefinitionBO.getStageDefinitionBOMap().containsKey(stageDefinitionBO.getName()),
                        String.format("Found a stage name that is duplicated: %s", stageDefinitionBO.getName()));

                taskDefinitionBO.getStageDefinitionBOMap().put(stageDefinitionBO.getName(), stageDefinitionBO);
                if (stageDefinitionBO.getIsStartingStage()) {
                    taskDefinitionBO.getRoots().add(stageDefinitionBO.getName());
                }

                taskDefinitionBO.getPointOutGraph().put(stageDefinitionBO.getName(), stageDefinitionBO.getToStageNames());
            }
        }


        // 构建graph以及stageDefinitionBO的fromStageMap
        for (String fromStageName : taskDefinitionBO.getPointOutGraph().keySet()) {
            Set<String> toStageNames = taskDefinitionBO.getPointOutGraph().get(fromStageName);
            for (String toStageName : toStageNames) {

                shallBeTrue(taskDefinitionBO.getStageDefinitionBOMap().containsKey(toStageName),
                        String.format("Undefined stage name found: %s", toStageName));

                taskDefinitionBO.getStageDefinitionBOMap().get(toStageName).getFromStageNames().add(fromStageName);
            }
        }

        shallBeFalse(taskDefinitionBO.getRoots().isEmpty(),
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
        for (String root : taskDefinitionBO.getRoots()) {
            validateCircle(root, taskDefinitionBO, new LinkedHashSet<>(), connectedStages);
        }


        // 图是否是连通的
        shallBeFalse(
                connectedStages.size() != taskDefinitionBO.getStageDefinitionBOMap().size() || connectedStages.containsAll(taskDefinitionBO.getStageDefinitionBOMap().keySet()),
                String.format("There are unreachable stages: %s",
                        String.join(",", Sets.difference(taskDefinitionBO.getStageDefinitionBOMap().keySet(), connectedStages))));
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
        Set<String> toStageNames = taskDefinitionBO.getStageDefinitionBOMap().get(currentStageName).getToStageNames();
        if (toStageNames.isEmpty()) {
            return;
        }
        dfsPath.add(currentStageName);
        for (String toStageName : toStageNames) {
            shallBeFalse(dfsPath.contains(toStageName),
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
                List<TaskDefinitionVO> taskDefinitionVOs = remoteTaskDefinitionService.getTaskDefinitionVOs(taskNameVersions);

                for (TaskDefinitionVO remoteVO : taskDefinitionVOs) {
                    checkLocalAndRemoteTaskDefinitionMatch(taskDefinitionBOMap.get(remoteVO.getName()), remoteVO);
                }
            }
        }
    }

    // 对比已经存在的task的定义与本身自己的task定义
    private void checkLocalAndRemoteTaskDefinitionMatch(TaskDefinitionBO localBO, TaskDefinitionVO remoteVO) throws IllegalTaskDefinitionException {
        String taskName = localBO.getName();
        shallBeFalse(remoteVO.getRoots().size() != localBO.getRoots().size() || !remoteVO.getRoots().containsAll(localBO.getRoots()),
                String.format("Conflicts with the task definition of the remote end. root nodes are not equal.task: %s", taskName));

        // 比较俩包装类型是否相等的稍微简单点的方法
        shallBeTrue(Option.of(remoteVO.getMaxRetryCount()).equals(Option.of(localBO.getMaxRetryCount())),
                String.format("Conflicts with the task definition of the remote end. max retry count are not equal.task: %s", taskName));

        shallBeTrue(Option.of(remoteVO.getTimeout()).equals(Option.of(localBO.getTimeoutInSecond())),
                String.format("Conflicts with the task definition of the remote end. timeout are not equal.task: %s", taskName));

        Map<String, Set<String>> remoteGraph = remoteVO.getPointOutGraph();
        Map<String, Set<String>> localGraph = localBO.getPointOutGraph();
        shallBeFalse(remoteGraph.size() != localGraph.size() || !remoteGraph.keySet().containsAll(localGraph.keySet()),
                String.format("Conflicts with the task definition of the remote end. graph keys are not equal.task: %s", taskName));

        // 利用bfs来比较两个graph是否相等，bfs写的比较简单
        LinkedList<String> deque = new LinkedList<>(remoteVO.getRoots());
        Set<String> visitedStageNames = new HashSet<>(remoteVO.getRoots());
        while (!deque.isEmpty()) {
            String stageName = deque.removeFirst();

            shallBeTrue(remoteVO.getStageDefinitionVOMap().containsKey(stageName) && localBO.getStageDefinitionBOMap().containsKey(stageName),
                    String.format("""
                                    Conflicts with the task definition of the remote end.
                                    Found a stage that exists only in either the remote end or the local end. stage name: %s""",
                            stageName));

            StageDefinitionVO remoteStageVO = remoteVO.getStageDefinitionVOMap().get(stageName);
            StageDefinitionBO localStageBO = localBO.getStageDefinitionBOMap().get(stageName);

            shallBeTrue(remoteStageVO.getVersion().equals(localStageBO.getVersion()),
                    String.format("""
                                    Conflicts with the task definition of the remote end.
                                    stage version conflicts with the same task version. stage name: %s""",
                            stageName));


            shallBeTrue(Option.of(remoteStageVO.getMaxRetryCount()).equals(Option.of(localStageBO.getMaxRetryCount())),
                    String.format("""
                                    Conflicts with the task definition of the remote end.
                                    max retry count is not equal. stage name: %s""",
                            stageName));

            shallBeTrue(Option.of(remoteStageVO.getTimeout()).equals(Option.of(localStageBO.getTimeout())),
                    String.format("""
                                    Conflicts with the task definition of the remote end.
                                    timeout is not equal. stage name: %s""",
                            stageName));

            Set<String> remoteStageChildren = remoteGraph.get(stageName);
            Set<String> localStageChildren = localGraph.get(stageName);

            shallBeTrue(remoteStageChildren != null && localStageChildren != null
                            && remoteStageChildren.size() != localStageChildren.size() && remoteStageChildren.containsAll(localStageChildren),
                    String.format("Conflicts with the task definition of the remote end. stage children are not equal.stage name: %s",
                            stageName));

            visitedStageNames.add(stageName);
            remoteStageChildren.stream()
                    .filter(child -> !visitedStageNames.contains(child))
                    .forEach(deque::addLast);
        }
    }


}
