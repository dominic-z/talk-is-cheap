package org.talk.is.cheap.project.free.flow.starter.worker.task.defination.annotaion.task;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 被这个注解标记的类将作为一个预定任务
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
@Scope("prototype")
public @interface ScheduleTask {

    /**
     * 任务的唯一标识符，全局唯一
     * @return
     */
    String name();

    /**
     * 版本号
     * @return
     */
    long version() default 0;

    /**
     * 定时调度需要调度起哪个task
     * @return
     */
    String taskName();

}
