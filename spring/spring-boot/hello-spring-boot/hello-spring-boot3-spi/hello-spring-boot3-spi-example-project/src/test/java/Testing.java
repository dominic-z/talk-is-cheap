import hello.spring.boot3.spi.starter.example.project.App;
import hello.spring.boot3.spi.starter.factory.MyBootstrapRegistryInitializer;
import hello.spring.boot3.spi.starter.service.HelloService;
import hello.spring.boot3.spi.starter.service.HiService;
import hello.spring.boot3.spi.starter.service.HijService;
import hello.spring.boot3.spi.starter.service.KonnichiwaService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest(classes = App.class)
@Slf4j
public class Testing {

    @Autowired
    HiService hiService;

    @Autowired
    HelloService helloService;


    @Autowired
    @Qualifier("conditionOnClassHiService")
    HiService conditionOnClassHiService;

    @Test
    public void say() {
        hiService.hi();
        helloService.sayHello();

        conditionOnClassHiService.hi();
    }


    @Autowired
    MyBootstrapRegistryInitializer.MyEarlyComponent myEarlyComponent;

    @Test
    public void bootstrapComponentDoSomething() {
        myEarlyComponent.doSomething();
    }


    @Value("${app.custom.property}")
    private String property;

    @Value("${server.port}")
    private int port;

    @Test
    public void testEnvPostProcessing() {
        log.info("property: {}, port: {}", property, port);
    }


    @Value("${property.source.key}")
    private String key;

    @Test
    public void testPropertySource() {
        log.info("property.source.key: {}", key);
    }


    @Value("${starter.configuration.properties}")
    private String starterConfigurationProperty;

    @Test
    public void testStarterConfigurationProperty() {
        log.info("starter.configuration.properties: {}", starterConfigurationProperty);
    }

    @Autowired
    ApplicationContext applicationContext;
    @Value("${app.initialized}")
    boolean appInitialized;
    @Autowired
    private HijService hijService;

    @Test
    public void testApplicationContext() {
        log.info("appContext id: {}", applicationContext.getId());
        log.info("app.initialized: {}", appInitialized);
        log.info("hijService.hij(): {}", hijService.hij());
    }


    @Autowired
    private KonnichiwaService konnichiwaService;

    @Test
    public void testEnableAnnoConfig() {
        log.info("konnichiwaService.konnichiwa(): {}", konnichiwaService.konnichiwa());
    }
}
