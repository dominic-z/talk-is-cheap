package org.talk.is.cheap.spring.security.csdn.tutorial.demo.security;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.dao.mysql.UserMapper;
import org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.message.ResponseResult;
import org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.model.User;
import org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.service.LoginService;

@SpringBootTest
@Slf4j
class Security1ApplicationTests {

	@Test
	void contextLoads() {
	}

	@Autowired
	private UserMapper userMapper;

	@Autowired
	private AuthenticationManager authenticationManager;

	@Test
	public void testUserMapper(){
		User littleZ = userMapper.selectUserByUsername("littleZ");
		log.info("{}",littleZ);
		log.info("{}",authenticationManager);
	}

	@Autowired
	private LoginService loginService;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Test
	public void testPwdEncoder(){
		String encode = passwordEncoder.encode("1234");
		log.info("encode pw {}", encode);

		boolean matches = passwordEncoder.matches("1234", encode);
		log.info("matches {}",matches);
	}

	@Test
	public void testLoginService(){
		User user = new User();
		user.setUsername("littleZ");
		user.setPassword("1234");
		ResponseResult result = loginService.login(user);
		log.info("{}",result);
	}
}
