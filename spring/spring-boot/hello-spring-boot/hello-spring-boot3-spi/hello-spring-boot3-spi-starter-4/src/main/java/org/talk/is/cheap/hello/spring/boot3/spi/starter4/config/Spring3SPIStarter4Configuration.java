package org.talk.is.cheap.hello.spring.boot3.spi.starter4.config;


import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.talk.is.cheap.hello.spring.boot3.spi.starter4.service.UseServiceFromOtherStarter;


/**
 * AutoConfigureAfter注解用于控制自动配置类的加载顺序，例如现在有两个AutoConfiguration类，autoConfig1和autoConfig2
 * 其中autoConfig1之中有一个会在ConditionOnMissingBean的条件下创建一个叫product的bean，而autoConfig2刚好会创建一个product的bean
 * 如果autoConfig1先于autoConfig2加载，那么autoConfig1中的ConditionOnMissingBean会看不到autoConfig2创建的product，那么结果就是两个autoconfig都会创建product
 * 如果autoConfig2先于autoConfig1加载，就只会有autoConfig2创建的bean了，autoConfig1收到COnditionOnMissingBean的影响，不会创建bean
 *
 * 另外，AutoConfigureAfter只影响加载配置类的顺序，并不会对实际执行创建bean的代码的执行顺序造成影响，也就是说，他只影响各种Condition的判断，不会对对象实际创建的顺序造成影响
 *
 * [autoconfigure会影响什么？](https://www.doubao.com/thread/wad2bd0a379e9444f)
 * 至于name里填什么，可以启动一个application然后通过applicationContext里捞bean的name
 */
@Configuration
@ComponentScan(basePackageClasses = UseServiceFromOtherStarter.class)
public class Spring3SPIStarter4Configuration {



}
