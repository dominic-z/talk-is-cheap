package org.talk.is.cheap.hippo4j;


import cn.hippo4j.core.enable.EnableDynamicThreadPool;
import org.talk.is.cheap.hippo4j.service.WorkerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableDynamicThreadPool
public class App implements CommandLineRunner {


    @Autowired
    private WorkerService workerService;
    public static void main(String[] args) {
        SpringApplication.run(App.class,args);
    }

    @Override
    public void run(String... args) throws Exception {
        workerService.logPool();
        int c = 1;
        while(c<100){
            c++;
            workerService.doTask(100);
        }
    }
}
