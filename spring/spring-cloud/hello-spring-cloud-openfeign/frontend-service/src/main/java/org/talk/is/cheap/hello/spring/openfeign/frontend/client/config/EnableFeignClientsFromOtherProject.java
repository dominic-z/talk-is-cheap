package org.talk.is.cheap.hello.spring.openfeign.frontend.client.config;


import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.talk.is.cheap.hello.spring.openfeign.common.client.EnableKonnichiwaClient;
import org.talk.is.cheap.hello.spring.openfeign.common.client.KonnichiwaClient;


/**
 * 即使这个Client来自于其他的依赖，可以可以通过这种方式来激活并创建一个Feign客户端
 * EnableFeignClients的源码注释是这样描述的。
 *
 */
@Configuration
// 下面三种方法等效
//@EnableFeignClients(basePackageClasses = KonnichiwaClient.class)
//@EnableFeignClients(basePackages = "org.talk.is.cheap.hello.spring.openfeign.common.client")
@EnableKonnichiwaClient
public class EnableFeignClientsFromOtherProject {
}
