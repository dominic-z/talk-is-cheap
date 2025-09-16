package org.talk.is.cheap.project.free.flow.starter.worker.task.definition.annotaion.task;


import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.talk.is.cheap.project.free.flow.common.task.codec.InputCodec;
import org.talk.is.cheap.project.free.flow.common.task.codec.SimpleStringInputCodec;

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
    int version();

    
    /**
     * 这个task的全局共享上下文的编解码器
     * @return
     */
    Class<? extends InputCodec<?>> sharedContextCodecClass() default SimpleStringInputCodec.class;
    /**
     * 重试次数
     * @return
     */
    int maxRetryCount() default 3;


    /**
     * 超时时间，默认不生效
     * @return
     */
    int timeout() default -1;

}
