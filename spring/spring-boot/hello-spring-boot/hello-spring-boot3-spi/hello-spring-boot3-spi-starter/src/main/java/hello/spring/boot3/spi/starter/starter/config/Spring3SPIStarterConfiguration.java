package hello.spring.boot3.spi.starter.starter.config;


import hello.spring.boot3.spi.starter.starter.config.properties.StarterConfigProperties;
import hello.spring.boot3.spi.starter.starter.controller.StarterControllerImpl;
import hello.spring.boot3.spi.starter.starter.service.HelloService;
import hello.spring.boot3.spi.starter.starter.service.HiService;
import hello.spring.boot3.spi.starter.starter.service.impl.HelloServiceImpl;
import hello.spring.boot3.spi.starter.starter.service.impl.HiServiceImpl;
import hello.spring.boot3.spi.starter.trigger.ClassTrigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

// 经过测试，不需要注解
//@Configuration

// 见readme
@EnableConfigurationProperties(value = StarterConfigProperties.class)
public class Spring3SPIStarterConfiguration {
    /**
     * 如果容器中没有叫做starterHelloService的bean，这个bean就会被创建出来
     *
     * @return
     */
    @Bean
    @ConditionalOnMissingBean(name = "starterHelloService")
    public HelloService helloService() {
        return new HelloServiceImpl();
    }


    /**
     * 如果容器中没有HiService的bean，这个bean就会被创建出来
     *
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
     *
     * @return
     */
    @Bean
    @ConditionalOnClass(value = ClassTrigger.class)
    public HiService conditionOnClassHiService() {
        return new HiServiceImpl();
    }

    /**
     * 让依赖这个starter的项目具备这个controller
     * 这里必须得返回Impl而不是接口，这是因为要想这个bean被requestMappingHandler接管，这个bean的类的定义上必须有RestControler
     * 而因为习惯问题，我想让接口定义不带任何注解，这样这个接口还可以作为FeignClient
     * 我将@Restcontroller和@RequestMapping注解没有写在接口上，而是写在了实现类上
     */
    @Bean
    public StarterControllerImpl starterController() {
        return new StarterControllerImpl();
    }

    /**
     * 测试starter中能不能自动构造出StarterConfigProperties，发现是可以的。
     *
     * @param starterConfigProperties
     * @return
     */
    @Bean(name = "testStarterConfigProperties")
    public Object starterConfigProperties(@Autowired StarterConfigProperties starterConfigProperties) {
        System.out.println("============读取starterConfigProperties" + starterConfigProperties);
        return starterConfigProperties;
    }

    // 见MyApplicationStartedEventListener注释
//    @Bean
//    public MyApplicationStartedEventListener myApplicationStartedEventListener(){
//        return new MyApplicationStartedEventListener();
//    }
}
