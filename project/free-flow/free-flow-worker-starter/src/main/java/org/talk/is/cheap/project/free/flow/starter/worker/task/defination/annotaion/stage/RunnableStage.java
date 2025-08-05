package org.talk.is.cheap.project.free.flow.starter.worker.task.defination.annotaion.stage;


import org.talk.is.cheap.project.free.flow.starter.worker.task.codec.InputCodec;
import org.talk.is.cheap.project.free.flow.starter.worker.task.codec.SimpleStringInputCodec;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 普通的运行一次的阶段
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RunnableStage {

    /**
     * 阶段的唯一标识，同一个flow下要唯一
     * @return
     */
    String name();

    /**
     * 版本号
     * @return
     */
    int version() default 0;

    /**
     * 用于input的编码和解码器类型
     * @return
     */
    Class<? extends InputCodec<?>> inputCodec() default SimpleStringInputCodec.class;

    /**
     * 指向当前stage完成后的下一组stage的name
     * @return
     */
    String[] to() default {};


    /**
     * 是否是一个task的起始stage
     * @return
     */
    boolean isStartingStage() default false;

}
