package geektime.demo;

import geektime.demo.domain.enums.AgeBracket;
import geektime.demo.message.HelloPostRequest;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class HelloWorldApplication {

	public static void main(String[] args) {
		SpringApplication.run(HelloWorldApplication.class, args);
	}

	@RequestMapping(value = "/convertAgeBracket", method = {RequestMethod.GET})
	public String convertAgeBracket(@RequestParam("ageBracket") AgeBracket ageBracket) {
		System.out.println(ageBracket);
		return "Hello World!";
	}

	@RequestMapping(value = "/helloPost", method = {RequestMethod.POST}, produces = MediaType.APPLICATION_JSON_VALUE)
	public String helloPost(@RequestBody HelloPostRequest request) {
		// RequestBody使用的是Jackson实现类型映射
		System.out.println(request);
		return "Hello World!";
	}

	@RequestMapping(value = "/hello")
	public String hello() {
		return "Hello World!";
	}
}

