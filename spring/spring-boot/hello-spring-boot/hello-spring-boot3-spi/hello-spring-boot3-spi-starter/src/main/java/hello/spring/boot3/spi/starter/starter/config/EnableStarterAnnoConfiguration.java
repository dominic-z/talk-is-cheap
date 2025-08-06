package hello.spring.boot3.spi.starter.starter.config;


import org.springframework.context.annotation.Import;

import java.lang.annotation.*;


/**
 * 倒不算是Spi的自动加载机制，没有利用spring.factories，但也是另一种触发自动配置的方法
 * 直接在应用的主函数中，挂上一个@EnableStarterAnnoConfiguration注解，那么EnableStarterAnnoConfiguration本身的注解也会生效
 * 来自https://cloud.tencent.com/developer/article/2306024
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
//以上元注解直接找个@EnableXxx类 拷贝进来即可
@Import(AnnotationStarterConfiguration.class)
public @interface EnableStarterAnnoConfiguration {
}
