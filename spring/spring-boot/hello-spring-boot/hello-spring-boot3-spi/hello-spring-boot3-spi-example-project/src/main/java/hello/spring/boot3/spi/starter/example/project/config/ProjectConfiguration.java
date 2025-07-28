package hello.spring.boot3.spi.starter.example.project.config;


import hello.spring.boot3.spi.starter.example.project.service.ProjectHelloServiceImpl;
import hello.spring.boot3.spi.starter.example.project.service.ProjectHiServiceImpl;
import hello.spring.boot3.spi.starter.starter.service.HiService;
import org.springframework.context.annotation.Configuration;


/**
 * 可以控制starter中的conditionOnMissingBean
 * 如果注释，那么就会使用starter中的bean
 * @return
 */
@Configuration
public class ProjectConfiguration {



//    @Bean(name = "starterHelloService")
    public ProjectHelloServiceImpl helloService() {
        return new ProjectHelloServiceImpl();
    }


//    @Bean
    public HiService hiService() {
        return new ProjectHiServiceImpl();
    }
}
