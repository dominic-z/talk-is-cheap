package hello.spring.boot3.spi.starter.config;


import hello.spring.boot3.spi.starter.service.HelloService;
import hello.spring.boot3.spi.starter.service.HiService;
import hello.spring.boot3.spi.starter.service.impl.HelloServiceImpl;
import hello.spring.boot3.spi.starter.service.impl.HiServiceImpl;
import hello.spring.boot3.spi.trigger.ClassTrigger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

// 测试，不需要注解
//@Configuration
public class Spring3SPIStarterConfiguration {
    /**
     * 如果容器中没有叫做starterHelloService的bean，这个bean就会被创建出来
     * @return
     */
    @Bean
    @ConditionalOnMissingBean(name = "starterHelloService")
    public HelloService helloService() {
        return new HelloServiceImpl();
    }


    /**
     * 如果容器中没有HiService的bean，这个bean就会被创建出来
     * @return
     */
    @Bean
    @ConditionalOnMissingBean(value = HiService.class)
    public HiService hiService() {
        return new HiServiceImpl();
    }


    /**
     * 如果项目中的classpath没有ClassTrigger这个类，简单说就是pom中没有引入，
     * 那么这个bean就会被创建出来
     * @return
     */
    @Bean
    @ConditionalOnClass(value = ClassTrigger.class)
    public HiService conditionOnClassHiService() {
        return new HiServiceImpl();
    }
}
