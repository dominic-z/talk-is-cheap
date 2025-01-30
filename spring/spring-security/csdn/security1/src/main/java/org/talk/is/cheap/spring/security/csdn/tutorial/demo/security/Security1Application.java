package org.talk.is.cheap.spring.security.csdn.tutorial.demo.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class Security1Application implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(Security1Application.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		log.info("{}",123);
	}
}
