package org.talk.is.cheap.spring.security.csdn.tutorial.demo.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.model.User;
import org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.utils.JsonUtil;
import org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.utils.JwtUtil;
import org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.utils.WebUtil;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

@Slf4j
public class Testing {


    @Test
    public void testJwtUtil() throws Exception {
        String jwt = JwtUtil.createJWT("zxy");
        System.out.println("jwt: " + jwt);

        Claims claims = JwtUtil.parseJWT(jwt);
        System.out.println("claims: " + claims);
    }

    @Test
    public void testJsonUtil() throws JsonProcessingException {
        String json = JsonUtil.toJsonString("河漏");

        System.out.println(json);
        String obj = JsonUtil.fromJsonString(json, String.class);
        System.out.println(obj);
    }

    @Test
    public void testWebUtil() throws IOException {
        HttpServletResponse resp = new MockHttpServletResponse();
        resp.setHeader("aa", "bb");
        System.out.println(resp.getHeader("aa"));
        WebUtil.renderString(resp, "some text");
    }

    @Test
    public void mockMysqlDb() {
        User user = new User();
        user.setId(2L);
        user.setUsername("littleZ");
        user.setNickname("pingu");
        user.setPassword("{noop}1234");
        user.setStatus("0");
        user.setEmail("@qq");
        user.setPhonenumber("13012344321");
        user.setSex("0");
        user.setAvatar("https://localhost/avatar/pingu.png");
        user.setCreateBy(1L);
        user.setCreateTime(new Date());
        user.setUpdateBy(1L);
        user.setUpdateTime(new Date());
        user.setDelFlag(0);
        try {
            String jsonString = JsonUtil.toJsonString(user);

            Path userDbPath = Paths.get("./user.db");

            System.out.println(jsonString);

            try (BufferedWriter fileWriter = new BufferedWriter(new FileWriter(userDbPath.toFile()))) {
                fileWriter.append(jsonString);
                fileWriter.newLine();
            }
            System.out.println(userDbPath.toAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    public void loadUserDB() {
        Path userDbPath = Paths.get("./user.db");

        try (BufferedReader reader = new BufferedReader(new FileReader(userDbPath.toFile()))) {

            String json = reader.readLine();
            User user = JsonUtil.fromJsonString(json, User.class);
            System.out.println(user);
            System.out.println(reader.readLine());


        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
