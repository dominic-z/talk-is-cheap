package org.talk.is.cheap.project.free.flow.starter.worker.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.talk.is.cheap.project.free.flow.starter.worker.task.definition.annotaion.task.Task;

import java.util.Map;

/**
 * 管理当前worker中的task
 */
@Slf4j
public class WorkerTaskValidator {


    @Autowired
    private ApplicationContext applicationContext;

    /**
     * 校验当前
     */
    public void validateTaskDefinition(){
        Map<String, Object> taskBean = applicationContext.getBeansWithAnnotation(Task.class);

        /**
         * todo:
         * 校验Task：
         * 1. 全部task下是否有重名
         * 2. 每个task是否是一个dag
         * 3. 和现有的task定义是否存在冲突
         */

    }

}
