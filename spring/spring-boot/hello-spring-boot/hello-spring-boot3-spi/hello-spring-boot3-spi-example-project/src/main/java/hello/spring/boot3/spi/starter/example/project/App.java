package hello.spring.boot3.spi.starter.example.project;


import hello.spring.boot3.spi.starter.config.EnableStarterAnnoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableStarterAnnoConfiguration
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class);
    }
}
