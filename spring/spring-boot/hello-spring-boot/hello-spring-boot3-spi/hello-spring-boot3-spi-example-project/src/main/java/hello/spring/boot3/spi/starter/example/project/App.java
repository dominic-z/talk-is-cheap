package hello.spring.boot3.spi.starter.example.project;


import hello.spring.boot3.spi.starter.starter.config.EnableStarterAnnoConfiguration;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableStarterAnnoConfiguration
public class App implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(App.class);
    }



    @Override
    public void run(String... args) throws Exception {

    }
}
