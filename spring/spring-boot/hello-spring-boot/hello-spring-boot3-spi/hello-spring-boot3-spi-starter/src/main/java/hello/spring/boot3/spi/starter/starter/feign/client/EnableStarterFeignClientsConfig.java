package hello.spring.boot3.spi.starter.starter.feign.client;


import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(clients = HiClient.class)
public class EnableStarterFeignClientsConfig {
}
