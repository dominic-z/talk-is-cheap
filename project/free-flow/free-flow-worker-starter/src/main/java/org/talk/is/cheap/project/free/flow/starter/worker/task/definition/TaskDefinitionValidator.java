package org.talk.is.cheap.project.free.flow.starter.worker.task.definition;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.talk.is.cheap.project.free.flow.starter.worker.task.codec.InputCodec;
import org.talk.is.cheap.project.free.flow.starter.worker.task.definition.annotaion.stage.RunnableStage;
import org.talk.is.cheap.project.free.flow.starter.worker.task.definition.annotaion.task.Task;
import org.talk.is.cheap.project.free.flow.starter.worker.task.definition.bo.StageDefinitionBO;
import org.talk.is.cheap.project.free.flow.starter.worker.task.definition.bo.TaskDefinitionBO;
import org.talk.is.cheap.project.free.flow.starter.worker.task.definition.exception.IllegalTaskDefinitionException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class TaskDefinitionValidator {

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * 校验task定义，要求：
     * 1. 无环
     * 2. 有明确的根节点
     * 3. 每个stage的toStage都有效
     * 4. 图是连通的，没有悬挂的节点
     *
     * @throws IllegalTaskDefinitionException
     */
    public void validateTaskDefinition() throws IllegalTaskDefinitionException {
        Map<String, Object> taskBeans = applicationContext.getBeansWithAnnotation(Task.class);

        Map<String, TaskDefinitionBO> taskDefinitionBOMap = new HashMap<>();
        for (String beanName : taskBeans.keySet()) {
            Object taskBean = taskBeans.get(beanName);
            Class<?> taskClass = taskBean.getClass();
            Task taskAnnotation = taskClass.getAnnotation(Task.class);
            String taskName = taskAnnotation.name();
            int version = taskAnnotation.version();
            int maxRetryCount = taskAnnotation.maxRetryCount();
            int timeoutInSeconds = taskAnnotation.timeoutInSeconds();

            if (StringUtils.isBlank(taskName)) {
                throw new IllegalTaskDefinitionException(String.format("taskName can't be blank, class: %s", taskClass.getName()));
            }

            if (taskDefinitionBOMap.containsKey(taskName)) {
                throw new IllegalTaskDefinitionException(String.format("Found a task name that is duplicated: %s", taskName));
            }

            TaskDefinitionBO taskDefinitionBO = TaskDefinitionBO.builder()
                    .name(taskName)
                    .version(version)
                    .maxRetryCount(maxRetryCount)
                    .timeoutInSecond(timeoutInSeconds).build();

            Method[] declaredMethods = taskClass.getDeclaredMethods();
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

                    taskDefinitionBO.getGraph().put(stageDefinitionBO.getName(), stageDefinitionBO.getToStageNames());
                }
            }

            for (List<String> toStageNames : taskDefinitionBO.getGraph().values()) {
                for (String toStageName : toStageNames) {
                    if (!taskDefinitionBO.getStageDefinitionBOMap().containsKey(toStageName)) {
                        throw new IllegalTaskDefinitionException(String.format("Undefined stage name found: %s", toStageName));
                    }
                }
            }

            if (taskDefinitionBO.getRoots().isEmpty()) {
                throw new IllegalTaskDefinitionException(String.format("Root stage not found, taskName: %s", taskName));
            }
            // todo: 判断连通性

            taskDefinitionBOMap.put(taskName, taskDefinitionBO);
        }

    }

}
