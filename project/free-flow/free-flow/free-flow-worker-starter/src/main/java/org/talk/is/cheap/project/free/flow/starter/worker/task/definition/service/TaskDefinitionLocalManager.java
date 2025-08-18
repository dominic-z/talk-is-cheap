package org.talk.is.cheap.project.free.flow.starter.worker.task.definition.service;

import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.talk.is.cheap.project.free.flow.common.task.definition.codec.InputCodec;
import org.talk.is.cheap.project.free.flow.common.utils.VerifyUtil;
import org.talk.is.cheap.project.free.flow.starter.worker.task.definition.annotaion.stage.RunnableStage;
import org.talk.is.cheap.project.free.flow.starter.worker.task.definition.annotaion.task.Task;
import org.talk.is.cheap.project.free.flow.common.task.definition.bo.StageDefinitionBO;
import org.talk.is.cheap.project.free.flow.common.task.definition.bo.TaskDefinitionBO;
import org.talk.is.cheap.project.free.flow.common.exception.IllegalTaskDefinitionException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * 本地(当前应用）的task的管理器
 */
@Slf4j
@Service
public class TaskDefinitionLocalManager {

    @Autowired
    private ApplicationContext applicationContext;

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
    public void validateTaskDefinition() throws IllegalTaskDefinitionException {
        Map<String, Object> taskBeans = applicationContext.getBeansWithAnnotation(Task.class);

        for (String beanName : taskBeans.keySet()) {
            Object taskBean = taskBeans.get(beanName);
            TaskDefinitionBO taskDefinitionBO = getAndValidateTaskDefinitionBO(taskBean);

            if (taskDefinitionBOMap.containsKey(taskDefinitionBO.getName())) {
                throw new IllegalTaskDefinitionException(String.format("Found a task name that is duplicated: %s", taskDefinitionBO.getName()));
            }
            taskDefinitionBOMap.put(taskDefinitionBO.getName(), taskDefinitionBO);
        }

    }

    /**
     * 解析@Task标注的bean，验证有效性并转化为TaskDefinitionBO对象
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
        int timeoutInSeconds = taskAnnotation.timeoutInSeconds();

        if (StringUtils.isBlank(taskName)) {
            throw new IllegalTaskDefinitionException(String.format("taskName can't be blank, class: %s", taskClass.getName()));
        }

        TaskDefinitionBO taskDefinitionBO = TaskDefinitionBO.builder()
                .name(taskName)
                .version(version)
                .maxRetryCount(maxRetryCount)
                .timeoutInSecond(timeoutInSeconds).build();

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
                if (StringUtils.isBlank(stageName)) {
                    throw new IllegalTaskDefinitionException(String.format("stageName can't be blank, method: %s", method.getName()));
                }


                int stageVersion = runnableStageAnno.version();
                boolean startingStage = runnableStageAnno.isStartingStage();
                Class<? extends InputCodec<?>> inputCodecClass = runnableStageAnno.inputCodecClass();
                String[] toStageNames = runnableStageAnno.toStageName();
                String methodName = method.getName();
                Parameter[] parameters = method.getParameters();

                stageDefinitionBO = StageDefinitionBO.builder()
                        .name(stageName)
                        .version(stageVersion)
                        .isStartingStage(startingStage)
                        .inputCodecClass(inputCodecClass)
                        .toStageNames(List.of(toStageNames))
                        .methodName(methodName)
                        .parameters(parameters).build();

            }

            if (stageDefinitionBO != null) {
                if (taskDefinitionBO.getStageDefinitionBOMap().containsKey(stageDefinitionBO.getName())) {
                    throw new IllegalTaskDefinitionException(String.format("Found a stage name that is duplicated: %s",
                            stageDefinitionBO.getName()));
                }
                taskDefinitionBO.getStageDefinitionBOMap().put(stageDefinitionBO.getName(), stageDefinitionBO);
                if (stageDefinitionBO.getIsStartingStage()) {
                    taskDefinitionBO.getRoots().add(stageDefinitionBO.getName());
                }

                taskDefinitionBO.getPointOutGraph().put(stageDefinitionBO.getName(), stageDefinitionBO.getToStageNames());
            }
        }


        // 构建graph以及stageDefinitionBO的fromStageMap
        for (String fromStageName : taskDefinitionBO.getPointOutGraph().keySet()) {
            List<String> toStageNames = taskDefinitionBO.getPointOutGraph().get(fromStageName);
            for (String toStageName : toStageNames) {
                if (!taskDefinitionBO.getStageDefinitionBOMap().containsKey(toStageName)) {
                    throw new IllegalTaskDefinitionException(String.format("Undefined stage name found: %s", toStageName));
                }
                taskDefinitionBO.getStageDefinitionBOMap().get(toStageName).getFromStageNames().add(fromStageName);
            }
        }

        if (taskDefinitionBO.getRoots().isEmpty()) {
            throw new IllegalTaskDefinitionException(String.format("Root stage not found, taskName: %s", taskDefinitionBO.getName()));
        }
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
        if (connectedStages.size() != taskDefinitionBO.getStageDefinitionBOMap().size() || connectedStages.containsAll(taskDefinitionBO.getStageDefinitionBOMap().keySet())) {
            throw new IllegalTaskDefinitionException(String.format("There are unreachable stages: %s",
                    String.join(",", Sets.difference(taskDefinitionBO.getStageDefinitionBOMap().keySet(), connectedStages))));
        }
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
        List<String> toStageNames = taskDefinitionBO.getStageDefinitionBOMap().get(currentStageName).getToStageNames();
        if (toStageNames.isEmpty()) {
            return;
        }
        dfsPath.add(currentStageName);
        for (String toStageName : toStageNames) {
            if (dfsPath.contains(toStageName)) {
                throw new IllegalTaskDefinitionException(String.format("there is a circle in task %s : %s", taskDefinitionBO.getName(),
                        String.join("->", dfsPath)));
            }
            validateCircle(toStageName, taskDefinitionBO, dfsPath, connectedStages);
        }
        dfsPath.remove(currentStageName);
    }

}
