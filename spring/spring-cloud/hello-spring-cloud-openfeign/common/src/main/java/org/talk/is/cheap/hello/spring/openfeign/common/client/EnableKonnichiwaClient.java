package org.talk.is.cheap.hello.spring.openfeign.common.client;


import org.springframework.cloud.openfeign.EnableFeignClients;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@EnableFeignClients(basePackageClasses = KonnichiwaClient.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EnableKonnichiwaClient {
}
