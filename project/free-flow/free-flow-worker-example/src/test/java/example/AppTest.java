package example;


import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.talk.is.cheap.project.free.example.App;
import org.talk.is.cheap.project.free.flow.starter.worker.config.properties.ZKConfigProperties;

@SpringBootTest(classes = App.class)
@Slf4j
public class AppTest {


    @Autowired
    private ZKConfigProperties zkConfigProperties;

    @Autowired
    private CuratorFramework starterCuratorZKClient;

    @Test
    public void testDI(){
        log.info("zkConfigProperties: {}",zkConfigProperties);
        log.info("starterCuratorZKClient: {}",starterCuratorZKClient);
    }
}