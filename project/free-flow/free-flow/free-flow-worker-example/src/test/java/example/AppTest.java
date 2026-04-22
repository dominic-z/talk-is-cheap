package example;


import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.talk.is.cheap.project.free.example.App;
import org.talk.is.cheap.project.free.flow.starter.repository.service.es.StageExecutionBizLogService;
import org.talk.is.cheap.project.free.flow.starter.worker.task.definition.annotaion.task.Task;
import org.talk.is.cheap.project.free.flow.starter.worker.client.SchedulerClusterInternalClient;
import org.talk.is.cheap.project.free.flow.starter.worker.config.properties.ZKConfigProperties;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

@SpringBootTest(classes = App.class)
@Slf4j
public class AppTest {


    @Autowired
    private ZKConfigProperties zkConfigProperties;

    @Autowired
    private CuratorFramework starterCuratorZKClient;

    @Autowired
    private SchedulerClusterInternalClient schedulerClusterClient;

    @Autowired
    private StageExecutionBizLogService stageExecutionBizLogService;


    @Autowired
    private ApplicationContext applicationContext;


    @Test
    public void testDI(){
//        log.info("zkConfigProperties: {}",zkConfigProperties);
//        log.info("starterCuratorZKClient: {}",starterCuratorZKClient);
//        log.info("SchedulerClusterClient: {}",schedulerClusterClient);

//        log.info("beansWithAnnotation {}",applicationContext.getBeansWithAnnotation(Task.class));
//        Map<String, Object> beansWithAnnotation = applicationContext.getBeansWithAnnotation(Task.class);
//        log.info("beansWithAnnotation {}", beansWithAnnotation);
//
//        for (Object value : beansWithAnnotation.values()) {
//            for (Method method : value.getClass().getMethods()) {
//                System.out.println(Arrays.toString(method.getAnnotations()));
//            }
//        }


    }

    @Test
    public void testMapper(){
//        stageExecutionBizLogService.logAsync(20L,"hahah");
    }




}