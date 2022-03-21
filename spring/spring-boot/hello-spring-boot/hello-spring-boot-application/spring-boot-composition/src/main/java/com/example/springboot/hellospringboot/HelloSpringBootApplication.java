package com.example.springboot.hellospringboot;

import com.example.springboot.hellospringboot.dao.jdbc.template.ItemJdbcDao;
import lombok.extern.slf4j.Slf4j;
import org.example.spring.starter.log.Loggers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
// @SpringBootApplication(exclude={DataSourceAutoConfiguration.class}) 如果没有手动配置datasource，那么就需要关闭数据源自动配置
@Slf4j(topic = "app_root")
@EnableFeignClients
@EnableDiscoveryClient
public class HelloSpringBootApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(HelloSpringBootApplication.class, args);
    }

    @Autowired
    private ItemJdbcDao itemJdbcDao;


    @Override
    public void run(String... args) throws Exception {
//		List<Item> items = itemJdbcDao.getItems();
//		items.stream().limit(20).forEach(item -> log.info(item.toString()));
//		log.error("test e",new RuntimeException("error test"));
        Loggers.ERROR_LOG.error("error_log test log back error", new RuntimeException());
        log.error("test log back error", new RuntimeException());
    }
}
