package hello.spring.boot3.spi.starter.starter.config;


import org.springframework.context.annotation.Import;

import java.lang.annotation.*;


/**
 * 也算是Spi的自动加载机制，只不过没有利用spring.factories
 * 来自https://cloud.tencent.com/developer/article/2306024
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
//以上元注解直接找个@EnableXxx类 拷贝进来即可
@Import(AnnotationStarterConfiguration.class)
public @interface EnableStarterAnnoConfiguration {
}
