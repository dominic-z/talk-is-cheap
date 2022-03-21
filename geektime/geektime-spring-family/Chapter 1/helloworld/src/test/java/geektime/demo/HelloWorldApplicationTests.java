package geektime.demo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;

@RunWith(SpringRunner.class)
@SpringBootTest
public class HelloWorldApplicationTests {

	@Test
	public void contextLoads() {
		System.out.println(Arrays.toString(String.class.getEnumConstants()));
	}

}

