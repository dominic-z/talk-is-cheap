package org.talk.is.cheap.project.free.flow.common.task.defination.annotaion.task;


import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 被FlowTask标记的类将被视为是一个任务流
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
@Scope("prototype")
public @interface Task {


    /**
     * 全局唯一，作为task的标识符
     * @return
     */
    String name();

    /**
     * 版本号
     * @return
     */
    long version() default 0;

    /**
     * 重试次数
     * @return
     */
    int retryCount() default 3;


    /**
     * 超时时间，默认不生效
     * @return
     */
    int timeoutInSeconds() default -1;

}
